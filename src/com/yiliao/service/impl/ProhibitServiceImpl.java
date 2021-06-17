package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yiliao.domain.MessageEntity;
import com.yiliao.evnet.PushMesgEvnet;
import com.yiliao.service.ProhibitService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.RedisUtil;
import com.yiliao.util.SystemConfig;

import net.sf.json.JSONObject;

@Service("prohibitService")
public class ProhibitServiceImpl extends ICommServiceImpl implements ProhibitService {
	
	@Autowired
	RedisUtil redisUtil;
	
	@Override
	public void handleGetOutOfLine(JSONObject json) {
		try {
			// 七牛鉴黄
			String stream = json.getString("stream");

			if (StringUtils.isNotBlank(stream)) {
				String[] str = stream.split("/");
				logger.info("七牛回调{}在{}房间违规", str[0], str[1]);
				if ("porn".equals(json.getString("label")) && new BigDecimal(json.getString("rate"))
						.setScale(1, BigDecimal.ROUND_DOWN).compareTo(BigDecimal.valueOf(0.9)) >= 0) {
					int userId = Integer.parseInt(str[0]);
					int roomId = Integer.parseInt(str[1]);

					HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "breakLink.do",
							"userId=0" + "&roomId=" + roomId);
					// 存储违规信息
					String inSql = " INSERT INTO t_yellowing_error (t_user_id, t_content,t_room_msg,t_create_time) VALUES (?, ?,?, ?) ";
					this.executeSQL(inSql, userId, json.toString(), roomId,
							DateUtils.format(new Date(), DateUtils.FullDatePattern));
					// 处理用户
					this.handleIllegalityUser(userId, json.getString("ts"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("违规处理异常", e);
		}

	}

	@Override
	public void handleIrregularitiesUser(int roomId, int userid, String videoUrl) {
		try {

			HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "breakLink.do",
					"userId=0" + "&roomId=" + roomId);

			// 调用用户的处理
			handleIllegalityUser(Integer.valueOf(userid), videoUrl);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("监管平台处理违规用户异常!", e);
		}
	}

	/***
	 * 处理用户违规
	 * 
	 * @param userId
	 */
	@Override
	public void handleIllegalityUser(Integer userId, String videoUrl) {
		try {

			// 获取当前用户已经封号次数
			List<Map<String, Object>> userDis = this
					.getQuerySqlList("SELECT count(t_id) AS total FROM t_disable WHERE t_user_id = ?", userId);

			int disCount = 0;
			// 得到用户封号次数
			if (userDis.isEmpty()) {
				disCount = 1;
			} else {
				disCount = Integer.parseInt(userDis.get(0).get("total").toString()) + 1;
			}
			// 获取封号设置
			List<Map<String, Object>> _bns = getQuerySqlList(
					"SELECT t_count,t_hours FROM t_banned_setup ORDER BY t_count ASC ");

			boolean isOk = false;

			for (Map<String, Object> m : _bns) {
				// 取得用户需要封号的时间
				if (disCount == Integer.parseInt(m.get("t_count").toString())) {
					// 设置封号时间
					Double bennedTime = Double.valueOf(m.get("t_hours").toString()) * 60;

					// 添加封号记录
					String sql = "INSERT INTO t_disable ( t_user_id, t_disable_time, t_start_time, t_end_time,t_state,t_operate_user,t_create_time,t_context) VALUES (?, ?, ?, ?,0,?,?,?);";

					this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, userId, bennedTime.intValue(),
							DateUtils.format(new Date(), DateUtils.FullDatePattern),
							DateUtils.format(DateUtils.afterMinute(DateUtils.nowCal(), bennedTime.intValue()),
									DateUtils.FullDatePattern),
							"APP鉴黄回调", DateUtils.format(new Date(), DateUtils.FullDatePattern),
							"http://yiliao39.1-liao.com/" + videoUrl);

					// 设置用户的状态
					String upSql = "UPDATE t_user SET t_disable = ? WHERE t_id = ? ";
					this.executeSQL(upSql, "-1".equals(m.get("t_hours").toString()) ? 2 : 1, userId);

					// 给用发送激光推送
					// 消息内容
					String message = "您因违反平台相关禁止内容规定."
							+ ("-1".equals(m.get("t_hours").toString()) ? "且违反次数较多,将被进行永久封号,如有异议请联系相关平台客服."
									: "本次将封号" + m.get("t_hours") + "小时");
					// 异步通知
					this.applicationContext
							.publishEvent(new PushMesgEvnet(new MessageEntity(userId, message, 0, new Date())));

					// socket推送
					HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "sendUserViolation.do",
							"userId=" + userId);

					isOk = true;
				}
			}

			// 没有处理到用户 表示用户封号次数太多没有识别 按照最严重的进行处理
			if (!isOk) {
				List<Map<String, Object>> _serious = getQuerySqlList(
						"SELECT t_count,t_hours FROM t_banned_setup ORDER BY t_count DESC LIMIT 1 ;");
				if (!_serious.isEmpty()) {
					// 设置用户的状态
					String upSql = "UPDATE t_user SET t_disable = ? WHERE t_id = ? ";
					this.executeSQL(upSql, "-1".equals(_serious.get(0).get("t_hours").toString()) ? 2 : 1, userId);

					// 给用发送激光推送
					// 消息内容
					String message = "您因违反平台相关禁止内容规定." + ("-1".equals(_serious.get(0).get("t_hours").toString())
							? "且违反次数较多,将被进行永久封号,如有异议请联系相关平台客服."
							: "本次将封号" + _serious.get(0).get("t_hours") + "小时");

					// 异步通知
					this.applicationContext
							.publishEvent(new PushMesgEvnet(new MessageEntity(userId, message, 0, new Date())));

					// socket推送
					HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "sendUserViolation.do",
							"userId=" + userId);
				}
			}
			
			//删除用户缓存
			redisUtil.remove("token_" + userId);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * IM信息处理 (non-Javadoc)
	 * 
	 * @see com.yiliao.service.ProhibitService#handleImCallBack(int, int,
	 * java.lang.String)
	 */
	@Override
	public void handleImCallBack(int sendUser, int acceptUser, String centent, String mesType) {

		try {
			
			String inSql = "INSERT INTO t_im_log (t_idcard, t_user_name, t_cover_idcard, t_cover_name, t_content, t_create_time, t_type) VALUES ( ?, ?, ?, ?, ?, ?,?);";
			
			this.executeSQL(inSql, sendUser,
					getMap("SELECT t_nickName FROM t_user WHERE t_idcard = ?", sendUser).get("t_nickName"),
					acceptUser,
					getMap("SELECT t_nickName FROM t_user WHERE t_idcard = ? ", acceptUser).get("t_nickName"),
					centent, DateUtils.format(new Date(), DateUtils.FullDatePattern),mesType);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("im回调异常", e);
		}

	}

}
