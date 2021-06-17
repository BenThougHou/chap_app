package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;

import com.yiliao.domain.Balance;
import com.yiliao.domain.Extract;
import com.yiliao.domain.WalletDetail;
import com.yiliao.service.GoldComputeService;
import com.yiliao.util.DateUtils;

@Service("goldComputeService")
public class GoldComputeServiceImpl extends ICommServiceImpl implements GoldComputeService {
	/**
	 * 获取平台分成比例
	 * 
	 * @return
	 */
	private List<Map<String, Object>> allotRatio() {
		// 取得各种分账比例
		String sql = "SELECT t_project_type,t_extract_ratio FROM t_extract";
		return this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);

	}

	/**
	 * 获取公会分账比例
	 * 
	 * @param guildId
	 */
	private Map<String, Object> getGuild(int guildId) {

		String qSql = "SELECT t_extract,t_user_id  FROM t_guild  WHERE t_id = ? AND t_examine= 1 ";

		List<Map<String, Object>> guildMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql, guildId);

		if (!guildMap.isEmpty()) {
			return guildMap.get(0);
		}
		return null;
	}

	/**
	 * 用户消费
	 * 
	 * @param userId          用户编号
	 * @param CHANGE_CATEGORY 消费类型 查看 WalletDetail 常量
	 * @param consumeGold     消费的金币
	 * @return
	 */
	public synchronized boolean userConsume(int userId, int change_category, BigDecimal consumeGold, int soureceId) {

		Map<String, Object> user = queryUserData(userId);

		BigDecimal totalGold = new BigDecimal(user.get("t_recharge_money").toString())
				.add(new BigDecimal(user.get("t_profit_money").toString()))
				.add(new BigDecimal(user.get("t_share_money").toString()));
		// 如果金币不足时 直接返回
		if (totalGold.setScale(2, BigDecimal.ROUND_DOWN).compareTo(consumeGold.setScale(2, BigDecimal.ROUND_DOWN)) < 0 ) {
			return false;
		}

		saveChangeRecord(userId, totalGold, consumeGold, WalletDetail.CHANGE_TYPE_PAY, change_category, 0, soureceId);

		return true;
	}

	/**
	 * gold 需分配的金币数 consumeUserId 消费人 coverConsumeUserId 被消费人 change_category 消费类型
	 * sourceId 数据源
	 */
	public synchronized void distribution(BigDecimal gold, int consumeUserId, int coverConsumeUserId,
			int change_category, int sourceId) {

		Map<String, Object> consume = queryUserData(consumeUserId);

		Map<String, Object> coverConsume = queryUserData(coverConsumeUserId);

		// 被消费人的收益
		BigDecimal coverGold = null;
		// 平台总的收益(暂未分配)
		BigDecimal systemGold = null;

		// 对消费的金币分为百分 得到每份数据
		BigDecimal copy = gold.divide(new BigDecimal(100), 3, RoundingMode.HALF_UP);

		List<Map<String, Object>> ratio = allotRatio();
		// 循环分配数据
		for (Map<String, Object> map : ratio) {
			// 平台抽成比例
			if (Extract.PROJECT_TYPE_PLATFORM == Integer.parseInt(map.get("t_project_type").toString())) {
				// 得到用户的分成比例
				BigDecimal fallInto = new BigDecimal("100");
				fallInto = fallInto.subtract(new BigDecimal(map.get("t_extract_ratio").toString()));

				coverGold = copy.multiply(fallInto).setScale(2, BigDecimal.ROUND_DOWN);

				systemGold = copy.multiply(new BigDecimal(map.get("t_extract_ratio").toString())).setScale(2, BigDecimal.ROUND_DOWN);

				break;
			}

		}

		/************ 存储被消费者的记录 *************/

		if (change_category != WalletDetail.CHANGE_CATEGORY_RED_PACKET) {

			saveChangeRecord(Integer.parseInt(coverConsume.get("t_id").toString()),
					new BigDecimal(coverConsume.get("t_profit_money").toString()), coverGold,
					WalletDetail.CHANGE_TYPE_INCOME, change_category, Balance.GOLD_TYPE_PROFIT, sourceId);
		} else {

			rebateRedPacket(Integer.parseInt(consume.get("t_id").toString()),
					Integer.parseInt(coverConsume.get("t_id").toString()),
					"来至于" + consume.get("t_nickName") + "的红包:" + gold + "金币\n实际到账:" + coverGold + "个金币", coverGold, 0,
					sourceId);
		}

		/*************************************/

		// 给推广用户分配收益 systemGold 这里的收益已经分配过了 剩下的全是 平台的收益
		systemGold = allotPlatformProfit(ratio, copy, consume, coverConsume, systemGold, sourceId);
		// 存储平台收益
		platformProfit(change_category, systemGold);

	}

	/**
	 * 分配平台收益 (含推广渠道)
	 * 
	 * @param ratio        系统设置的分别比例
	 * @param copy         分隔100份以后 每份的基准值
	 * @param consume      消费者
	 * @param coverConsume coverConsume 被消费者
	 * @param systemGold   平台总的分配额(还需要分减给推广用户)
	 */
	private BigDecimal allotPlatformProfit(List<Map<String, Object>> ratio, BigDecimal copy,
			Map<String, Object> consume, Map<String, Object> coverConsume, BigDecimal systemGold, int sourceId) {

		Map<Integer, BigDecimal> profit = new HashMap<Integer, BigDecimal>();

		BigDecimal totalAllotMoney = new BigDecimal(0);
		// 分配平台的总收益
		for (Map<String, Object> map : ratio) {

			if (Integer.parseInt(map.get("t_project_type").toString()) == Extract.PROJECT_TYPE_ONE_ANCHOR_EXTENSION) {
				profit.put(Extract.PROJECT_TYPE_ONE_ANCHOR_EXTENSION,
						copy.multiply(new BigDecimal(map.get("t_extract_ratio").toString())));
				totalAllotMoney = totalAllotMoney.add(profit.get(Extract.PROJECT_TYPE_ONE_ANCHOR_EXTENSION));
			} else if (Integer.parseInt(map.get("t_project_type").toString()) == Extract.PROJECT_TYPE_TWO_ANCHOR_EXTENSION) {
				profit.put(Extract.PROJECT_TYPE_TWO_ANCHOR_EXTENSION,
						copy.multiply(new BigDecimal(map.get("t_extract_ratio").toString())));
				totalAllotMoney = totalAllotMoney.add(profit.get(Extract.PROJECT_TYPE_TWO_ANCHOR_EXTENSION));
			} else if (Integer.parseInt(map.get("t_project_type").toString()) == Extract.PROJECT_TYPE_TWO_USER_EXTENSION) {
				profit.put(Extract.PROJECT_TYPE_TWO_USER_EXTENSION,
						copy.multiply(new BigDecimal(map.get("t_extract_ratio").toString())));
				totalAllotMoney = totalAllotMoney.add(profit.get(Extract.PROJECT_TYPE_TWO_USER_EXTENSION));
			} else if (Integer.parseInt(map.get("t_project_type").toString()) == Extract.PROJECT_TYPE_ONE_USER_EXTENSION) {
				profit.put(Extract.PROJECT_TYPE_ONE_USER_EXTENSION,
						copy.multiply(new BigDecimal(map.get("t_extract_ratio").toString())));
				totalAllotMoney = totalAllotMoney.add(profit.get(Extract.PROJECT_TYPE_ONE_USER_EXTENSION));
			}

		}
		
		logger.info("总的需要分配金币-->{}",totalAllotMoney);

		/******
		 * 获取当前消费人是否是由CPS联盟推广进入的 如果是 那么推广用户将不在返利 否则正常返利
		 ********/
//		String qSql = " SELECT t_id,t_proportions FROM t_cps WHERE t_user_id =? AND t_audit_status = 1 ";
//
//		List<Map<String, Object>> cpsList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql,
//				Integer.parseInt(consume.get("t_referee").toString()));
		// 判断当前消费人是否存在推荐人 且计算分配金币
//		if (null != consume.get("t_referee") && !"".equals(consume.get("t_referee").toString())
//				&& !"0".equals(consume.get("t_referee").toString()) && cpsList.isEmpty()) {
//		 AND t_start_using = 0
		String sSql = " SELECT t_id FROM t_cps_user WHERE t_uid =? ";
		List<Map<String, Object>> cpsList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sSql,
				Integer.parseInt(consume.get("t_referee").toString()));
		
		if (null != consume.get("t_referee") && !"".equals(consume.get("t_referee").toString())
					&& !"0".equals(consume.get("t_referee").toString())&& cpsList.isEmpty()) {
			// 查询是否存在一级推荐人
			Map<String, Object> oneExtension = this
					.queryUserData(Integer.parseInt(consume.get("t_referee").toString()));
			
			//判断二级推荐人是否为CPS
			 cpsList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sSql,
						Integer.parseInt(oneExtension.get("t_referee").toString()));
			 
			// 判断是否存在二级推荐人
			if (null != oneExtension && null != oneExtension.get("t_referee")
					&& !"".equals(oneExtension.get("t_referee").toString())
//					&& !"0".equals(oneExtension.get("t_referee").toString()) && cpsList.isEmpty()) {
				    && !"0".equals(oneExtension.get("t_referee").toString())&& cpsList.isEmpty()) {
				// 查询二级推荐人
				Map<String, Object> twoExtension = this
						.queryUserData(Integer.parseInt(oneExtension.get("t_referee").toString()));
				BigDecimal money = profit.get(Extract.PROJECT_TYPE_TWO_USER_EXTENSION);
				;

				if (money.compareTo(new BigDecimal(1)) >= 0 && !twoExtension.isEmpty() && null != twoExtension
						&& !twoExtension.isEmpty()) {
					// 给间接推广人分配金额
					rebateRedPacket(Integer.valueOf(consume.get("t_id").toString()),
							Integer.valueOf(twoExtension.get("t_id").toString()),
							"来自于" + consume.get("t_nickName") + "师徒奖励红包:", money, 1, sourceId);
				}
				totalAllotMoney = totalAllotMoney.add(money);
			}

			BigDecimal money = profit.get(Extract.PROJECT_TYPE_ONE_USER_EXTENSION);
			;

			if (money.compareTo(new BigDecimal(1)) >= 0 && null != oneExtension && !oneExtension.isEmpty()) {
				// 给推荐人分配收益
				rebateRedPacket(Integer.valueOf(consume.get("t_id").toString()),
						Integer.valueOf(oneExtension.get("t_id").toString()),
						"来自于" + consume.get("t_nickName") + "师徒奖励红包:", money, 1, sourceId);
			}

			totalAllotMoney = totalAllotMoney.add(money);
		}

		// 获取被消费人是否加入了公会
		String qSql = " SELECT ag.t_guild_id FROM t_anchor_guild ag LEFT JOIN  t_guild g ON ag.t_guild_id = g.t_id  WHERE ag.t_anchor_id = ? AND g.t_examine =1 ";
		List<Map<String, Object>> guild = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql,
				coverConsume.get("t_id"));
		if (!guild.isEmpty()) {
			// 获取当前公会的提成比例
			Map<String, Object> ex = this.getGuild(Integer.parseInt(guild.get(0).get("t_guild_id").toString()));

			// 获取应该给该公会的管理人返利多少
			BigDecimal gold = new BigDecimal(ex.get("t_extract").toString()).multiply(copy).setScale(2,
					BigDecimal.ROUND_DOWN);

			// 统计用户变动前的金额
			String banSql = "SELECT * FROM t_balance WHERE t_user_id = ? ";

			Map<String, Object> balance = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(banSql,
					ex.get("t_user_id"));

			saveChangeRecord(Integer.parseInt(ex.get("t_user_id").toString()),
					new BigDecimal(balance.get("t_profit_money").toString()), gold, WalletDetail.CHANGE_TYPE_INCOME,
					WalletDetail.CHANGE_CATEGOR_GUILD_INCOME, 2, sourceId);

			// 更新主播贡献值
			String uSql = " UPDATE t_anchor_devote SET t_devote_value = t_devote_value+? WHERE t_anchor_id=? ";

			this.getFinalDao().getIEntitySQLDAO().executeSQL(uSql, gold, coverConsume.get("t_id"));

		} else // 判断当前被消费人是否存在推荐人
		if (null != coverConsume && null != coverConsume.get("t_referee")
				&& !"".equals(coverConsume.get("t_referee").toString())
				&& !"0".equals(coverConsume.get("t_referee").toString())) {

			// 查询是否存在二级推荐人
			Map<String, Object> oneExtension = this
					.queryUserData(Integer.parseInt(coverConsume.get("t_referee").toString()));

			if (null != oneExtension && null != oneExtension && null != oneExtension.get("t_referee")
					&& !"".equals(oneExtension.get("t_referee").toString())
					&& !"0".equals(oneExtension.get("t_referee").toString())) {
				// 查询二级推荐人
				Map<String, Object> twoExtension = this
						.queryUserData(Integer.parseInt(oneExtension.get("t_referee").toString()));
				// 给间接推广人分配金额
				if (profit.get(Extract.PROJECT_TYPE_TWO_ANCHOR_EXTENSION).compareTo(new BigDecimal(1)) >= 0
						&& null != twoExtension && !twoExtension.isEmpty()) {
					rebateRedPacket(Integer.valueOf(coverConsume.get("t_id").toString()),
							Integer.valueOf(twoExtension.get("t_id").toString()),
							"来自于" + coverConsume.get("t_nickName") + "师徒奖励红包:",
							profit.get(Extract.PROJECT_TYPE_TWO_ANCHOR_EXTENSION), 1, sourceId);
				}

				totalAllotMoney = totalAllotMoney.add(profit.get(Extract.PROJECT_TYPE_TWO_ANCHOR_EXTENSION));
			}

			if (profit.get(Extract.PROJECT_TYPE_ONE_ANCHOR_EXTENSION).compareTo(new BigDecimal(1)) >= 0
					&& null != oneExtension && !oneExtension.isEmpty()) {
				// 给直接推广人分配金额
				rebateRedPacket(Integer.valueOf(coverConsume.get("t_id").toString()),
						Integer.valueOf(oneExtension.get("t_id").toString()),
						"来自于" + coverConsume.get("t_nickName") + "师徒奖励红包:",
						profit.get(Extract.PROJECT_TYPE_ONE_ANCHOR_EXTENSION), 1, sourceId);
			}

			totalAllotMoney = totalAllotMoney.add(profit.get(Extract.PROJECT_TYPE_ONE_ANCHOR_EXTENSION));

		}

		logger.info("最终分配金额-->{}",totalAllotMoney);
		// 返回 平台收益减去推广分成的比例
		return systemGold.subtract(totalAllotMoney).setScale(2, BigDecimal.ROUND_DOWN);
	}

	/**
	 * 平台收益明细
	 * 
	 * @param profit_type 收益来源
	 * @param profit_gold 收益金币
	 */
	private void platformProfit(int profit_type, BigDecimal profit_gold) {
		// 存储平台收益明细
		String sql = "INSERT INTO t_profit_detail (t_profit_type, t_profit_gold, t_create_time) " + " VALUES (?, ?, ?)";

		this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, profit_type, profit_gold,
				DateUtils.format(new Date(), DateUtils.FullDatePattern));

		// 跟新平台总收益
		sql = "UPDATE t_platform_income SET t_gold=t_gold +?";

		this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, profit_gold);
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
	 * 查询用户的信息(用户编号，用户余额，推荐人)
	 * 
	 * @param userId
	 * @return
	 */
	public Map<String, Object> queryUserData(int userId) {
		String sql = "SELECT u.t_id,u.t_sex,u.t_nickName,u.t_role,u.t_referee,b.t_recharge_money,b.t_profit_money,b.t_share_money FROM t_user u LEFT JOIN t_balance b ON b.t_user_id = u.t_id WHERE u.t_id = ?";

		List<Map<String, Object>> data = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql, userId);

		return null == data ? null : data.isEmpty() ? null : data.get(0);
	}

	/**
	 * 用户消费金币
	 * 
	 * @param userId     用户编号
	 * @param valueMoney
	 */
	private void updateBalane(int userId, BigDecimal valueMoney) {

		Map<String, Object> user = queryUserData(userId);

		// 充值金币
		BigDecimal recharge_money = new BigDecimal(user.get("t_recharge_money").toString());
		// 收益金币
		BigDecimal t_profit_money = new BigDecimal(user.get("t_profit_money").toString());
		// 分享金币
		BigDecimal t_share_money = new BigDecimal(user.get("t_share_money").toString());

		// 充值金币 大于了消费金币 直接更新用户的记录
		if (recharge_money.intValue() > valueMoney.intValue()) {

			recharge_money = recharge_money.subtract(valueMoney).setScale(2, BigDecimal.ROUND_DOWN);

			saveBalane(userId, recharge_money, Balance.GOLD_TYPE_RECHARGE);
		} else {
			// 减去所有的可用充值金币
			saveBalane(userId, new BigDecimal(0), Balance.GOLD_TYPE_RECHARGE);

			valueMoney = valueMoney.subtract(recharge_money).setScale(2, BigDecimal.ROUND_DOWN);
			// 在次计算收益金币是否足够本次消费
			if (t_profit_money.intValue() > valueMoney.intValue()) {
				t_profit_money = t_profit_money.subtract(valueMoney).setScale(2, BigDecimal.ROUND_DOWN);
				saveBalane(userId, t_profit_money, Balance.GOLD_TYPE_PROFIT);
			} else { // 还是不足以抵扣本次消费
				valueMoney = valueMoney.subtract(t_profit_money).setScale(2, BigDecimal.ROUND_DOWN);
				// 设置所有的收益金币为0
				saveBalane(userId, new BigDecimal(0), Balance.GOLD_TYPE_PROFIT);

				t_share_money = t_share_money.subtract(valueMoney).setScale(2, BigDecimal.ROUND_DOWN);
				// 剩余的消费金额使用推广金币进行支付
				saveBalane(userId, t_share_money, Balance.GOLD_TYPE_SHARE);
			}
		}
	}

	/**
	 * 记录用户的余额变动日志
	 * 
	 * @param userId         用户编号
	 * @param sourceMoney    变动前的金额
	 * @param valueMoney     变动金额
	 * @param changeType     变动类型 (收入或者支出)
	 * @param changeCategory 变动类别(明细查看 变动明细常量 WalletDetail 类 )
	 */
		public synchronized void saveChangeRecord(int userId, BigDecimal sourceMoney, BigDecimal valueMoney, int changeType,
		int changeCategory, int goldType, int soureceId) {
			// 存储变动记录
			String sql = "INSERT INTO t_wallet_detail ( t_user_id, t_change_type, t_change_category, t_change_front, t_value, t_change_after, t_change_time,t_sorece_id)"
					+ "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?);";

			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, userId, changeType, changeCategory, sourceMoney,
					valueMoney,
					changeType == WalletDetail.CHANGE_TYPE_INCOME ? sourceMoney.add(valueMoney)
							: sourceMoney.subtract(valueMoney).setScale(2, BigDecimal.ROUND_DOWN),
					DateUtils.format(new Date(), DateUtils.FullDatePattern), soureceId);

			logger.info("本次变动用户-->{},变动类型-->{},变动值->{}", userId, changeType, valueMoney);
			// 存储用户收益
			if (changeType == WalletDetail.CHANGE_TYPE_INCOME) {
				saveBalane(userId, sourceMoney.add(valueMoney), goldType);
			} else {
				// 用户消费金币
				updateBalane(userId, valueMoney);
			}

		// 清空缓存 需要重新获取
//		GoogleCacheUtil.userCache.invalidate(userId);
	}

	/**
	 * 修改用户可用余额
	 * 
	 * @param userId     用户编号
	 * @param money      变动值
	 * @param updateType 变动类型(1.充值 2.收益3.推广)
	 */
	private synchronized void saveBalane(int userId, BigDecimal money, int updateType) {

		logger.info("当前用户-->{},本次跟新结果-->{},本次更新类型-->{}", userId, money, updateType);

		// 得到数据Id
		Map<String, Object> map = this.getMap("SELECT t_id FROM t_balance WHERE t_user_id = ? ", userId);

		TransactionStatus status = this.getStatus(); // 获得事务状态

		switch (updateType) {
		case 1: // 充值
			// 存储到可用余额中
			String sql = "UPDATE t_balance SET  t_recharge_money = ? WHERE t_id = ?";
			this.executeSQL(sql, money, map.get("t_id"));
			this.getTxManager().commit(status);
			break;
		case 2: // 收益
			// 存储到可用余额中
			sql = "UPDATE t_balance SET  t_profit_money=? WHERE t_id = ?";
			this.executeSQL(sql, money, map.get("t_id"));
			this.getTxManager().commit(status);
			break;
		case 3: // 推广
			// 存储到可用余额中
			sql = "UPDATE t_balance SET  t_share_money=? WHERE t_id = ?";
			this.executeSQL(sql, money, map.get("t_id"));
			this.getTxManager().commit(status);
			break;

		}

	}
}
