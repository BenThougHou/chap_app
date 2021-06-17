package com.yiliao.service.version2_0;

import java.util.Map;

import com.yiliao.util.MessageUtil;

public interface BigRoomService {

	/**
	 * 获取主播列表
	 * @param page
	 * @return
	 */
	MessageUtil getBigRoomList(int page);
	
	/**
	 * 开启直播
	 * @param userId
	 * @return
	 */
	MessageUtil openLiveTelecast(int userId);
	/**
	 * 用户退出房间
	 * @param userId
	 * @return
	 */
	MessageUtil userQuitBigRoom(int userId);
	
	/**
	 *  获取贡献排行榜
	 * @param userId
	 * @return
	 */
	MessageUtil getContributionList(int userId,int page);
	
	/**
	 * 获取个人主页信息
	 * @param userId
	 * @return
	 */
	MessageUtil getUserIndexData(int userId);
	
	/**
	 * 获取房间中的用户列表
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getRoomUserList(int userId,int page);
	
	/**
	 * 获取主播封面
	 * @param userId
	 * @return
	 */
	MessageUtil  getUserCoverImg(int userId);
	 
	
	/**
	 * 获取所有在播的大房间主播
	 * @return
	 */
	MessageUtil getTotalBigRoomList();
}
