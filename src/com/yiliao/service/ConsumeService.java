package com.yiliao.service;

import java.math.BigDecimal;
import java.util.Map;

import com.yiliao.util.MessageUtil;

public interface ConsumeService {
	
	/**
	 * 查看图片
	 * @param consumeUserId
	 * @param coverConsumeUserId
	 * @return
	 */
	public MessageUtil seeImgConsume(int consumeUserId,int coverConsumeUserId,int photoId);
	
	/**
	 * 查看私密视频
	 * @param consumeUserId
	 * @param coverConsumeUserId
	 * @return
	 */
	public MessageUtil seeVideoConsume(int consumeUserId,int coverConsumeUserId,int videoId);
	
	/**
	 * 查看手机号
	 * @param consumeUserId
	 * @param coverConsumeUserId
	 * @return
	 */
	public MessageUtil seePhoneConsume(int consumeUserId,int coverConsumeUserId);
	
	/**
	 * 查看微信号
	 * @param consumeUserId
	 * @param coverConsumeUserId
	 * @return
	 */
	public MessageUtil seeWeiXinConsume(int consumeUserId,int coverConsumeUserId);

	/**
	 * 非VIP发送文本消息 (non-Javadoc)
	 * @param consumeUserId 消费者id
	 * @param coverConsumeUserId 被消费者id
	 * @param chatType 聊天类型 1:文字 2:图片 3:语音
	 */
	public MessageUtil sendTextConsume(int consumeUserId,int coverConsumeUserId);
	
	/**
	 * VIP 查看私密图片或者视频
	 * @param vipUserId
	 * @param sourceId
	 * @return
	 */
	public MessageUtil vipSeeData(int vipUserId,int sourceId);
	
	
	/**
	 * 消费者给被消费者发红包
	 * @param consumeUserId
	 * @param coverConsumeUserId
	 * @param gold
	 */
	public MessageUtil sendRedEnvelope(int consumeUserId,int coverConsumeUserId , int gold);
	
	/**
	 * 获取礼物列表
	 * @return
	 */
	public MessageUtil getGiftList();
	
	/**
	 * 用户赠送礼物
	 * @param consumeUserId
	 * @param coverConsumeUserId
	 * @param giftId
	 * @param giftNum
	 * @return
	 */
	public MessageUtil userGiveGift(int consumeUserId,int coverConsumeUserId , int giftId,int giftNum);
	
	
	/**
	 * 领取红包
	 * @param userId
	 * @return
	 */
	public MessageUtil receiveRedPacket(Integer t_id,int userId);
	
	/**
	 * 用户充值VIP
	 * @param userId
	 * @param setMealId
	 * @return
	 */
	public MessageUtil vipStoreValue(int userId,int setMealId,int payType,int payDeployId);
	
	/**
	 * 用户充值金币
	 * @param userId
	 * @param setMealId
	 * @param payType
	 * @return
	 */
	public MessageUtil goldStoreValue(int userId,int setMealId,int payType,int payDeployId);
	
	/**
	 * 支付成功回调
	 * @param t_order_no 服务器内部订单号
	 * @param t_tripartite_order 第三方订单号
	 */
	public void payNotify(String t_order_no,String t_tripartite_order);
	/**
	 * 支付宝验证
	 * @param params
	 */
	public void getAlipayPublicKey(Map<String, String> params);
	
	/**
	 * 获取支付宝公钥
	 */
	public String getAlipayPublicKey() ;
	/**
	 * 获取支付宝的app_Id
	 * @return
	 */
	public String getAlipayAppId();
	
	/**
	 * 异步处理充值回调后的其它信息
	 * @param t_id
	 * @param userId
	 */
    void asynchronousProcessing(int userId);

	public MessageUtil seeQQConsume(int int1, int int2);

	/**
	 * 获取守护礼物
	 * @return
	 */
	MessageUtil getGuard();

	/**
	 * 支付派回调地址
	 * @param orderId 订单id
	 * @param channelOrderNum 第三方订单号
	 * @param payAount 金额
	 */
    void zhifupaiH5_callback(String orderId, String channelOrderNum, BigDecimal payAount);
}
