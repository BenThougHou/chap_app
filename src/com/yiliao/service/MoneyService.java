package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface MoneyService {

	/**
	 * 获取用户的钱包
	 * @param userId
	 * @return
	 */
	public MessageUtil getUserMoney(int userId);
	
	/**
	 * 查询用户余额
	 * @param userId
	 * @return
	 */
	public MessageUtil getQueryUserBalance(int userId);
	
	
	/**
	 * 获取提现折扣
	 * @return
	 */
	public MessageUtil getPutforwardDiscount(int t_end_type);
	
	/**
	 * 获取充值折扣
	 * @return
	 */
	public MessageUtil getRechargeDiscount(Integer t_end_type);
	
	/**
	 * 获取可提现金币
	 * @param userId
	 * @return
	 */
	public MessageUtil getUsableGold(int userId);
	
	/**
	 * 更新提现资料
	 * @param userId
	 * @param t_real_name
	 * @param t_account_number
	 * @param t_type
	 * @return
	 */
	public MessageUtil modifyPutForwardData(int userId,String t_real_name,String t_nick_name,String t_account_number,int t_type,String t_head_img,String qrCodeUrl);
	
	/**
	 * 确认提现
	 * @param dataId
	 * @param userId
	 * @return
	 */
	public MessageUtil confirmPutforward(int dataId,int userId,int putForwardId);
	
	/**
	 *   获取用户金币明细
	 * @param userId
	 * @param year
	 * @param month
	 * @param queryType
	 * @return
	 */
	public MessageUtil getUserGoldDetails(int userId,int year,int month,int queryType,int page);
	
	/**
	 * 获取指定月份的收入与支出
	 * @param userId
	 * @param year
	 * @param monty
	 * @return
	 */
	MessageUtil getProfitAndPayTotal(int userId,int year,int month);
	
	
	/**
	 * 获取支付配置
	 * @return
	 */
	MessageUtil getPayDeployList();
	
}
