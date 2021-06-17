package com.yiliao.service.service1_7;

import com.yiliao.util.MessageUtil;

public interface SpeedDatingService {

	/**
	 * 主播开启输配
	 * 
	 * @param userId
	 * @param roomId
	 * @return
	 */
	MessageUtil openSpeedDating(int userId, int roomId);

	/**
	 * 结束速配
	 * 
	 * @param userId
	 * @return
	 */
	MessageUtil endSpeedDating(int userId,int methodType);

	/**
	 * 用户拉取速配主播
	 * 
	 * @param userId
	 * @return
	 */
	MessageUtil getSpeedDatingAnchor(int userId);
	
	/**
	 *  获取主播速配时长
	 * @param userId
	 * @return
	 */
	MessageUtil getUserSpeedTime(int userId);

	MessageUtil getSpeedDatingList();

}
