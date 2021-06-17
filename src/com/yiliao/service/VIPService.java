package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface VIPService {

	
	/**
	 * 获取VIP列表
	 * @param t_vip_type vip类型
	 * @return
	 */
	public MessageUtil getVIPSetMealList(Integer t_vip_type);
	
	/**
	 * 修改所有已到期的VIP用户状态
	 * @return
	 */
	public void updateVIPExpire();
	
	/**
	 * 修改虚拟主播状态
	 */
	public void updateVirtualState();
	
	
}
