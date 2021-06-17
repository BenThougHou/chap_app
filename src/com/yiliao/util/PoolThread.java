package com.yiliao.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yiliao.service.ICommService;

public class PoolThread extends Thread {

	Logger logger = LoggerFactory.getLogger(getClass());

	/** 注册人数 **/
	public static final int data_query_user = 1;
	/** 充值 **/
	public static final int data_query_recharges = 2;
	/** 视频聊天 **/
	public static final int data_query_video_chat = 3;
	/** 赠送礼物 **/
	public static final int data_query_gift = 4;
	/** 查看付费视频 **/
	public static final int data_query_pay_video = 5;
	/** 主播提现 **/
	public static final int data_query_extract = 6;

	/**
	 * 查询类型
	 */
	public int data_query;
	/**
	 * 查询条件
	 */
	public Object obj;

	public int userId;

	public PoolThread(int data_query, Object obj) {
		super();
		this.data_query = data_query;
		this.obj = obj;
	}

	public PoolThread(int data_query, Object obj, int userId) {
		super();
		this.data_query = data_query;
		this.obj = obj;
		this.userId = userId;
	}

	// 获取 videoChatService
	private static ICommService iCommServiceImpl = null;

	static {
		iCommServiceImpl = (ICommService) SpringConfig.getInstance().getBean("iCommServiceImpl");
	}

	@Override
	public void run() {
		// 查询当前日期是否存在数据
		List<Map<String, Object>> sqlList = iCommServiceImpl.getQuerySqlList(
				"SELECT t_id FROM t_financial_statements WHERE t_statistical_date = ? ",
				DateUtils.format(new Date(), DateUtils.defaultDatePattern));

		if (null == sqlList || sqlList.isEmpty()) {
			install(data_query, obj); // 新增数据
		} else { // 更新数据
			update(data_query, obj, Integer.parseInt(sqlList.get(0).get("t_id").toString()));
		}
	}

	/**
	 * 新增数据
	 * 
	 * @param data_query
	 * @param obj
	 */
	void install(int data_query, Object obj) {
		StringBuffer inst = new StringBuffer();
		StringBuffer vals = new StringBuffer();

		inst.append("INSERT INTO t_financial_statements (t_statistical_date");
		vals.append("VALUES ('").append(DateUtils.format(new Date(), DateUtils.defaultDatePattern)).append("'");

		switch (data_query) {
		case data_query_user: // 注册人数
			inst.append(",t_registered_user");
			vals.append(",").append(obj);
			break;
		case data_query_recharges: // 充值相关
			inst.append(",t_recharges_count");
			vals.append(",1");

			// 获取当前用户是否是第一次充值
			Map<String, Object> map = iCommServiceImpl.getMap(
					"SELECT COUNT(t_id) total FROM t_recharge WHERE t_order_state = 1 AND t_user_id = ?", userId);

			if (1 == Integer.valueOf(map.get("total").toString())) {
				inst.append(",t_one_recharges");
				vals.append(",1");
			}
			inst.append(",t_recharges_user");
			vals.append(",1");

			inst.append(",t_total_amount");
			vals.append(",").append(new BigDecimal(obj.toString()).setScale(2, BigDecimal.ROUND_DOWN));

			inst.append(",t_day_profit");
			vals.append(",").append(new BigDecimal(obj.toString()).setScale(2, BigDecimal.ROUND_DOWN));
			break;
		case data_query_video_chat: // 视频聊天相关
			// 根据订单编号获取视频聊天信息
			Map<String, Object> video = iCommServiceImpl.getMap("SELECT * FROM t_order WHERE t_id = ? ", obj);
			inst.append(",t_video_minute,t_video_gold");
			vals.append(",").append(video.get("t_log_time")).append(",").append(video.get("t_amount"));
			break;
		case data_query_gift: // 礼物相关
			// 根据编号得到赠送礼物需要多少金币
			Map<String, Object> gift = iCommServiceImpl.getMap("SELECT * FROM t_gift WHERE t_gift_id = ? ", obj);
			inst.append(",t_gift_quantity").append(",t_gift_consume");
			vals.append(",").append(1).append(",").append(gift.get("t_gift_gold"));
			break;
		case data_query_pay_video: // 付费视频相关
			// 获取视频信息
			Map<String, Object> pay_video = iCommServiceImpl.getMap("SELECT * FROM t_album WHERE t_id = ? ;", obj);

			inst.append(",t_pay_video,t_pay_video_gold");
			vals.append(",1").append(",").append(pay_video.get("t_money"));

			break;
		case data_query_extract: // 主播提现

			inst.append(",t_extract_amount");
			vals.append(",").append(new BigDecimal(obj.toString()).setScale(2, BigDecimal.ROUND_DOWN));

			inst.append(",t_day_profit");
			vals.append(",-")
					.append(new BigDecimal(obj.toString()).setScale(2, BigDecimal.ROUND_DOWN));
			break;
		}

		inst.append(") ");
		vals.append(")");

		String str = inst.append(vals).toString();

		logger.info("新增数据信息-->{}", str);
		// 写入数据
		iCommServiceImpl.executeSQL(str);
	}

	/**
	 * 修改数据
	 * 
	 * @param data_query
	 * @param obj
	 */
	void update(int data_query, Object obj, int dateId) {

		try {
			StringBuffer uSql = new StringBuffer();
			uSql.append("UPDATE t_financial_statements SET ");

			switch (data_query) {
			case data_query_user: // 注册人数
				uSql.append(" t_registered_user = t_registered_user+1  ");
				break;
			case data_query_recharges: // 充值相关
				uSql.append("t_recharges_count = t_recharges_count +1  "); // 充值次数+1
				uSql.append(",t_total_amount = t_total_amount + ")
						.append(new BigDecimal(obj.toString()).setScale(2, BigDecimal.ROUND_DOWN)); // 充值总金额
				uSql.append(",t_day_profit = t_day_profit + ")
						.append(new BigDecimal(obj.toString()).setScale(2, BigDecimal.ROUND_DOWN));
				
				// 获取用户今日是否已经充值过了
				Map<String, Object> regCount = iCommServiceImpl.getMap(
						"SELECT COUNT(t_id) total FROM t_recharge WHERE t_order_state = 1 AND t_user_id = ? AND t_fulfil_time BETWEEN  ? AND ?;",
						userId, DateUtils.format(new Date(), DateUtils.defaultDatePattern) + " 00:00:00",
						DateUtils.format(new Date(), DateUtils.defaultDatePattern) + " 23:59:59");

				// 当前用户第一次充值
				if (Integer.valueOf(regCount.get("total").toString()) == 1) {
					uSql.append(",t_recharges_user = t_recharges_user + 1"); // 充值人数+1
				}
				// 获取当前用户是否是第一次充值
				Map<String, Object> map = iCommServiceImpl.getMap(
						"SELECT COUNT(t_id) total FROM t_recharge WHERE t_order_state = 1 AND t_user_id = ? ", userId);
				if (1 == Integer.valueOf(map.get("total").toString())) {
					uSql.append(",t_one_recharges = t_one_recharges +1 ");
				}
				break;
			case data_query_video_chat: // 视频聊天相关
				// 根据订单编号获取视频聊天信息
				Map<String, Object> video = iCommServiceImpl.getMap("SELECT * FROM t_order WHERE t_id = ? ", obj);
				uSql.append("t_video_minute = t_video_minute + ").append(video.get("t_log_time"));
				uSql.append(",t_video_gold = t_video_gold + ").append(video.get("t_amount"));
				break;
			case data_query_gift: // 礼物相关
				// 根据编号得到赠送礼物需要多少金币
				Map<String, Object> gift = iCommServiceImpl.getMap("SELECT * FROM t_gift WHERE t_gift_id = ? ", obj);
				uSql.append("t_gift_quantity = t_gift_quantity +1 ");
				uSql.append(",t_gift_consume = t_gift_consume +").append(gift.get("t_gift_gold"));
				break;
			case data_query_pay_video: // 付费视频相关
				// 获取视频信息
				Map<String, Object> pay_video = iCommServiceImpl.getMap("SELECT * FROM t_album WHERE t_id = ? ;", obj);

				uSql.append("t_pay_video = t_pay_video +1 ");
				uSql.append(",t_pay_video_gold = t_pay_video_gold +").append(pay_video.get("t_money"));
				break;
			case data_query_extract: // 主播提现
				uSql.append("t_extract_amount = t_extract_amount + ")
						.append(new BigDecimal(obj.toString()).setScale(2, BigDecimal.ROUND_DOWN));
				uSql.append(",t_day_profit = t_day_profit - ")
						.append(new BigDecimal(obj.toString()).setScale(2, BigDecimal.ROUND_DOWN));
				break;
			}


			uSql.append("  WHERE t_id = ? ");

			logger.info("执行sql-》{}", uSql);
			
			iCommServiceImpl.executeSQL(uSql.toString(), dateId);

			logger.info("执行成功!");
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
