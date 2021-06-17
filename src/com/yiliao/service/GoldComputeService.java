package com.yiliao.service;

import java.math.BigDecimal;
import java.util.Map;

public interface GoldComputeService {
	/**
	 * 扣除用户消费金币
	 * @param userId
	 * @param change_category
	 * @param consumeGold
	 * @return
	 */
	 boolean  userConsume(int userId, int change_category,BigDecimal consumeGold,int sourceId);
	/**
	 * 分配金币
	 * @param gold 金币数
	 * @param consume 消费人
	 * @param coverConsume 被消费人
	 * @param change_category 变动类型
	 * @param ratio 比例
	 */
	void distribution(BigDecimal gold, int consumeUserId, int coverConsumeUserId, int change_category,int source);
	
	/**
	 * 获取用户数据
	 * @param userId
	 * @return
	 */
	Map<String, Object> queryUserData(int userId);
	
	/**
	 * 余额变动日志
	 * @param userId 用户编号
	 * @param sourceMoney  变动前的金额
	 * @param valueMoney  变动值
	 * @param changeType  变动类型
	 * @param changeCategory  变动类别
	 * @param goldType  金币类型
	 */
	void saveChangeRecord(int userId, BigDecimal sourceMoney,
			BigDecimal valueMoney, int changeType, int changeCategory,
			int goldType,int soureceId);

}
