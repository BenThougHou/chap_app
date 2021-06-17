package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

import com.yiliao.domain.MessageEntity;
import com.yiliao.domain.SmsDescribe;
import com.yiliao.domain.User;
import com.yiliao.domain.WalletDetail;
import com.yiliao.evnet.PushMesgEvnet;
import com.yiliao.evnet.PushSms;
import com.yiliao.service.GoldComputeService;
import com.yiliao.service.LoginService;
import com.yiliao.util.ChannelSettlementThread;
import com.yiliao.util.CharUtil;
import com.yiliao.util.DateUtils;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.KeyWordUtil;
import com.yiliao.util.Md5Util;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.PoolThread;
import com.yiliao.util.PrintUtil;
import com.yiliao.util.RedisUtil;
import com.yiliao.util.SpringConfig;
import com.yiliao.util.SystemConfig;
import com.yiliao.util.UpdateUserDataThread;

import net.sf.json.JSONObject;

/**
 * 登录相关服务实现
 * 
 * @author Administrator
 * 
 */
@Service("loginAppService")
public class LoginServiceImpl extends ICommServiceImpl implements LoginService {


	private GoldComputeService goldComputeService = (GoldComputeService) SpringConfig.getInstance()
			.getBean("goldComputeService");

	@Autowired
	RedisUtil redisUtil;

	// 手动事物
	@Autowired
	private DataSourceTransactionManager transactionManager;

	/**
	 * 登录
	 */
	@Override
	public MessageUtil login(String phone, String smsCode, String t_phone_type, String t_system_version,
			String t_ip_address, String deviceNumber, int shareUserId) {
		MessageUtil mu = null;
		try {
			// 获取当前验证码是否存在
			SmsDescribe sd = (SmsDescribe) JSONObject
					.toBean(JSONObject.fromObject(redisUtil.getKeyObject("verify_" + phone)), SmsDescribe.class);

			// 判断验证码是否正确
			if (null == sd || !sd.getSmsCode().equals(smsCode)) {
				return new MessageUtil(0, "账号或者验证码错误!");
			}

			Map<String, Object> user_data = getUserIsExist(phone, null, null);
			String t_token = UUID.randomUUID().toString().replaceAll("-", "");

			// 已经注册过用户了
			if (null != user_data && !user_data.isEmpty() && null != user_data.get("t_id")) {
				int user_id = Integer.parseInt(user_data.get("t_id").toString());
				// 表示用户已被封号或者永久禁用
				if (!"0".equals(user_data.get("t_disable").toString())) {
					return getUserDesTime(Integer.parseInt(user_data.get("t_disable").toString()), user_id);
				}
				user_data.put("t_token", t_token);
				// 修改用户为空状态
				upateLoginState(user_id,user_data.get("t_disable").toString(), t_token);
				// 设置用户余额
				user_data.put("gold",
						new BigDecimal(null == user_data.get("amount") ? "0" : user_data.get("amount").toString()));

				if ("iPhone".equals(t_phone_type)) {
					this.executeSQL("UPDATE t_user SET t_online_setup = 1,t_onLine = ? WHERE t_id = ? ",
							User.ONLINE_IDLE, user_id);
				}
				mu = new MessageUtil();
				mu.setM_istatus(1);
				mu.setM_strMessage("登陆成功!");
				mu.setM_object(user_data);
			} else {
				// 判断当前设备是否已经注册
				if (!StringUtils.isNotBlank(deviceNumber) || !getDeviceIsInstal(deviceNumber, t_ip_address)) {
					//return new MessageUtil(-200, "该设备已经注册过账号了!");
				}

				user_data = saveUser(null, null, null, null, phone, null, null, null, null, null, t_phone_type,
						t_system_version, t_ip_address, deviceNumber, null, shareUserId, t_token);
				user_data.put("t_token", t_token);
				mu = new MessageUtil();
				mu.setM_istatus(1);
				mu.setM_strMessage("注册成功~!");
				mu.setM_object(user_data);
			}

			redisUtil.remove("verify_" + phone);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("登录异常!", e);
			mu = new MessageUtil(0, "登录异常!");
		}
		return mu;
	}

	/*
	 * 修改在线状态 (non-Javadoc)
	 * 
	 * @see com.yiliao.service.app.LoginService#updateAnchorOnline(int, int)
	 */
	@Override
	public MessageUtil updateAnchorOnline(int userId, int type) {
		MessageUtil mu = null;
		try {

			String sql;

			switch (type) {
			case 0: // 空闲
				// 登陆时设置用户 在线切空闲
				sql = "UPDATE t_user SET t_onLine=? WHERE t_id=? ;";
				this.executeSQL(sql, User.ONLINE_IDLE, userId);
				break;
			case 1: // 忙碌
				sql = "UPDATE t_user SET t_onLine=? WHERE t_id=? ;";
				this.executeSQL(sql, User.ONLINE_BE_BUSY, userId);
				break;
			case 2: // 离线
				// 离线状态时还需要修改用户表中的在先状态
				sql = "UPDATE t_user SET t_onLine=? WHERE t_id=1 ;";
				this.executeSQL(sql, User.ONLINE_OFF_LINE, userId);
				break;
			}

			mu = new MessageUtil(1, "更新成功!");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("修改在先状态异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 获取设备是否已经注册过了
	 * 
	 * @param device
	 * @return
	 */
	@Transactional
	public boolean getDeviceIsInstal(String device, String userIp) {
		// 获取设备列表
		List<Map<String, Object>> sqlList = this
				.getQuerySqlList("SELECT * FROM t_device_login_log WHERE t_device_number = ?", device);

		Map<String, Object> total = this
				.getMap("SELECT COUNT(t_id) AS total FROM t_device_login_log WHERE t_user_ip = ? ", userIp);
		// 未注册返回true
		if (sqlList.isEmpty() || Integer.parseInt(total.get("total").toString()) > 5) {
			// 3.获得事务状态
			TransactionStatus status = this.getStatus();

			// 插入设备信息
			String inSql = " INSERT INTO t_device_login_log (t_device_number,t_login_time,t_user_ip) VALUES (?,?,?) ";
			this.executeSQL(inSql, device, DateUtils.format(new Date(), DateUtils.FullDatePattern), userIp);
			// 立即提交数据
			this.transactionManager.commit(status);

			return true;
		}
		return false;
	}

	/**
	 * 微信登录
	 */
	@Override
	public MessageUtil weixinLogin(String openId, String nickName, String handImg, String city, String t_phone_type,
			String t_system_version, String t_ip_address, String deviceNumber, int shareUserId) {
		MessageUtil mu = null;
		try {

			Map<String, Object> user_data = getUserIsExist(null, null, openId);
			String t_token = UUID.randomUUID().toString().replaceAll("-", "");
			logger.info("查询出来的对象-->{}", user_data.toString());
			// 判断用户是否存在 如果存在查询用户余额信息
			if (null != user_data && !user_data.isEmpty() && null != user_data.get("t_id")) {

				int user_id = Integer.parseInt(user_data.get("t_id").toString());

				// 表示用户已被封号或者永久禁用
				if (!"0".equals(user_data.get("t_disable").toString())) {
					return getUserDesTime(Integer.parseInt(user_data.get("t_disable").toString()), user_id);
				}
				user_data.put("t_token", t_token);
				// 修改用户为空状态
				upateLoginState(user_id,user_data.get("t_disable").toString(), t_token);
				// 设置用户余额
				user_data.put("gold",
						new BigDecimal(null == user_data.get("amount") ? "0" : user_data.get("amount").toString()));

				if ("iPhone".equals(t_phone_type)) {
					this.executeSQL("UPDATE t_user SET t_online_setup = 1,t_onLine = 0 WHERE t_id = ? ", user_id);
				}

				mu = new MessageUtil();
				mu.setM_istatus(1);
				mu.setM_strMessage("登陆成功!");
				mu.setM_object(user_data);

			} else { // 不存在 新增用户信息

				// 判断当前设备是否已经注册
				if (!StringUtils.isNotBlank(deviceNumber) || !getDeviceIsInstal(deviceNumber, t_ip_address)) {
					//return new MessageUtil(-200, "该设备已经注册过账号了!");
				}

				user_data = saveUser(nickName, null, null, handImg, null, null, city, null, openId, null, t_phone_type,
						t_system_version, t_ip_address, deviceNumber, null, shareUserId, t_token);
				user_data.put("t_token", t_token);
				mu = new MessageUtil();
				mu.setM_istatus(1);
				mu.setM_strMessage("注册成功~!");
				mu.setM_object(user_data);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("微信登陆异常!", e);
			mu = new MessageUtil(0, "登录异常!");
		}
		return mu;
	}

	/**
	 * 新增用户
	 * 
	 * @param t_nickName      用户昵称
	 * @param t_sex           用户性别
	 * @param t_age           用户年龄
	 * @param t_handImg       用户
	 * @param t_phone         用户电话
	 * @param t_constellation 用户星座
	 * @param t_city          用户城市
	 * @param t_vocation      用户职业
//	 * @param marking         推广标示
	 */
	private Map<String, Object> saveUser(String t_nickName, Integer t_sex, Integer t_age, String t_handImg,
			String t_phone, String t_constellation, String t_city, String t_vocation, String openId, String qqopenId,
			String t_phone_type, String t_system_version, String t_ip_address, String deviceNumber, String password,
			int shareUserId, String t_token) {

		logger.info("手机型号->{},系统版本->{},ip地址->{}", t_phone_type, t_system_version, t_ip_address);

		int refereeId = 0;

		if (shareUserId > 0) {
			refereeId = shareUserId;
		}
		// 查询该用户是否存在推广人
		if (refereeId == 0 && StringUtils.isNotBlank(t_phone_type) && StringUtils.isNotBlank(t_system_version)) {
			// 查询该用户是否存在推荐人
			String sql = "SELECT t_id,t_referee_id FROM t_device WHERE t_phone_type = ? AND t_system_version = ? AND t_ip_address = ? AND t_is_use = 0";
			List<Map<String, Object>> referee = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql,
					t_phone_type.trim(), t_system_version.trim(), t_ip_address.trim());

			if (!referee.isEmpty()) {
				refereeId = Integer.parseInt(referee.get(0).get("t_referee_id").toString());
				// 设置当前这条推广信息作废
				this.getFinalDao().getIEntitySQLDAO().executeSQL("UPDATE t_device SET t_is_use = 1 WHERE t_id = ?",
						referee.get(0).get("t_id"));
			}
		}

		StringBuffer insert = new StringBuffer();
		StringBuffer value = new StringBuffer();

		insert.append("INSERT INTO t_user (");
		value.append(" VALUES (");
		// 昵称

		if (StringUtils.isNotBlank(t_nickName)) {
			insert.append("t_nickName,");
			// 获取系统设置关键字过滤
			List<Map<String, Object>> querySqlList = this
					.getQuerySqlList("SELECT t_nickname_filter FROM t_system_setup");
			if (querySqlList.isEmpty()) {
				value.append("'").append(t_nickName).append("',");
			} else {
				String[] keyWord = querySqlList.get(0).get("t_nickname_filter").toString().split(",");
				value.append("'").append(KeyWordUtil.filterKeyWord(keyWord, t_nickName)).append("',");
			}
		}else {
			t_nickName=CharUtil.generateName();
		}
		// 性别
		if (null != t_sex) {
			insert.append("t_sex,");
			value.append(t_sex).append(",");
		}
		// 年龄
		if (null != t_age) {
			insert.append("t_age,");
			value.append(t_age).append(",");
		}
		// 头像
		if (StringUtils.isNotBlank(t_handImg)) {
			insert.append("t_handImg,");
			value.append("'").append(t_handImg).append("',");
		}
		// 手机号
		if (StringUtils.isNotBlank(t_phone)) {
			insert.append("t_phone,");
			value.append("'").append(t_phone).append("',");
		}
		// 星座
		if (StringUtils.isNotBlank(t_constellation)) {
			insert.append("t_constellation,");
			value.append("'").append(t_constellation).append("',");
		}
		// 城市
		if (StringUtils.isNotBlank(t_city)) {
			insert.append("t_city,");
			value.append("'").append(t_city).append("',");
		}
		// 用户职业
		if (StringUtils.isNotBlank(t_vocation)) {
			insert.append("t_vocation,");
			value.append("'").append(t_vocation).append("',");
		}
		// openid
		if (StringUtils.isNotBlank(openId)) {
			insert.append("t_open_id,");
			value.append("'").append(openId).append("',");
		}
		// QQ openid
		if (StringUtils.isNotBlank(qqopenId)) {
			insert.append("t_qq_open_id,");
			value.append("'").append(qqopenId).append("',");
		}
		if (StringUtils.isNotBlank(password)) {
			insert.append("t_pass_wrod,");
			value.append("'").append(Md5Util.encodeByMD5(password)).append("',");
		}

		// 其他的字段
		insert.append(
				"t_create_time, t_referee, t_role, t_disable, t_login_time,t_is_vip, t_browse_sum, t_onLine,t_token)");
		value.append("?,?,?,?,?,?,?,?,?)");

		// 拼接字符串
		insert.append(value);

		int user_id = this.getFinalDao().getIEntitySQLDAO().saveData(insert.toString(),
				DateUtils.format(new Date(), DateUtils.FullDatePattern), refereeId, User.ROLE_TYPE_USER,
				User.DISABLE_NO, DateUtils.format(new Date(), DateUtils.FullDatePattern), User.IS_VIP_NO, 0,
				User.ONLINE_IDLE, t_token);

		// 处理CPS推广注册量
		int user_Android=0;
		int user_iPhone=0;
		if(null!=t_phone_type&&t_phone_type.equalsIgnoreCase("Android")) {
			//安卓用户
			user_Android++;
		}else if(null!=t_phone_type&&t_phone_type.equalsIgnoreCase("iPhone")) {
			//苹果用户
			user_iPhone++;
		}
		ChannelSettlementThread.channelRegister(refereeId,user_Android,user_iPhone);

		new PoolThread(PoolThread.data_query_user, 1).start();
		// 多线程处理
		new UpdateUserDataThread(user_id, t_nickName).start();

		return new HashMap<String, Object>() {
			private static final long serialVersionUID = 1L;
			{
				put("t_id", user_id);
				put("t_is_vip", 1);
				put("t_is_svip",1);
				put("t_role", 0);
				put("t_sex", null);
				put("gold", 0);
			}
		};
	}

	/**
	 * 发送验证码
	 */
	@Override
	public void sendPhoneVerificationCode(String phone, String verifyCode, int resType, String smsCode,
			HttpServletResponse response) {
		MessageUtil mu = null;

		try {

			// 获取当前验证码是否存在
			SmsDescribe sms_entity = (SmsDescribe) JSONObject
					.toBean(JSONObject.fromObject(redisUtil.getKeyObject("verify_" + phone)), SmsDescribe.class);

			if (resType == 2) {
				// 获取当前手机号是否已经存在了
				String phoneSql = "SELECT t_id FROM t_user  WHERE t_phone = ? ";

				List<Map<String, Object>> da = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(phoneSql, phone);

				if (null != da && !da.isEmpty()) {
					PrintUtil.printWri(new MessageUtil(0, "该手机号已被其他用户绑定!"), response);
					return;
				}
			} else if (resType == 3) {
				// 获取当前手机号是否已经存在了
				String phoneSql = "SELECT t_id FROM t_user  WHERE t_phone = ? ";
				List<Map<String, Object>> da = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(phoneSql, phone);

				if (null == da || da.isEmpty() || da.size() == 0) {
					PrintUtil.printWri(new MessageUtil(0, "用户不存在！"), response);
					return;
				}
			} else {
				if (null == sms_entity || !verifyCode.equals(sms_entity.getImgVerify())) {
					PrintUtil.printWri(new MessageUtil(-1, "图形验证码匹配失败!"), response);
					return;
				}
			}

			// 当前手机号在5分钟内已经发送过验证码
			if (null != sms_entity && StringUtils.isNotBlank(sms_entity.getSmsCode())) {
				long send_time = sms_entity.getTime().getTime();
				long current_time = System.currentTimeMillis();
				// 当用户1分钟内多次发送 模拟返回已经发送消息
				if ((current_time - send_time) <= (1000 * 60)) {
					logger.info("--{}用户重复发送短信--", phone);
					mu = new MessageUtil(1, "发送成功!");
					PrintUtil.printWri(mu, response);
					return;
				}
				// 1分钟后发的短信 如果内存中存在 继续发送同一个短信
				smsCode = sms_entity.getSmsCode();
			}

			String sql = "SELECT appid,appkey,templateId,smsSign,t_platform_type FROM t_sms_steup WHERE t_is_enable = 0";

			List<Map<String, Object>> smsList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);

			logger.info("当前验证码{}", smsCode);

			Map<String, String> sms_map = new HashMap<>();

			if (null != smsList && !smsList.isEmpty()) {

				sms_map.put("appid", smsList.get(0).get("appid").toString());
				sms_map.put("appkey", smsList.get(0).get("appkey").toString());
				sms_map.put("templateId", smsList.get(0).get("templateId").toString());
				sms_map.put("smsSign", smsList.get(0).get("smsSign").toString());

				switch (Integer.valueOf(smsList.get(0).get("t_platform_type").toString())) {
				case 0: // 腾讯短信
					sms_map.put("sms_type", "0");
					break;
				case 1: // 阿里云短信
					sms_map.put("sms_type", "1");
					break;
				case 2: // 网易短信
					sms_map.put("sms_type", "2");
					break;
				}
			} else {
				sms_map.clear();
				sms_map.put("phone", phone);
				sms_map.put("smsCode", smsCode);
			}

			// 发送成功时 存储到内存中
			sms_entity.setPhone(phone);
			sms_entity.setSmsCode(String.valueOf(smsCode));
			sms_entity.setTime(new Timestamp(System.currentTimeMillis()));

			redisUtil.set("verify_" + phone, JSONObject.fromObject(sms_entity).toString(), 5l);

//			SmsTimer.verificationCode.put(phone, sms_entity);
			PrintUtil.printWri(new MessageUtil(1, "发送成功!"), response);

			// 调用异步发送短信

			sms_map.put("phone", phone);
			sms_map.put("smsCode", smsCode);
			// 发送成功时 存储到内存中
			// 异步通知
			this.applicationContext.publishEvent(new PushSms(sms_map));

		} catch (Exception e) {
			e.printStackTrace();
			PrintUtil.printWri(new MessageUtil(0, "程序异常!"), response);
		}

	}

	/*
	 * 修改用户性别(non-Javadoc)
	 * 
	 * @see com.yiliao.service.LoginService#upateUserSex(int, int)
	 */
	@Override
	public synchronized MessageUtil upateUserSex(int userId, int sex) {
		MessageUtil mu = null;
		try {

			String sql = "UPDATE t_user SET t_sex = ? WHERE t_id = ?";

			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, sex, userId);

			
			String sql2 = "UPDATE t_user SET t_handImg = ? WHERE t_id = ? and t_handImg is NULL";
			String t_handImg=null;
			if(sex==0) {
				t_handImg = SystemConfig.getValue("default_girl_handImg");
			}else if(sex==1) {
				t_handImg = SystemConfig.getValue("default_boy_handImg");
			}
			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql2, t_handImg, userId);
			
			
			
			// 开始赠送金币
			String qSql = " SELECT t_id,t_gold FROM t_enroll WHERE t_sex = ? ";

			List<Map<String, Object>> dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql, sex);

			// 不为null时
			if (!dataMap.isEmpty()) {

				// 判断该用户是否已经赠送过了
				qSql = " SELECT * FROM t_wallet_detail WHERE t_user_id = ? AND t_change_category =? ";

				List<Map<String, Object>> daM = this.getQuerySqlList(qSql, userId,
						WalletDetail.CHANGE_CATEGOR_GIVE_GOLD);
				// 判断是否有赠送金币
				// 赠送金币是直接到账 所以不会入账消费订单中 也不会有红包记录
				// 资源id为 注册赠送 t_enroll表中的 Id
				if (Integer.parseInt(dataMap.get(0).get("t_gold").toString()) > 0 && daM.isEmpty()) {
					// 给用户写入金币
					goldComputeService.saveChangeRecord(userId, new BigDecimal(0),
							new BigDecimal(dataMap.get(0).get("t_gold").toString()), WalletDetail.CHANGE_TYPE_INCOME,
							WalletDetail.CHANGE_CATEGOR_GIVE_GOLD, 1,
							Integer.parseInt(dataMap.get(0).get("t_id").toString()));

					// 写入通知信息
					String inSql = " INSERT INTO t_give_gold_notice (t_user_id, t_gold, t_is_read, t_create_time) VALUES (?, ?, ?, ?) ;";
					this.getFinalDao().getIEntitySQLDAO().executeSQL(inSql, userId,
							new BigDecimal(dataMap.get(0).get("t_gold").toString()), 0,
							DateUtils.format(new Date(), DateUtils.FullDatePattern));
				}
			}

			// 女用户这里不获取奖励
			if (sex == 0){
				return new MessageUtil(1, "保存成功!");
			}

			// 获取当前性别是否能贡献师徒奖励
			qSql = "SELECT * FROM t_spread_award WHERE t_sex = ?";
			List<Map<String, Object>> querySqlList = this.getQuerySqlList(qSql, sex);

			if (!querySqlList.isEmpty()) {
				querySqlList.forEach(s -> {
					if (s.get("t_rank").toString().equals("1")) {
						// 获取用户的直接推广人
						Map<String, Object> map = this.getMap("SELECT t_referee FROM t_user WHERE t_id = ?", userId);
						if (null != map.get("t_referee") && 0 != Integer.parseInt(map.get("t_referee").toString())) {
							// 插入订单记录
							int orderId = saveOrder(0, Integer.parseInt(map.get("t_referee").toString()),
									Integer.parseInt(s.get("t_id").toString()),
									WalletDetail.CHANGE_CATEGOR_RECOMMENDATION,
									new BigDecimal(s.get("t_gold").toString()));

							List<Map<String, Object>> userData = getQuerySqlList(
									"SELECT t_nickName FROM t_user WHERE t_id = ?", userId);
							// 给用户发送红包
							if (!userData.isEmpty()) {
								// 写入红包记录
								rebateRedPacket(userId, Integer.parseInt(map.get("t_referee").toString()),
										"来自于" + userData.get(0).get("t_nickName") + "师徒奖励红包:",
										new BigDecimal(s.get("t_gold").toString()), 1, orderId);

								HttpUtil.httpClentJson(
										SystemConfig.getValue("chat_mina_url") + "sendRedPackegNotice.do",
										"userId=" + map.get("t_referee"));

							}
						}
					} else if (s.get("t_rank").toString().equals("2")) {
						// 获取用户的直接推广人
						List<Map<String, Object>> sqlList = this.getQuerySqlList(
								"SELECT u.t_referee FROM t_user u LEFT JOIN t_user ul  ON u.t_id = ul.t_referee WHERE ul.t_id = ? ",
								userId);
						if (!sqlList.isEmpty() && !sqlList.get(0).get("t_referee").toString().equals("0")) {

							List<Map<String, Object>> userData = getQuerySqlList(
									"SELECT t_nickName FROM t_user WHERE t_id = ?", userId);
							// 给用户发送红包
							if (!userData.isEmpty()) {
								int orderId = saveOrder(0, Integer.parseInt(sqlList.get(0).get("t_referee").toString()),
										Integer.parseInt(s.get("t_id").toString()),
										WalletDetail.CHANGE_CATEGOR_RECOMMENDATION,
										new BigDecimal(s.get("t_gold").toString()));
								// 写入红包记录
								rebateRedPacket(userId, Integer.parseInt(sqlList.get(0).get("t_referee").toString()),
										"来自于" + userData.get(0).get("t_nickName") + "师徒奖励红包:",
										new BigDecimal(s.get("t_gold").toString()), 1, orderId);
								// 调用红包推送
								HttpUtil.httpClentJson(
										SystemConfig.getValue("chat_mina_url") + "sendRedPackegNotice.do",
										"userId=" + sqlList.get(0).get("t_referee"));

							}
						}
					}
				});

			}
			// 加入到缓存
			mu = new MessageUtil(1, "保存成功!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("修改性别异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 存储订单记录
	 * 
//	 * @param t_id          订单号
	 * @param consume 消费者
	 * @param cover_consume 被消费者
	 * @param consume_score 消费资源数据编号
	 * @param consume_type  消费类型
	 * @param amount        消费金额
	 */
	private int saveOrder(int consume, int cover_consume, int consume_score, int consume_type, BigDecimal amount) {

		String sql = "INSERT INTO t_order (t_consume, t_cover_consume, t_consume_type, t_consume_score, t_amount, t_create_time) VALUES (?, ?, ?, ?, ?, ?)";

		return this.getFinalDao().getIEntitySQLDAO().saveData(sql, consume, cover_consume, consume_type, consume_score,
				amount, DateUtils.format(new Date(), DateUtils.FullDatePattern));

	}

	/**
	 * 获取订单Id
	 * 
	 * @return
	 */
	public int getOrderId() {
		List<Map<String, Object>> arr = this.getQuerySqlList("SELECT t_id FROM t_order ORDER BY t_id DESC LIMIT 1;");
		return arr.isEmpty() ? 1 : (Integer) arr.get(0).get("t_id") + 1;
	}

	/*
	 * 获取登陆设置列表(non-Javadoc)
	 * 
	 * @see com.yiliao.service.LoginService#getLongSetUpList()
	 */
	@Override
	public MessageUtil getLongSetUpList() {
		MessageUtil mu = null;

		try {

			String sql = "SELECT t_app_id,t_app_secret,t_type FROM t_login_setup WHERE t_state = 0 ";

			List<Map<String, Object>> setUpList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(setUpList);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取登陆设置列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}

		return mu;
	}

	/**
	 * qq登陆
	 */
	@Override
	public MessageUtil qqLogin(String openId, String nickName, String handImg, String city, String t_phone_type,
			String t_system_version, String t_ip_address, String deviceNumber, int shareUserId) {
		MessageUtil mu = null;
		try {

			Map<String, Object> user_data = getUserIsExist(null, openId, null);
			String t_token = UUID.randomUUID().toString().replaceAll("-", "");
			logger.info("查询出来的对象-->{}", user_data.toString());
			// 判断用户是否存在 如果存在查询用户余额信息
			if (null != user_data && !user_data.isEmpty() && null != user_data.get("t_id")) {

				int user_id = Integer.parseInt(user_data.get("t_id").toString());

				// 表示用户已被封号或者永久禁用
				if (!"0".equals(user_data.get("t_disable").toString())) {
					return getUserDesTime(Integer.parseInt(user_data.get("t_disable").toString()), user_id);
				}
				user_data.put("t_token", t_token);
				// 修改用户为空状态
				upateLoginState(user_id,user_data.get("t_disable").toString(), t_token);
				// 设置用户余额
				user_data.put("gold",
						new BigDecimal(null == user_data.get("amount") ? "0" : user_data.get("amount").toString()));

				if ("iPhone".equals(t_phone_type)) {
					this.executeSQL("UPDATE t_user SET t_online_setup = 1,t_onLine = 0 WHERE t_id = ? ", user_id);
				}

				mu = new MessageUtil();
				mu.setM_istatus(1);
				mu.setM_strMessage("登陆成功!");
				mu.setM_object(user_data);

			} else { // 不存在 新增用户信息
				// 判断当前设备是否已经注册
				if (!StringUtils.isNotBlank(deviceNumber) || !getDeviceIsInstal(deviceNumber, t_ip_address)) {
					//return new MessageUtil(-200, "该设备已经注册过账号了!");
				}

				user_data = saveUser(nickName, null, null, handImg, null, null, city, null, null, openId, t_phone_type,
						t_system_version, t_ip_address, deviceNumber, null, shareUserId, t_token);
				user_data.put("t_token", t_token);
				mu = new MessageUtil();
				mu.setM_istatus(1);
				mu.setM_strMessage("注册成功~!");
				mu.setM_object(user_data);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("QQ登陆异常!", e);
			mu = new MessageUtil(0, "登录异常!");
		}
		return mu;
	}

	/**
	 * 修改用户为空闲状态
	 * 
	 * @param userId
	 */
	private void upateLoginState(int userId,String t_disable, String t_token) {
		// 修改用户表的状态
		String uSql = "UPDATE t_user SET  t_onLine=? ,t_token=? WHERE t_id=?;";
		this.getFinalDao().getIEntitySQLDAO().executeSQL(uSql, User.ONLINE_IDLE, t_token, userId);
		redisUtil.hmset("token_" + userId, new HashMap<String, Object>(){
			private static final long serialVersionUID = 1L;
		{
			put("t_disable", t_disable);
			put("t_token", t_token);
		}},1000*60l);
	}

	/*
	 * 更新手机号(non-Javadoc)
	 * 
	 * @see com.yiliao.service.LoginService#updatePhone(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public MessageUtil updatePhone(Integer userId, String phone, String smsCode) {
		MessageUtil mu = null;
		try {

			SmsDescribe sd = (SmsDescribe) JSONObject
					.toBean(JSONObject.fromObject(redisUtil.getKeyObject("verify_" + phone)), SmsDescribe.class);
			// 判断验证码是否正确
			if (null == sd || !sd.getSmsCode().equals(smsCode)) {
				return new MessageUtil(0, "账号或者验证码错误!");
			}
			// 更新手机号
			String sql = "UPDATE t_user SET t_phone = ? WHERE t_id = ?";

			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, phone, userId);

			mu = new MessageUtil(1, "更新成功!");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mu;
	}

	/*
	 * 用户登登出(non-Javadoc)
	 * 
	 * @see com.yiliao.service.LoginService#logout(int)
	 */
	@Override
	public MessageUtil logout(int userId) {
		MessageUtil mu = null;
		try {

			String uSql = "UPDATE t_user SET  t_onLine = ?  WHERE t_id=?;";
			this.getFinalDao().getIEntitySQLDAO().executeSQL(uSql, User.ONLINE_OFF_LINE, userId);
			// 修改主播表
			this.executeSQL("UPDATE t_home_table SET  t_onLine = ? WHERE t_id = ?;", User.ONLINE_OFF_LINE, userId);

			// 调用mina服务器方法
			HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "userHangupLink.do", "userId=" + userId);

			mu = new MessageUtil(1, "登出成功!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("登出异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 账号密码登陆
	 */
	@Override
	public MessageUtil userLogin(String phone, String password) {
		MessageUtil mu = null;
		try {
			String sql = " SELECT u.t_id,u.t_nickName,u.t_handImg,u.t_is_vip,u.t_role,u.t_sex,u.t_disable,SUM(b.t_profit_money+b.t_recharge_money+b.t_share_money) AS amount,u.t_is_svip  FROM t_user u LEFT JOIN t_balance  b ON b.t_user_id = u.t_id  WHERE u.t_phone = ? AND u.t_pass_wrod = ? ";
			List<Map<String, Object>> user = this.getQuerySqlList(sql, phone, Md5Util.encodeByMD5(password));
			String t_token = UUID.randomUUID().toString().replaceAll("-", "");
			// 已经注册过用户了
			if (null != user && !user.isEmpty() && null != user.get(0).get("t_disable")) {
				if (!"0".equals(user.get(0).get("t_disable").toString())) {
					return getUserDesTime(Integer.parseInt(user.get(0).get("t_disable").toString()),
							Integer.parseInt(user.get(0).get("t_id").toString()));
				}

				// 修改用户为空状态
				upateLoginState(Integer.parseInt(user.get(0).get("t_id").toString()),user.get(0).get("t_disable").toString(), t_token);

				// 查询出用户的可用余额
				sql = "SELECT SUM(b.t_profit_money+b.t_recharge_money+b.t_share_money) AS amount FROM t_balance  b WHERE b.t_user_id = ?";

				Map<String, Object> money = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql,
						user.get(0).get("t_id"));

				user.get(0).put("gold", new BigDecimal(money.get("amount").toString()).intValue());
				user.get(0).put("t_token", t_token);
				mu = new MessageUtil();
				mu.setM_istatus(1);
				mu.setM_object(user.get(0));
			} else {
				mu = new MessageUtil(-1, "用户名或密码错误");
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("账号密码登陆异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 拉取新用户推送消息
	 */
	@Override
	public MessageUtil getPushMsg(int userId) {
		MessageUtil mu = null;
		try {

			// 获取用户的性别
			Map<String, Object> userMap = this.getMap("SELECT t_sex FROM t_user WHERE t_id = ?", userId);

			String qSql = "SELECT t_system_lang_girl,t_system_lang_male FROM t_system_setup";
			Map<String, Object> map = this.getMap(qSql);

			if ("0".equals(userMap.get("t_sex").toString())) {
				// 异步通知
				this.applicationContext.publishEvent(new PushMesgEvnet(
						new MessageEntity(userId, map.get("t_system_lang_girl").toString(), 0, new Date())));
			} else {
				// 异步通知
				this.applicationContext.publishEvent(new PushMesgEvnet(
						new MessageEntity(userId, map.get("t_system_lang_male").toString(), 0, new Date())));
			}
			mu = new MessageUtil(1, "已发送.");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return mu;
	}

	/**
	 * 获取最新版本
	 */
	@Override
	public MessageUtil getNewVersion() {
		MessageUtil mu = null;
		try {

			String sql = "SELECT t_download_url,t_version,t_version_depict FROM t_version WHERE t_is_new = 1 AND t_version_type = 'android' ";

			List<Map<String, Object>> verData = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(!verData.isEmpty() ? verData.get(0) : null);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取最新版本异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 更新最后登陆时间
	 */
	@Override
	public void upLoginTime(int userId) {
		try {

			String uSql = " UPDATE t_user SET t_login_time = ? WHERE t_id = ? ";
			this.getFinalDao().getIEntitySQLDAO().executeSQL(uSql,
					DateUtils.format(new Date(), DateUtils.FullDatePattern), userId);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("更新登陆时间异常!", e);
		}
	}

	/**
	 * socket断线
	 */
	public void socketBreak(int userId) {

		logger.info("当前用户已下线-->{}", userId);

		// 获取当前用户是否存在永久在线权限 如果存在
		List<Map<String, Object>> userList = this
				.getQuerySqlList("SELECT * FROM t_user WHERE t_online_setup = 1 AND t_id = ? ", userId);

		if (userList.isEmpty()) {

			String uSql = "UPDATE t_user SET  t_onLine=?  WHERE t_id=?;";
			this.executeSQL(uSql, User.ONLINE_OFF_LINE, userId);

			// 获取当前人是否是主播
			List<Map<String, Object>> userDatas = this
					.getQuerySqlList("SELECT * FROM t_user WHERE t_id = ? AND t_role = 1 ", userId);
			if (null != userDatas && !userDatas.isEmpty()) {
				// 修改home_table 表中在线状态为忙碌
				this.executeSQL("UPDATE t_home_table SET t_onLine = ? WHERE t_id = ? ", User.ONLINE_OFF_LINE, userId);
			}
			List<Map<String, Object>> sqlList = this.getQuerySqlList(
					"SELECT t_id,t_login_time FROM t_log_time WHERE t_user_id = ? AND t_logout_time IS NULL ;", userId);

			if (!sqlList.isEmpty()) {
				try {

					long logoutTime = System.currentTimeMillis();

					long time = logoutTime - DateUtils
							.parse(sqlList.get(0).get("t_login_time").toString(), DateUtils.FullDatePattern).getTime();

					this.executeSQL("UPDATE t_log_time SET t_logout_time = ?,t_duration = ? WHERE t_id = ? ;",
							DateUtils.format(logoutTime, DateUtils.FullDatePattern), (time / 1000),
							sqlList.get(0).get("t_id"));
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}

		}
		// 结束速配
//		sp.endSpeedDating(userId);
		// 清理缓存

//		RoomTimer.getUserIdReturnRoomId(userId).forEach(s -> {
//			this.videoChatService.breakLink(s, 4);
//		});
	}

	/*
	 * 修改启动状态 (non-Javadoc)
	 * 
	 * @see com.yiliao.service.LoginService#startUpOnLine()
	 */
	@Override
	public void startUpOnLine() {
		String uSql = " UPDATE t_user SET t_onLine = 1 WHERE t_id NOT IN (SELECT t_user_id FROM t_virtual) ";
		this.getFinalDao().getIEntitySQLDAO().executeSQL(uSql);
		// 修改home_table 表中在线状态为忙碌
		this.executeSQL("UPDATE t_home_table SET t_onLine = 2 WHERE t_id NOT IN (SELECT t_user_id FROM t_virtual) ");
	}

	/*
	 * 获取用户是否已经被封号(non-Javadoc)
	 * 
	 * @see com.yiliao.service.LoginService#getUserIsDisable(int)
	 */
	@Override
	public Map<String, Object> getUserIsDisable(int userId) {
		try {
			// 用户未被封号返回false 否则返回true
			logger.info("当前查询用户编号-->{}", userId);

			String qSql = " SELECT t_disable , ifnull(t_token,'') t_token FROM  t_user WHERE t_id = ?  ";

			List<Map<String, Object>> sqlList = this.getQuerySqlList(qSql, userId);
			if (sqlList.isEmpty()) {
				return null;
			} else {
				return sqlList.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	@Override
	public Map<String, Object> getUserVerifyToken(int tokenId, String token) {
		try {

			return new HashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					 Map<String, Object> userVerify = redisUtil.hmget("token_" + tokenId);
					// 如果缓存中存在数据 且token和APP端上传的不一致
					// 返回 -1
					if (null != userVerify  && !userVerify.isEmpty()) {
						if (!userVerify.get("t_token").toString().equals(token))
							put("token", -1);
						else 
							put("token", 1);
						
						//验证用户是否被封号
						int disable = Integer.valueOf(userVerify.get("t_disable").toString());
                        
						if(disable == 0 ) 
							put("disable", 1);
						else 
							put("disable", -1);
						
					} else {
						FutureTask<Map<String, Object>> task = new FutureTask<>(new Callable<Map<String, Object>>() {
							@Override
							public Map<String, Object> call() throws Exception {
								
								return getMap(" SELECT t_disable , ifnull(t_token,'') t_token FROM  t_user WHERE t_id = ? ", tokenId);
							}
						});
						pool.submit(task);
						Map<String, Object> map = task.get();
						//验证token
						if (!map.get("t_token").toString().equals(token))
							put("token", -1);
						else 
							put("token", 1);
						
						//验证用户是否被封号
						int disable = Integer.valueOf(map.get("t_disable").toString());
                        
						if(disable == 0 ) 
							put("disable", 1);
						else 
							put("disable", -1);
						map.put("t_disable", map.get("t_disable").toString());
						//写入到缓存中
						redisUtil.hmset("token_" + tokenId, map,1000*60l);
					}
				}
			};
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new HashMap<String, Object>();
	}

	/**
	 * 获取ios最新版本
	 */
	@Override
	public MessageUtil getIosVersion() {
		MessageUtil mu = null;
		try {

			String sql = "SELECT t_download_url,t_version,t_version_depict FROM t_version WHERE t_is_new = 1 AND t_version_type = 'ios' ";

			List<Map<String, Object>> verData = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(!verData.isEmpty() ? verData.get(0) : null);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取最新版本异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 手动绑定推广人
	 */
	@Override
	public MessageUtil uniteIdCard(int userId, int idCard) {
		try {
			// 获取用户是否已经有推广人了
			List<Map<String, Object>> users = this
					.getQuerySqlList("SELECT t_id,t_referee,t_sex FROM t_user WHERE t_id = ? ", userId);
			// 获取推广人是否存在
			List<Map<String, Object>> reUsers = this.getQuerySqlList("SELECT t_id FROM t_user WHERE t_id = ?",
					(idCard - 10000));

			if (userId == (idCard - 10000)) {
				return new MessageUtil(-1, "推广人不能是自己");
			} else if (reUsers.isEmpty()) {
				return new MessageUtil(-2, "当前推广人不存在!");
			} else if (null != users.get(0).get("t_referee")
					&& 0 < Integer.parseInt(users.get(0).get("t_referee").toString())) {
				return new MessageUtil(-3, "当前用户已绑定推广人.");
			} else {

				// 开始绑定推广人
				this.executeSQL("UPDATE t_user SET t_referee = ? WHERE t_id = ?", (idCard - 10000), userId);

				// 获取当前性别是否能贡献师徒奖励
				List<Map<String, Object>> querySqlList = this
						.getQuerySqlList("SELECT * FROM t_spread_award WHERE t_sex = ?", users.get(0).get("t_sex"));

				if (!querySqlList.isEmpty()) {
					querySqlList.forEach(s -> {
						if (s.get("t_rank").toString().equals("1")) {
							// 获取用户的直接推广人
							Map<String, Object> map = this.getMap("SELECT t_referee FROM t_user WHERE t_id = ?",
									userId);
							if (null != map.get("t_referee")
									&& 0 != Integer.parseInt(map.get("t_referee").toString())) {
								// 插入订单记录
								int orderId = saveOrder(0, Integer.parseInt(map.get("t_referee").toString()),
										Integer.parseInt(s.get("t_id").toString()),
										WalletDetail.CHANGE_CATEGOR_RECOMMENDATION,
										new BigDecimal(s.get("t_gold").toString()));

								List<Map<String, Object>> userData = getQuerySqlList(
										"SELECT t_nickName FROM t_user WHERE t_id = ?", userId);
								// 给用户发送红包
								if (!userData.isEmpty()) {
									rebateRedPacket(userId, Integer.parseInt(map.get("t_referee").toString()),
											"来自于" + userData.get(0).get("t_nickName") + "师徒奖励红包:",
											new BigDecimal(s.get("t_gold").toString()), 1, orderId);

									HttpUtil.httpClentJson(
											SystemConfig.getValue("chat_mina_url") + "sendRedPackegNotice.do",
											"userId=" + map.get("t_referee"));

								}
							}
						} else if (s.get("t_rank").toString().equals("2")) {
							// 获取用户的间接推广人
							List<Map<String, Object>> sqlList = this.getQuerySqlList(
									"SELECT u.t_referee FROM t_user u LEFT JOIN t_user ul  ON u.t_id = ul.t_referee WHERE ul.t_id = ? ",
									userId);
							if (!sqlList.isEmpty() && !sqlList.get(0).get("t_referee").toString().equals("0")) {

								List<Map<String, Object>> userData = getQuerySqlList(
										"SELECT t_nickName FROM t_user WHERE t_id = ?", userId);
								// 给用户发送红包
								if (!userData.isEmpty()) {
									// 插入订单记录
									int orderId = saveOrder(0,
											Integer.parseInt(sqlList.get(0).get("t_referee").toString()),
											Integer.parseInt(s.get("t_id").toString()),
											WalletDetail.CHANGE_CATEGOR_RECOMMENDATION,
											new BigDecimal(s.get("t_gold").toString()));
									rebateRedPacket(userId,
											Integer.parseInt(sqlList.get(0).get("t_referee").toString()),
											"来自于" + userData.get(0).get("t_nickName") + "师徒奖励红包:",
											new BigDecimal(s.get("t_gold").toString()), 1, orderId);

									HttpUtil.httpClentJson(
											SystemConfig.getValue("chat_mina_url") + "sendRedPackegNotice.do",
											"userId=" + sqlList.get(0).get("t_referee"));

								}
							}
						}
					});
				}

				return new MessageUtil(1, "绑定成功！");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new MessageUtil(0, "程序异常!");
		}
	}

	/**
	 * 存储红包记录
	 * 
	 * @param t_hair_userId       贡献者
	 * @param t_receive_userId    接收人
	 * @param t_redpacket_content 提示内容
	 * @param t_redpacket_gold    金币
	 * @param t_redpacket_type    红包类型 0.赠送红包 1.贡献红包
	 */
	private void rebateRedPacket(int t_hair_userId, int t_receive_userId, String t_redpacket_content,
			BigDecimal t_redpacket_gold, int t_redpacket_type, int orderId) {
		String sql = "INSERT INTO t_redpacket_log (t_hair_userId, t_receive_userId, t_redpacket_content, t_redpacket_gold, t_redpacket_draw, t_redpacket_type, t_create_time,t_order_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

		this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, t_hair_userId, t_receive_userId, t_redpacket_content,
				t_redpacket_gold, 0, t_redpacket_type, DateUtils.format(new Date(), DateUtils.FullDatePattern),
				orderId);
	}

	/**
	 * 发送模拟呼叫用户
	 */
	@Override
	public void sendSimulationVideo(int userId) {

		MessageUtil mu = new MessageUtil();
		try {
			// 获取视频呼叫最低标准
			Map<String, Object> map = getMap("SELECT t_extract_ratio FROM t_extract WHERE t_project_type = 5");

			String[] str = map.get("t_extract_ratio").toString().split(",");

			// 获取用户的余额
//			MessageUtil mu= (MessageUtil) GoogleCacheUtil.userCache.get(userId);

			JSONObject jsonObject = JSONObject.fromObject(mu.getM_object());
			// 如果用户的余额小于最低视频呼叫标准
			// 那么加入到模拟呼叫系统中
			if (jsonObject.getInt("amount") < Integer.parseInt(str[0])) {

				String sql = "SELECT t_user_id FROM t_virtual v LEFT JOIN t_user u ON v.t_user_id = u.t_id  WHERE u.t_disable = 0 ORDER BY rand() LIMIT 3;";
				List<Map<String, Object>> anchors = this.getQuerySqlList(sql);
				List<Integer> arr = new ArrayList<>();

				anchors.forEach(s -> {
					arr.add(Integer.parseInt(s.get("t_user_id").toString()));
				});

				HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "getSpeedDatingRoom.do",
						"userId=" + userId + "&anchor=" + arr + "&callCount=0");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据手机号或者QQopenId或者微信openId查询用户是否存在
	 * 
	 * @param phone      手机号
	 * @param qq_open_id qq
	 * @param wx_open_id 微信
	 * @return
	 */
	private Map<String, Object> getUserIsExist(String phone, String qq_open_id, String wx_open_id) {

		StringBuffer sql = new StringBuffer();
		sql.append(
				" SELECT u.t_phone,u.t_id,u.t_nickName,u.t_handImg,u.t_is_vip,u.t_role,u.t_sex,u.t_disable,SUM(b.t_profit_money+b.t_recharge_money+b.t_share_money) AS amount,u.t_is_svip ");
		sql.append(" FROM t_user u LEFT JOIN t_balance  b ON b.t_user_id = u.t_id  ");
		// 判断
		if (StringUtils.isNotBlank(phone)) {
			sql.append("WHERE t_phone = ? ");
		} else if (StringUtils.isNotBlank(qq_open_id)) {
			sql.append("WHERE t_qq_open_id = ? ");
		} else if (StringUtils.isNotBlank(wx_open_id)) {
			sql.append("WHERE t_open_id = ? ");
		} else {
			return new HashMap<>();
		}

		List<Map<String, Object>> user_data = this.getQuerySqlList(sql.toString(),
				StringUtils.isNotBlank(phone) ? phone : StringUtils.isNotBlank(qq_open_id) ? qq_open_id : wx_open_id);

		if (user_data.isEmpty() || null == user_data.get(0).get("t_id")) {
			return new HashMap<>();
		}
		return user_data.get(0);
	}

	/**
	 * 获取用户封号状态
	 * 
	 * @param disable_state 封号状态
	 * @param user_id       用户编号
	 */
	private MessageUtil getUserDesTime(int disable_state, int user_id) {
		MessageUtil mu = null;

		if (1 == disable_state) {
			// 获取封号到期时间
			String benSql = "SELECT DATE_FORMAT(t_end_time,'%Y-%m-%d %T') AS endTime FROM t_disable WHERE t_user_id = ? ORDER BY t_id DESC LIMIT 0,1";

			List<Map<String, Object>> banTime = this.getQuerySqlList(benSql, user_id);

			mu = new MessageUtil();
			mu.setM_istatus(-1);
			mu.setM_strMessage("很抱歉!您因涉嫌违规被封号,封号到期时间为:" + banTime.get(0).get("endTime"));
		} else {
			mu = new MessageUtil();
			mu.setM_istatus(-1);
			mu.setM_strMessage("很抱歉!您因涉嫌违规被永久封号,如有异议请联系平台.");

		}
		return mu;
	}

	/**
	 * 注册用户
	 */
	@Override
	public MessageUtil register(String phone, String password, String smsCode, String t_phone_type,
			String t_system_version, String t_ip_address, String deviceNumber, int shareUserId) {
		try {
			// 判断验证码是否正确
			SmsDescribe sd = (SmsDescribe) JSONObject
					.toBean(JSONObject.fromObject(redisUtil.getKeyObject("verify_" + phone)), SmsDescribe.class);
			// 判断验证码是否正确
			if (null == sd || !sd.getSmsCode().equals(smsCode)) {
				return new MessageUtil(-2, "账号或者验证码错误!");
			}
			// 获取当前手机号是否已经被注册
			List<Map<String, Object>> sqlList = this.getQuerySqlList("SELECT * FROM t_user WHERE t_phone  = ? ", phone);

			if (!sqlList.isEmpty()) {
				return new MessageUtil(-1, "手机号已存在!");
			}
			String t_token = UUID.randomUUID().toString().replaceAll("-", "");
			// 注册用户
			saveUser("", null, 18, "", phone, "", "", "", "", "", t_phone_type, t_system_version, t_ip_address,
					deviceNumber, password, shareUserId, t_token);

			return new MessageUtil(1, "注册成功!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}注册账号异常!", phone, e);
			return new MessageUtil(0, "程序异常!");
		}
	}

	@Override
	public MessageUtil upPassword(String phone, String password, String smsCode) {
		try {
			// 验证
			// 判断验证码是否正确
			SmsDescribe sd = (SmsDescribe) JSONObject
					.toBean(JSONObject.fromObject(redisUtil.getKeyObject("verify_" + phone)), SmsDescribe.class);
			// 判断验证码是否正确
			if (null == sd || !sd.getSmsCode().equals(smsCode)) {
				return new MessageUtil(-2, "验证码错误!");
			}
			// 修改该手机号下的登陆密码
			int executeSQL = this.executeSQL("UPDATE t_user SET t_pass_wrod = ? WHERE t_phone  = ? ;",
					Md5Util.encodeByMD5(password), phone);
			// 重新调用登陆
			if (executeSQL > 0) {
				return new MessageUtil(executeSQL > 0 ? 1 : -1, "重置密码成功!");
			}
			return new MessageUtil(-1, "账号不存在!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}修改密码异常!", phone, e);
			return new MessageUtil(0, "程序异常!");
		}
	}

	/**
	 * 非vip首次登陆获取私信数
	 * @param userId 登陆人id
	 * @return
	 */
	@Override
	public MessageUtil privateChatNumber(int userId){

		Map<String, Object> obj = new HashMap<>();
		obj.put("isCase",false);
		//判断是否是会员
		List<Map<String, Object>> maps = this.getQuerySqlList("SELECT t_is_vip FROM t_user WHERE t_id = ?",userId);
		if ("1".equals(maps.get(0).get("t_is_vip").toString())){
			// 不是会员
			if (!redisUtil.exists("private_chat_number_"+userId)){
				// 查询配置表获取私信数量
				String qSql = " SELECT t_private_letter_number FROM t_system_setup ";
				maps = this.getQuerySqlList(qSql);
				if (maps.size()>0){
					redisUtil.set("private_chat_number_"+userId,maps.get(0).get("t_private_letter_number").toString(),DateUtils.millis(1));
					obj.put("isCase",true);
					obj.put("number",maps.get(0).get("t_private_letter_number"));
				}
			}
		}
		return new MessageUtil(1,obj);
	}

	/**
	 * 打招呼
	 * @param userId 打招呼人id
	 * @param anchorUserId 被打招呼
	 * @return
	 */
	@Override
	public MessageUtil greet(int userId,int anchorUserId){
		MessageUtil mu = new MessageUtil();
		if (redisUtil.get(userId+"user_greet_"+anchorUserId) == null){
			redisUtil.set(userId+"user_greet_"+anchorUserId,"true",DateUtils.millis(1));
			mu.setM_istatus(1);
			mu.setM_strMessage("成功打招呼");
			mu.setM_object(true);
			return mu;
		}else{
			mu.setM_istatus(0);
			mu.setM_strMessage("一天只能打一次招呼,今天你已经打过招呼了");
			mu.setM_object(false);
			return mu;
		}
	}

}
