package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yiliao.domain.WalletDetail;
import com.yiliao.service.CallBackService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.SystemConfig;

/**
 * 回调查询服务层实现类
 * 
 * @author Administrator
 * 
 */

@Service("callBackService")
public class CallBackServiceImpl extends ICommServiceImpl implements
		CallBackService {

	/**
	 * 根据订单号查询订单
	 */
	@Override
	public Map<String, Object> getOrderByOrderNo(String orderNo) {

		try {

			String sql = "SELECT * FROM t_recharge WHERE t_order_no = ?";

			List<Map<String, Object>> datas = this.getQuerySqlList(sql, orderNo);

			if (null != datas && !datas.isEmpty()) {
				return datas.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("根据订单号获取订单信息异常!", e);
		}
		return null;
	}

	/**
	 * 支付成功!
	 * synchronized 同步  先进先完成
	 */
	@Override
	public synchronized void  alipayPaymentComplete(String orderNo) {
		try {
			Map<String, Object> map = this.getOrderByOrderNo(orderNo);
			
			//更新订单状态
			String upSql = "UPDATE t_recharge SET  t_order_state=1,t_fulfil_time=? WHERE t_order_no = ?;";
			
			this.executeSQL(upSql, DateUtils.format(new Date(), DateUtils.FullDatePattern),orderNo);
			
			//判断用户是充值金币还是支付VIP金额
			//0.VIP充值 1.金币充值
			if(0 == Integer.parseInt(map.get("t_recharge_type").toString())){
				//处理用户VIP
				handleVip(Integer.parseInt(map.get("t_user_id").toString()),
						Integer.parseInt(map.get("t_setmeal_id").toString()));
			}else{
				//用户充值金币
				handleRecharge(Integer.parseInt(map.get("t_user_id").toString()),
						Integer.parseInt(map.get("t_setmeal_id").toString()),Integer.parseInt(map.get("t_id").toString()));
			}
		} catch (Exception e) {
			logger.error("{}订单处理异常!", orderNo,e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 处理VIP充值
	 * @param t_user_id 用户编号
	 * @param t_setmeal_id 套餐编号
	 */
	private void handleVip(int t_user_id,int t_setmeal_id){
		try {
			//获取用户信息
			String useSql = "SELECT t_is_vip FROM t_user WHERE t_id  = ?";
			Map<String, Object> userMap = this.getMap(useSql, t_user_id);
			
			//获取套餐信息
			String mealSql = "SELECT C  FROM t_vip_setmeal WHERE t_id = ?";
			Map<String, Object> vipMap = this.getMap(mealSql, t_setmeal_id);
			
			
			//判断当前用户是否是VIP
			//0.是 1.否
			if(1 == Integer.parseInt(userMap.get("t_is_vip").toString())){
				//获取用户是否开通过VIP
				String vipSql = "SELECT * FROM t_vip WHERE t_user_id =? ";
				List<Map<String, Object>> vipData = this.getQuerySqlList(vipSql, t_user_id);
				
				if(null == vipData || vipData.isEmpty()){
					//存储用户VIP时间信息
					String inseVipSql = "INSERT INTO t_vip (t_user_id, t_openUp_time, t_end_time) VALUES (?, ?, ?);";
					
					this.executeSQL(inseVipSql, t_user_id,
							DateUtils.format(new Date(), DateUtils.FullHourDatePattern),
							DateUtils.format(DateUtils.afterHours(DateUtils.afterMonths(
									Calendar.getInstance(),Integer.parseInt(vipMap.get("t_duration").toString())), 1),
									DateUtils.HDatePattern)
							);
				}else{ //用户开通过VIP 但是已经到期了
					String upVIpSql = "UPDATE t_vip SET  t_openUp_time=?, t_end_time=?  WHERE t_user_id= ?";
					this.executeSQL(upVIpSql,
							DateUtils.format(new Date(), DateUtils.FullHourDatePattern),
							DateUtils.format(DateUtils.afterHours(DateUtils.afterMonths(
									Calendar.getInstance(),Integer.parseInt(vipMap.get("t_duration").toString())), 1),
									DateUtils.HDatePattern));
				}
				//修改用户为VIP
				String upVipSql = "UPDATE t_user SET  t_is_vip = ? WHERE t_id = ? ";
				
				this.executeSQL(upVipSql, 0 , t_user_id);
				
			}else{ //用户是VIP 切还没到期
				
				
				//获取用户的VIP时间信息
				String vipSql = "SELECT DATE_FORMAT(t_end_time,'%Y-%m-%d %T') AS t_end_time  FROM t_vip  WHERE t_user_id =? ";
				Map<String, Object> vipData = this.getMap(vipSql, t_user_id);
				
				//设置到期时间
				Calendar cal = Calendar.getInstance();
				cal.setTime(DateUtils.parse(vipData.get("t_end_time").toString()));

				//更新用户VIP到期时间
				String upSql = "UPDATE t_vip SET t_end_time = ? WHERE t_user_id = ?";
				this.executeSQL(upSql,
						DateUtils.format(DateUtils.afterHours(DateUtils.afterMonths(
								cal,Integer.parseInt(vipMap.get("t_duration").toString())), 1),
								DateUtils.HDatePattern)
						);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("处理用户充值VIP异常!", e);
		}
	}
	
	/**
	 * 处理用户充值金币
	 * @param t_user_id
	 * @param t_setmeal_id
	 */
	private void handleRecharge(int t_user_id,int t_setmeal_id,Integer orderId){
		
		try {
			//获取用户选择的充值套餐
			String setmealSql = "SELECT t_gold from t_set_meal WHERE t_id = ? ";
			Map<String, Object> goldMap = this.getMap(setmealSql, t_setmeal_id);
			
			//给用户增加金币
			String upUSql = "UPDATE t_balance SET t_recharge_money = t_recharge_money + ? WHERE t_user_id = ?";
			this.executeSQL(upUSql,Integer.parseInt(goldMap.get("t_gold").toString()) ,t_user_id);
			
			//统计当前用户的所有余额
			String totalMoney="SELECT SUM(t_recharge_money+t_profit_money+t_share_money) AS totalGold FROM t_balance WHERE t_user_id = ?";
			
			Map<String, Object> total = this.getMap(totalMoney, t_user_id);
			
			BigDecimal totalGold = new BigDecimal(total.get("totalGold").toString());
			
			//调用方法
			HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url")+"addRoomRate.do", "userId=" + t_user_id);
			
		 
			//生成充值记录
			String inseSql = "INSERT INTO t_wallet_detail (t_user_id, t_change_type, t_change_category, t_change_front, t_value, t_change_after, t_change_time) VALUES (?, ?, ?, ?, ?, ?, ?);";
			
			this.executeSQL(inseSql, t_user_id,WalletDetail.CHANGE_TYPE_INCOME,
					WalletDetail.CHANGE_CATEGORY_RECHARGE,totalGold,new BigDecimal(goldMap.get("t_gold").toString()),
					totalGold.add(new BigDecimal(goldMap.get("t_gold").toString())),
					DateUtils.format(new Date(), DateUtils.FullDatePattern));
			//生产充值订单记录
			
			inseSql ="INSERT INTO t_order (t_cover_consume, t_consume_type, t_consume_score, t_amount, t_create_time) VALUES (?,?,?,?,?);";
			this.executeSQL(inseSql, t_user_id,WalletDetail.CHANGE_TYPE_INCOME,
					orderId,new BigDecimal(goldMap.get("t_gold").toString()),DateUtils.format(new Date(), DateUtils.FullDatePattern));
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("用户充值金币异常!", e);
		}
	}

	/**
	 * 验证支付金额是否正确
	 *//*
	@Override
	public Boolean payMoneyIdentical(String orderNo,String realprice,final int userId) {
		try {
			
			String qSql= " SELECT * FROM t_recharge WHERE t_order_no = ? AND t_recharge_money = ? ";
			
			List<Map<String, Object>> querySqlList = this.getQuerySqlList(qSql, orderNo,realprice);
			
			//支付金额不匹配或者订单号异常
			if(!querySqlList.isEmpty()) {
				//加入到消息通知列表中
				HtmlPayTimer.notify.put(orderNo, new HashMap<String,Object>() {{
					put("payState", 1);
					put("userId", userId);
					put("payTime", System.currentTimeMillis());
				}});
				return true;
			}
			
			logger.info("html支付验证失败-订单号{}，支付金额-{}",orderNo,realprice);
			//加入到消息列表通知中
			HtmlPayTimer.notify.put(orderNo,new HashMap<String,Object>() {{
				put("payState", 2);
				put("userId", userId);
				put("payTime", System.currentTimeMillis());
			}});
			
		} catch (Exception e) {
			logger.error("{}-订单验证支付金额必须完全一致!",orderNo,e);
		}
		return false;
	}

	@Override
	public MessageUtil getHtmlPayNotify(final int userId) {
		
		MessageUtil mu = null;
		try {
			
			List<Map<String, Object>> dataMap = new ArrayList<>();
			
			Map<String, Object> map = HtmlPayTimer.notify;
			for(Iterator<Map.Entry<String, Object>> it = map.entrySet().iterator(); it.hasNext();) {
				 final Map.Entry<String, Object> item = it.next();
				 final Map<String, Object> val = (Map<String, Object>) item.getValue();
				 
				 if(val.get("userId").equals(""+userId)) {
					 dataMap.add(new HashMap<String, Object>() {{
						 put("userId", userId);
						 put("orderNo",item.getKey());
						 put("payState", val.get("payState"));
						 put("payTime", DateUtils.format(Long.valueOf(val.get("payTime").toString()), DateUtils.FullDatePattern));
					 }});
				 }
			}
			
//			 dataMap.add(new HashMap<String, Object>() {{
//				 put("userId", userId);
//				 put("orderNo","13135434");
//				 put("payState", 1);
//				 put("payTime", "2018-10-18 11:53:59");
//			 }});
			
			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(dataMap);
		} catch (Exception e) {
			logger.error("{}获取订单支付结果异常!", userId,e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}*/

}
