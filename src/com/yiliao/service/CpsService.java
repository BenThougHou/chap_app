package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface CpsService {

	/**
	 * 申请成为联盟主
	 * @param cpsName
	 * @param cpsUrl
	 * @param active
	 * @param proportions
	 * @param realName
	 * @param takeOutId
	 * @param accountNumber
	 * @param phone
	 * @return
	 */
	MessageUtil addCpsMs(int userId,String cpsName,String cpsUrl,int active,int proportions,String realName,
			int takeOutId,String accountNumber,String phone);
	
	
	/**
	 * 获取贡献列表
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getContributionList(int userId,int page);
	
	/**
	 * 获取cps统计明细
	 * @param userId
	 * @return
	 */
	MessageUtil getTotalDateil(int userId);
}
