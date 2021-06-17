package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.yiliao.domain.WalletDetail;
import com.yiliao.service.MoneyService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.PoolThread;
import com.yiliao.util.cache.Cache;
import com.yiliao.util.cache.CacheRemove;

/**
 * 
 * @author Administrator 查询个人钱包
 */
@Service("moneyService")
public class MoneyServiceImpl extends ICommServiceImpl implements MoneyService {


	private Logger logger = LoggerFactory.getLogger(getClass());

	/*
	 * 查询个人钱包(non-Javadoc)
	 * 
	 * @see com.yiliao.service.MoneyService#getUserMoney(int)
	 */
	@Override
	public MessageUtil getUserMoney(int userId) {
		MessageUtil mu = new MessageUtil();
		try {
			String totalMoney = "SELECT SUM(b.t_profit_money+b.t_recharge_money+b.t_share_money) AS amount FROM  t_balance b  WHERE b.t_user_id = ?";
			// 查询用户的总金额
			Map<String, Object> totalMoneyMap = this.getFinalDao().getIEntitySQLDAO()
					.findBySQLUniqueResultToMap(totalMoney, userId);
			totalMoneyMap.put("amount", new BigDecimal(totalMoneyMap.get("amount").toString()).intValue());

			// 查询用户可提现金额
			String sql = "SELECT SUM(b.t_profit_money+b.t_share_money) AS putforward FROM  t_balance b  WHERE b.t_user_id = ?";
			Map<String, Object> putforwardMoney = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql,
					userId);

//			//得到金币 
			BigDecimal putforwardMon = new BigDecimal(
					null == putforwardMoney.get("putforward") ? "0" : putforwardMoney.get("putforward").toString());
			totalMoneyMap.put("putforward", putforwardMon.intValue());
			// 查询用户本月收入金币
			String monthSql = "SELECT SUM(t_value) as monthMoney FROM t_wallet_detail t WHERE t.t_user_id = ? AND t.t_change_type = 0 AND t.t_value>=1  AND t_change_time BETWEEN ? and ? ";
			Map<String, Object> monthMoney = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(monthSql,
					userId, DateUtils.getFirstDayOfMonth(0, 0), DateUtils.getLastDayOfMonth(0, 0));
			totalMoneyMap.put("monthMoney",
					monthMoney.isEmpty() ? 0
							: null == monthMoney.get("monthMoney") ? 0
									: new BigDecimal(monthMoney.get("monthMoney").toString()).intValue());

			// 查询用户本月支出金币
			String monthExpenditureSql = "SELECT SUM(t_value) as monthExpenditureMoney FROM t_wallet_detail t WHERE t.t_user_id = ? AND t.t_change_type = 1  AND t_change_time BETWEEN ? and ? ";
			Map<String, Object> monthExpenditureMoney = this.getFinalDao().getIEntitySQLDAO()
					.findBySQLUniqueResultToMap(monthExpenditureSql, userId, DateUtils.getFirstDayOfMonth(0, 0),
							DateUtils.getLastDayOfMonth(0, 0));
			totalMoneyMap.put("monthExpenditureMoney", monthExpenditureMoney.isEmpty() ? 0
					: null == monthExpenditureMoney.get("monthExpenditureMoney") ? 0
							: new BigDecimal(monthExpenditureMoney.get("monthExpenditureMoney").toString()).intValue());
			// 查询用户的充值明细
			String rechargeSql = "SELECT SUM(t_value) as rechargeMoney FROM t_wallet_detail t WHERE t.t_user_id = ? AND t.t_change_type = 0 AND t.t_change_category = 0 AND t_change_time BETWEEN ? and ? ";
			Map<String, Object> rechargeMoneyMap = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(
					rechargeSql, userId, DateUtils.getFirstDayOfMonth(0, 0), DateUtils.getLastDayOfMonth(0, 0));
			totalMoneyMap.put("rechargeMoney",
					rechargeMoneyMap.isEmpty() ? 0
							: null == rechargeMoneyMap.get("rechargeMoney") ? 0
									: new BigDecimal(rechargeMoneyMap.get("rechargeMoney").toString()).intValue());
			// 查询本月提现统计
			String withdrawCashSql = "SELECT SUM(t_value) as withdrawCash  FROM t_wallet_detail t WHERE t.t_user_id = ? AND t.t_change_type = 1 AND t.t_change_category = 10 AND t_change_time BETWEEN ? and ? ";
			Map<String, Object> withdrawCashMap = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(
					withdrawCashSql, userId, DateUtils.getFirstDayOfMonth(0, 0), DateUtils.getLastDayOfMonth(0, 0));
			totalMoneyMap.put("withdrawCashMap",
					withdrawCashMap.isEmpty() ? 0
							: null == withdrawCashMap.get("withdrawCash") ? 0
									: new BigDecimal(withdrawCashMap.get("withdrawCash").toString()).intValue());

			mu.setM_istatus(1);
			mu.setM_object(totalMoneyMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}查询钱包主页异常", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 查询用户个人余额 (non-Javadoc)
	 * 
	 * @see com.yiliao.service.app.MoneyService#getQueryUserBalance(int)
	 */
	@Override
	public MessageUtil getQueryUserBalance(int userId) {
		MessageUtil mu = new MessageUtil();
		try {
			String totalMoney = "SELECT SUM(b.t_profit_money+b.t_share_money) AS putAmount,SUM(b.t_profit_money+b.t_recharge_money+b.t_share_money) AS amount FROM  t_balance b  WHERE b.t_user_id = ?";
			// 查询用户的总金额
			Map<String, Object> totalMoneyMap = this.getFinalDao().getIEntitySQLDAO()
					.findBySQLUniqueResultToMap(totalMoney, userId);

			mu.setM_istatus(1);
			mu.setM_object(totalMoneyMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}查询余额异常!", userId, e);
			mu = new MessageUtil(0, "获取用户余额异常!");
		}
		return mu;
	}

	/*
	 * 获取提现折扣 (non-Javadoc)
	 * 
	 * @see com.yiliao.service.app.MoneyService#getPutforwardDiscount()
	 */
	@Override
	@Cache(cacheKey = "#APP_GOLD_RECHARGE_DISCOUNT_LIST_2_&t_end_type#", setArguments = false, expire = 24 * 60L)
	public MessageUtil getPutforwardDiscount(int t_end_type) {
		MessageUtil mu = new MessageUtil();
		try {

			String sql = "SELECT t_id,t_gold,t_money FROM "
					+ "t_set_meal WHERE t_project_type = 2 AND  t_end_type = ? AND t_is_enable = 0 ORDER BY t_money ASC ";

			List<Map<String, Object>> findBySQLTOMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql,
					t_end_type);

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(findBySQLTOMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取提现折扣列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 获取充值折扣 (non-Javadoc)
	 * 
	 * @see com.yiliao.service.app.MoneyService#getRechargeDiscount()
	 */
	@Override
	@Cache(cacheKey = "#APP_GOLD_RECHARGE_DISCOUNT_LIST_1_&t_end_type#", setArguments = false, expire = 24 * 60L)
	public MessageUtil getRechargeDiscount(Integer t_end_type) {
		MessageUtil mu = new MessageUtil();

		try {
			String sql = "SELECT t_id,t_gold,t_money,t_describe FROM t_set_meal WHERE t_project_type = 1 ";

			if (null != t_end_type) {
				sql = sql + " AND  t_end_type = " + t_end_type;
			}

			sql = sql + " AND t_is_enable = 0 ORDER BY t_money ASC ";

			List<Map<String, Object>> findBySQLTOMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);
			
			if(findBySQLTOMap==null||findBySQLTOMap.isEmpty()) {
				t_end_type=-1;
				sql = "SELECT t_id,t_gold,t_money,t_describe FROM t_set_meal WHERE t_project_type = 1 ";

				if (null != t_end_type) {
					sql = sql + " AND  t_end_type = " + t_end_type;
				}
				sql = sql + " AND t_is_enable = 0 ORDER BY t_money ASC ";
				 findBySQLTOMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);
			}
			
			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(findBySQLTOMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取充值折扣列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 确认提现
	 */
	@Override
	public synchronized MessageUtil confirmPutforward(int dataId, int userId, int putForwardId) {
		MessageUtil mu = new MessageUtil();
		try {
			// 查询用户的数据编号
			String qSql = "SELECT t_id FROM t_put_forward_data WHERE t_id = ? ";
			List<Map<String, Object>> userData = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql,
					putForwardId);

			if (null == userData || userData.isEmpty()) {
				return new MessageUtil(0, "暂未绑定微信或支付宝账号.");
			}
			
			//查询用户当天是否已经提现了
			qSql = "SELECT DATE_FORMAT(t_create_time,'%Y-%m-%d') AS putdate FROM t_put_forward  WHERE t_user_id = ?  ORDER BY t_id DESC  LIMIT 1 " ;
			
			List<Map<String, Object>> putTime = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql, userId);
			
			if(!putTime.isEmpty() && putTime.get(0).get("putdate").toString().equals(DateUtils.format(new Date(), "yyyy-MM-dd"))){
				return new MessageUtil(0, "每天每位用户仅限提现一次!");
			}
 
			String sql = "SELECT t_profit_money,t_share_money FROM t_balance WHERE t_user_id = ? ";

			Map<String, Object> balance = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql, userId);

			// 查询要体现的金额
			Map<String, Object> queryPutforwardData = this.getFinalDao().getIEntitySQLDAO()
					.findBySQLUniqueResultToMap(" SELECT t_gold,t_money FROM t_set_meal  WHERE t_id = ?", dataId);

			// 收入的金币
			BigDecimal t_profit_money = new BigDecimal(balance.get("t_profit_money").toString());
			// 分享金币
			BigDecimal t_share_money = new BigDecimal(balance.get("t_share_money").toString());

			BigDecimal total = t_profit_money.add(t_share_money).setScale(2, BigDecimal.ROUND_DOWN);
			// 提现金币
			BigDecimal t_gold = new BigDecimal(queryPutforwardData.get("t_gold").toString());

			if (total.compareTo(t_gold) < 0) {

				mu = new MessageUtil(0, "金币不足!");
				return mu;
			}
			// 计算 扣除金币
			if (t_profit_money.compareTo(t_gold) > -1) {
				sql = " update t_balance set t_profit_money = ? WHERE t_user_id = ?";
				this.getFinalDao().getIEntitySQLDAO().executeSQL(sql,
						t_profit_money.subtract(t_gold).setScale(2, BigDecimal.ROUND_DOWN), userId);

			} else {
				sql = " update t_balance set t_profit_money = 0,t_share_money = ? WHERE t_user_id = ?";
				this.getFinalDao().getIEntitySQLDAO().executeSQL(sql,
						total.subtract(t_gold).setScale(2, BigDecimal.ROUND_DOWN), userId);

			}

			String orderNo = "tx" + userId + "" + System.currentTimeMillis();

			// 新增提现记录
			sql = "INSERT INTO t_put_forward (t_user_id, t_order_no, t_money, t_setmeal_id, t_create_time,t_order_state, t_income_gold, t_share_gold, t_data_id) VALUES (?,?,?,?,?,?,?,?,?) ";
			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, userId, orderNo, queryPutforwardData.get("t_money"),
					dataId, DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"), 0,
					t_profit_money.compareTo(t_gold) > -1 ? t_gold : t_profit_money,
					t_profit_money.compareTo(t_gold) > -1 ? 0
							: t_gold.subtract(t_profit_money).setScale(2, BigDecimal.ROUND_DOWN),
					putForwardId);

			sql = "SELECT t_id FROM t_put_forward WHERE t_order_no = ? AND t_user_id = ? ";

			Map<String, Object> map = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql, orderNo,
					userId);

			// 存储变动记录
			sql = "INSERT INTO t_wallet_detail ( t_user_id, t_change_type, t_change_category, t_change_front, t_value, t_change_after, t_change_time ,t_sorece_id )"
					+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?);";

			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, userId, WalletDetail.CHANGE_TYPE_PAY,
					WalletDetail.CHANGE_CATEGOR_PUT_FORWARD, total, t_gold,
					total.subtract(t_gold).setScale(2, BigDecimal.ROUND_DOWN),
					DateUtils.format(new Date(), DateUtils.FullDatePattern), map.get("t_id"));

			new PoolThread(PoolThread.data_query_extract, queryPutforwardData.get("t_money")).start();
			
			mu = new MessageUtil(1, "申请成功!");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}申请提现异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 获取可提现金额(non-Javadoc)
	 * 
	 * @see com.yiliao.service.MoneyService#getUsableGold(int)
	 */
	@Override
	public MessageUtil getUsableGold(int userId) {
		MessageUtil mu = new MessageUtil();

		try {

			List<Map<String, Object>> querySqlList = getQuerySqlList("SELECT t_profit_money,t_share_money FROM t_balance WHERE t_user_id = ?", userId);
			
			if(null != querySqlList && !querySqlList.isEmpty()) {
				// 收入的金币
				BigDecimal t_profit_money = new BigDecimal(querySqlList.get(0).get("t_profit_money").toString());
				// 分享金币
				BigDecimal t_share_money = new BigDecimal(querySqlList.get(0).get("t_share_money").toString());

				// 获取用户是否已经绑定了提现账号
				String outforwardSql = "SELECT t_id,t_real_name,t_account_number,t_nick_name,t_type,t_head_img,t_QR_code qrCode FROM t_put_forward_data  WHERE t_user_id = ? ";

				List<Map<String, Object>> data = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(outforwardSql,
						userId);

				return new MessageUtil(1, new HashMap<String, Object>() {
					private static final long serialVersionUID = 1L;
					{
						put("totalMoney", t_profit_money.add(t_share_money).intValue());
						put("data", data);
					}
				});
			} else  {
				return new MessageUtil(1, new HashMap<String, Object>() {
					private static final long serialVersionUID = 1L;
					{
						put("totalMoney", 0);
						// 获取用户是否已经绑定了提现账号
						String outforwardSql = "SELECT t_id,t_real_name,t_account_number,t_nick_name,t_type,t_head_img,t_QR_code qrCode FROM t_put_forward_data  WHERE t_user_id = ? ";
						List<Map<String, Object>> data = getQuerySqlList(outforwardSql,userId);
						
						put("data", data);
					}
				});
			}
			

		

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取可提现金额异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}

		return mu;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.yiliao.service.MoneyService#modifyPutForwardData(int,
	 * java.lang.String, java.lang.String, int)
	 */
	@Override
	public MessageUtil modifyPutForwardData(int userId, String t_real_name, String t_nick_name, String t_account_number,
			int t_type, String t_head_img,String qrCodeUrl) {
		MessageUtil mu = new MessageUtil();
		try {
			// 查询用户的数据编号
			String qSql = "SELECT t_id FROM t_put_forward_data WHERE t_user_id = ? AND t_type = ?";
			List<Map<String, Object>> userData = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql, userId,
					t_type);

			int t_id = 0;
			// 不存在 新增绑定操作
			if (null == userData || userData.isEmpty()) {

				String inSql = "INSERT INTO t_put_forward_data (t_user_id, t_real_name, t_nick_name,t_head_img,t_account_number, t_create_time, t_QR_code, t_type) VALUES (?,?,?,?,?,?,?,?)";

				this.getFinalDao().getIEntitySQLDAO().executeSQL(inSql, userId, t_real_name, t_nick_name, t_head_img,
						t_account_number, DateUtils.format(new Date(), DateUtils.FullDatePattern), qrCodeUrl,t_type);

				Map<String, Object> map = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(qSql, userId,
						t_type);

				t_id = (Integer) map.get("t_id");
			} else { // 存在数据 更新资料

				String upSql = "UPDATE t_put_forward_data SET t_real_name = ? ,t_account_number = ? ,t_nick_name = ?,t_head_img = ?,t_QR_code= ? WHERE t_user_id = ? AND t_type = ? ";

				this.getFinalDao().getIEntitySQLDAO().executeSQL(upSql, t_real_name, t_account_number, t_nick_name,
						t_head_img, qrCodeUrl,userId, t_type);

				t_id = (Integer) userData.get(0).get("t_id");
			}

			mu = new MessageUtil(1, "更新成功!");
			mu.setM_object(t_id);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("更新提现资料异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getUserGoldDetails(int userId, int year, int month, int queryType, int page) {
		MessageUtil mu = new MessageUtil();
		try {

			StringBuffer bodySql = new StringBuffer();
			bodySql.append("SELECT ");
			bodySql.append(" DATE_FORMAT(f.t_change_time,'%Y-%m-%d %T') AS tTime, ");
			bodySql.append(" f.t_value,t_change_category,f.t_change_type,f.t_sorece_id,f.t_id,f.t_user_id ");
			bodySql.append("FROM ");
			bodySql.append(" t_wallet_detail f ");
			bodySql.append("WHERE ");
			bodySql.append(" f.t_user_id = ?");
			bodySql.append(" AND f.t_value >= 1 ");
			bodySql.append(" AND f.t_change_time BETWEEN ? AND ? ");
			if (queryType == 0 || queryType == 1) {
				bodySql.append(" AND  f.t_change_type = ").append(queryType);
			}
			bodySql.append(" ORDER BY tTime DESC");

			// 得到数据明细
			List<Map<String, Object>> queryList = this.getQuerySqlList(
					"SELECT * FROM (" + bodySql + ") aa  LIMIT ? , 10;", userId,
					DateUtils.getFirstDayOfMonth(year, month), DateUtils.getLastDayOfMonth(year, month),
					(page - 1) * 10);

			// 得到统计
			Map<String, Object> totalMap = this.getMap("SELECT COUNT(1) AS totalLog FROM (" + bodySql + ") aa", userId,
					DateUtils.getFirstDayOfMonth(year, month), DateUtils.getLastDayOfMonth(year, month));

			queryList.forEach(s -> {

				Map<String, Object> map = null;

				int profitAndPay = -1;
				switch (Integer.parseInt(s.get("t_change_category").toString())) {
				case WalletDetail.CHANGE_CATEGORY_RECHARGE: // 充值
					s.put("detail", "金币充值");
					profitAndPay = 1;
					break;
				case WalletDetail.CHANGE_CATEGORY_TEXT: // 发送文字聊天
					// 获取给谁发送了文本聊天
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));
						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】与我文本聊天");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】与我文本聊天");

						s.put("t_handImg", map.get("t_handImg"));
						profitAndPay = 1;
					} else { // 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));
						if (null == map.get("t_nickName"))
							s.put("detail", "与【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】文本聊天");
						else
							s.put("detail", "与【" + map.get("t_nickName") + "】文本聊天");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
				case WalletDetail.CHANGE_CATEGORY_VIDEO: // 视频聊天
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));
						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】与我视频聊天");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】与我视频聊天");

						s.put("t_handImg", map.get("t_handImg"));
						profitAndPay = 1;
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));
						if (null == map.get("t_nickName"))
							s.put("detail", "与【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】视频聊天");
						else
							s.put("detail", "与【" + map.get("t_nickName") + "】视频聊天");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
					
				case WalletDetail.CHANGE_CATEGORY_VOICE: // 语音聊天
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));
						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】与我语音聊天");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】与我语音聊天");

						s.put("t_handImg", map.get("t_handImg"));
						profitAndPay = 1;
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));
						if (null == map.get("t_nickName"))
							s.put("detail", "与【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】语音聊天");
						else
							s.put("detail", "与【" + map.get("t_nickName") + "】语音聊天");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
				case WalletDetail.CHANGE_CATEGORY_PRIVATE_PHOTO: // 查看私密照片
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】查看私密照片");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】查看私密照片");

						s.put("t_handImg", map.get("t_handImg"));
						profitAndPay = 1;
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "查看【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】私密照片");
						else
							s.put("detail", "查看【" + map.get("t_nickName") + "】私密照片");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
				case WalletDetail.CHANGE_CATEGORY_PRIVATE_VIDEO: // 查看私密视频
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】查看私密视频");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】查看私密视频");

						profitAndPay = 1;
						s.put("t_handImg", map.get("t_handImg"));
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "查看【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】私密视频");
						else
							s.put("detail", "查看【" + map.get("t_nickName") + "】私密视频");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
				case WalletDetail.CHANGE_CATEGORY_PHONE: // 查看手机号
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】查看手机号码");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】查看手机号码");

						profitAndPay = 1;
						s.put("t_handImg", map.get("t_handImg"));
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "查看【聊友" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】手机号码");
						else
							s.put("detail", "查看【" + map.get("t_nickName") + "】的手机号码");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
				case WalletDetail.CHANGE_CATEGORY_WEIXIN: // 查看微信号
					// 收益
					if (s.get("t_change_type").toString().equals("0")) { // t_consume
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】查看微信号");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】查看微信");

						profitAndPay = 1;

						s.put("t_handImg", map.get("t_handImg"));
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "查看聊友【" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】微信号");
						else
							s.put("detail", "查看【" + map.get("t_nickName") + "】微信号");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
				case WalletDetail.CHANGE_CATEGORY_RED_PACKET: // 红包
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg,r.t_redpacket_type FROM t_redpacket_log r LEFT JOIN t_user u ON u.t_id = r.t_hair_userId WHERE  r.t_redpacket_id = ? AND r.t_redpacket_draw = 1 ;",
								s.get("t_sorece_id"));
						if (map.get("t_redpacket_type").toString().equals("0")) {
							if (null == map.get("t_nickName"))
								s.put("detail", "收到【聊友:" + map.get("t_phone").toString()
										.substring(map.get("t_phone").toString().length() - 4) + "】赠送红包");
							else
								s.put("detail", "收到【" + map.get("t_nickName") + "】赠送红包");
						} else if (map.get("t_redpacket_type").toString().equals("1")) {
							if (null == map.get("t_nickName"))
								s.put("detail", "收到【聊友:" + map.get("t_phone").toString()
										.substring(map.get("t_phone").toString().length() - 4) + "】贡献红包");
							else
								s.put("detail", "收到【" + map.get("t_nickName") + "】贡献红包");
						} else if (map.get("t_redpacket_type").toString().equals("2")) {
							s.put("detail", "新人主播认证红包");
						} else {
							s.put("detail", "官方赠送红包");
						}
						profitAndPay = 1;

						s.put("t_handImg", map.get("t_handImg"));
					} else {

						List<Map<String, Object>> sqlList = this.getQuerySqlList(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_order o LEFT JOIN t_user u ON o.t_cover_consume = u.t_id WHERE o.t_id = ?",
								s.get("t_sorece_id"));

						if (!sqlList.isEmpty()) {
							map = sqlList.get(0);
						} else {
							// 支出
							map = this.getMap(
									"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_redpacket_log r LEFT JOIN t_user u ON u.t_id = r.t_receive_userId WHERE  r.t_order_id = ? ;",
									s.get("t_sorece_id"));
						}

						if (null == map.get("t_nickName"))
							s.put("detail", "赠送【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】红包");
						else
							s.put("detail", "赠送【" + map.get("t_nickName") + "】红包");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
				case WalletDetail.CHANGE_CATEGOR_VIP: // 充值VIP
					s.put("detail", "VIP充值");
					break;
				case WalletDetail.CHANGE_CATEGOR_GIFT: // 赠送礼物
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg,g.t_gift_name FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume LEFT JOIN t_gift g ON g.t_gift_id = o.t_consume_score   WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail",
									"收到【" + map.get("t_phone").toString()
											.substring(map.get("t_phone").toString().length() - 4) + "】赠送的【"
											+ map.get("t_gift_name") + "】");
						else
							s.put("detail", "收到【" + map.get("t_nickName") + "】赠送的【" + map.get("t_gift_name") + "】");
						profitAndPay = 1;

						s.put("t_handImg", map.get("t_handImg"));
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg,g.t_gift_name FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume LEFT JOIN t_gift g ON g.t_gift_id = o.t_consume_score  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail",
									"赠送给【" + map.get("t_phone").toString()
											.substring(map.get("t_phone").toString().length() - 4) + "】的【"
											+ map.get("t_gift_name") + "】");
						else
							s.put("detail", "赠送【" + map.get("t_nickName") + "】的【" + map.get("t_gift_name") + "】");

						s.put("t_handImg", map.get("t_handImg"));
					}

					break;
				case WalletDetail.CHANGE_CATEGOR_PUT_FORWARD: // 提现

					map = this.getMap(
							"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_wallet_detail w LEFT JOIN t_user u ON u.t_id = w.t_user_id WHERE w.t_id = ? ;",
							s.get("t_id"));

					if (null == map.get("t_nickName"))
						s.put("detail", "【聊友:"
								+ map.get("t_phone").toString().substring(map.get("t_phone").toString().length() - 4)
								+ "】申请提现");
					else
						s.put("detail", "【" + map.get("t_nickName") + "】申请提现");

					s.put("t_handImg", map.get("t_handImg"));

					break;
				case WalletDetail.CHANGE_CATEGOR_RECOMMENDATION: // 推荐分成

					List<Map<String, Object>> sqlList = getQuerySqlList(
							"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_redpacket_log r LEFT JOIN t_user u ON t_hair_userId = u.t_id  WHERE t_redpacket_id = ?  AND t_receive_userId = ? ;",
							s.get("t_sorece_id"), s.get("t_user_id"));

					if (sqlList.isEmpty()) {

						List<Map<String, Object>> data = this.getQuerySqlList(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg,o.t_consume_score FROM t_order o LEFT JOIN t_user u ON u.t_id = o.t_consume WHERE o.t_id = ? ",
								s.get("t_id"));

						if (!data.isEmpty() && null == data.get(0).get("t_nickName")) {
							s.put("detail", "邀请【聊友:" + data.get(0).get("t_phone").toString()
									.substring(data.get(0).get("t_phone").toString().length() - 4) + "】注册收益");
							s.put("t_handImg", data.get(0).get("t_handImg"));
						} else if (!data.isEmpty()) {
							s.put("detail", "邀请【" + data.get(0).get("t_nickName") + "】注册收益");
							s.put("t_handImg", data.get(0).get("t_handImg"));
						} else
							s.put("detail", "邀请注册收益");

					} else {

						if (null == sqlList.get(0).get("t_nickName"))
							s.put("detail", "收到【聊友:" + sqlList.get(0).get("t_phone").toString()
									.substring(sqlList.get(0).get("t_phone").toString().length() - 4) + "】推荐收益");
						else
							s.put("detail", "收到【" + sqlList.get(0).get("t_nickName") + "】推荐收益\"");

						s.put("t_handImg", sqlList.get(0).get("t_handImg"));
					}

					profitAndPay = 1;

					break;
				case WalletDetail.CHANGE_CATEGOR_PUTFORWARD_RETURN: // 提现失败 原路退回
					s.put("detail", "提现失败!退回");
					profitAndPay = 1;
					break;
				case WalletDetail.CHANGE_CATEGOR_GIVE_GOLD: // 注册赠送
					s.put("detail", "注册系统赠送金币");
					profitAndPay = 1;
					break;
				case WalletDetail.CHANGE_CATEGOR_GUILD_INCOME: // 公会收益

					map = this.getMap(
							"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
							s.get("t_sorece_id"));

					if (null == map.get("t_nickName"))
						s.put("detail", "收到【"
								+ map.get("t_phone").toString().substring(map.get("t_phone").toString().length() - 4)
								+ "】公会贡献收益");
					else
						s.put("detail", "收到【" + map.get("t_nickName") + "】公会贡献收益");

					s.put("detail", "公会收益");

					profitAndPay = 1;
					s.put("t_handImg", map.get("t_handImg"));
					break;
				case WalletDetail.CHANGE_CATEGOR_DYNAMIC_PHOTO:
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】查看动态私密照片");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】查看动态私密照片");

						s.put("t_handImg", map.get("t_handImg"));
						profitAndPay = 1;
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "查看【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】动态私密照片");
						else
							s.put("detail", "查看【" + map.get("t_nickName") + "】动态私密照片");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
				case WalletDetail.CHANGE_CATEGOR_DYNAMIC_VIDEO:
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】查看动态私密视频");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】查看动态私密视频");

						profitAndPay = 1;
						s.put("t_handImg", map.get("t_handImg"));
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "查看【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】动态私密视频");
						else
							s.put("detail", "查看【" + map.get("t_nickName") + "】动态私密视频");

						s.put("t_handImg", map.get("t_handImg"));
					}
					break;
					
				case WalletDetail.CHANGE_CATEGOR_FIRST_PRIZE:
					map = this.getMap(
							"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
							s.get("t_sorece_id"));
					s.put("detail", "获得推广【"+map.get("t_nickName")+"】获得首冲奖励");
					s.put("t_handImg", map.get("t_handImg"));
					profitAndPay = 1;
					break;
				case WalletDetail.CHANGE_CATEGORY_QQ: // 查看QQ
					// 收益
					if (s.get("t_change_type").toString().equals("0")) {
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "【聊友:" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】查看QQ");
						else
							s.put("detail", "【" + map.get("t_nickName") + "】查看QQ");

						profitAndPay = 1;
						s.put("t_handImg", map.get("t_handImg"));
					} else {
						// 支出
						map = this.getMap(
								"SELECT u.t_nickName,u.t_phone,u.t_handImg FROM t_user u LEFT JOIN t_order o ON u.t_id =o.t_cover_consume  WHERE o.t_id = ? ;",
								s.get("t_sorece_id"));

						if (null == map.get("t_nickName"))
							s.put("detail", "查看【聊友" + map.get("t_phone").toString()
									.substring(map.get("t_phone").toString().length() - 4) + "】QQ");
						else
							s.put("detail", "查看【" + map.get("t_nickName") + "】的QQ");

						s.put("t_handImg", map.get("t_handImg"));
					}
					
					break;
				case WalletDetail.PLATFORM_DEDUCTION:
					s.put("detail", "平台扣除金币");
					break;
				}

				s.remove("t_id");
				s.remove("t_sorece_id");
				s.put("profitAndPay", profitAndPay);
				s.remove("t_change_type");
			});

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(new HashMap<String, Object>() {
				{
					put("pageCount",
							Integer.parseInt(totalMap.get("totalLog").toString()) % 10 == 0
									? Integer.parseInt(totalMap.get("totalLog").toString()) / 10
									: Integer.parseInt(totalMap.get("totalLog").toString()) / 10 + 1);
					put("data", queryList);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取金币明细异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getProfitAndPayTotal(int userId, int year, int month) {
		MessageUtil mu = new MessageUtil();
		try {
			// 收入查询
			StringBuffer profitSql = new StringBuffer(170);
			profitSql.append("SELECT ");
			profitSql.append(" SUM(f.t_value) AS totalGold ");
			profitSql.append("FROM ");
			profitSql.append(" t_wallet_detail f ");
			profitSql.append("WHERE ");
			profitSql.append(" f.t_user_id = ? ");
			profitSql.append(" AND f.t_change_type = 0 ");
			profitSql.append(" AND f.t_value >= 1 ");
			profitSql.append(" AND f.t_change_time BETWEEN ? ");
			profitSql.append(" AND ? ");

			Map<String, Object> profit = this.getMap(profitSql.toString(), userId,
					DateUtils.getFirstDayOfMonth(year, month), DateUtils.getLastDayOfMonth(year, month));

			StringBuffer paySql = new StringBuffer(170);
			paySql.append("SELECT ");
			paySql.append(" SUM(f.t_value) AS totalGold ");
			paySql.append("FROM ");
			paySql.append(" t_wallet_detail f ");
			paySql.append("WHERE ");
			paySql.append(" f.t_user_id = ? ");
			paySql.append(" AND f.t_change_type = 1 ");
			paySql.append(" AND f.t_value >= 1 ");
			paySql.append(" AND f.t_change_time BETWEEN ? ");
			paySql.append(" AND ? ");

			Map<String, Object> pay = this.getMap(paySql.toString(), userId, DateUtils.getFirstDayOfMonth(year, month),
					DateUtils.getLastDayOfMonth(year, month));

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(new HashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					put("profit", null == profit.get("totalGold") ? 0
							: new BigDecimal(profit.get("totalGold").toString()).intValue());
					put("pay", null == pay.get("totalGold") ? 0
							: new BigDecimal(pay.get("totalGold").toString()).intValue());
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取指定月份的收入与支出异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 获取支付配置列表
	 */
	@Override
	public MessageUtil getPayDeployList() {
		try {

			List<Map<String, Object>> sqlList = getQuerySqlList(
					"SELECT t_id,t_pay_icon payIcon,t_pay_name payName,t_pay_type payType,t_is_default isdefault FROM t_pay_dispose  WHERE t_is_scrap = 1 ORDER BY t_is_default DESC ");

			return new MessageUtil(1, sqlList);
		} catch (Exception e) {
			e.printStackTrace();
			return new MessageUtil(0, "程序异常!");
		}
	}

}
