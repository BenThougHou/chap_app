package com.yiliao.service;

import java.util.Map;

public interface CallBackService {
	
	/**
	 * 根据订单号获取订单信息
	 * @param orderNo
	 * @return
	 */
	public Map<String, Object> getOrderByOrderNo(String orderNo);
	
	/**
	 * 支付成功处理
	 * @param orderNo
	 * @param channel
	 */
	public void alipayPaymentComplete(String orderNo);
	
//	/**
//	 * 验证支付的金额是否完全一致
//	 * @param orderNo
//	 * @return
//	 */
//	public Boolean payMoneyIdentical(String orderNo,String realprice,int userId);
//	
//	/**
//	 * 
//	 * @param userId
//	 * @return
//	 */
//	public MessageUtil getHtmlPayNotify(int userId);

}
