package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface VideoChatService {
	
	/**
	 * IM获取用户签名
	 * @param userId
	 * @return
	 */
	public MessageUtil getImUserSig(int userId);
	/**
	 * 获取用户的用户的usersig 和 privateMapKey
	 * @param userId
	 * @return
	 */
	public MessageUtil getVideoChatAutograph(int userId,int anthorId);
	
	/**
	 * 获取PrivateMapKey
	 * @param userId
	 * @param roomId
	 * @return
	 */
	public MessageUtil getVideoChatPriavteMapKey(int userId,int roomId);
	
	/**
	 * 获取 VIP视频开关
	 * @param type  1 拨打视频 2:接听视频 返回 false:VIP打开 并判断是否为VIP
	 * 	 @param userId 用户Id
	 * @return
	 */
	public Boolean checkVipSwitch(int type,int userId);
	
	
	/**
	 * 获取 用户勿扰开关
	 * @param type  1 拨打视频 2:接听视频 返回 false:VIP打开 并判断是否为VIP
	 * 	 @param userId 用户Id
	 * @return
	 */
	public Boolean checkChatSwitch(int type,int userId);
	
	/**
	 * 黑名单查询
	 * @param userId 查询人
	 * @param coverUserId 被拉入黑名单人
	 * @return
	 */
	public Boolean checkBlackUserInfo(int userId, int coverUserId); 
	MessageUtil getVideoScreenshotStatus();

	MessageUtil addVideoScreenshotInfo(int userId,int videoUserId, int videoAnchorUserId, int roomId,String url);

	/**
	 * 判断主播是否还有免费次数
	 * @param userId 用户id
	 * @param coverLinkUserId 主播id
	 * @return true 允许拨打视频   false 已无次数,不能拨打视频
	 */
	MessageUtil svipSwitch(int userId, int coverLinkUserId);

}
