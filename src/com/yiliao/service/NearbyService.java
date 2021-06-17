package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface NearbyService {

	/**
	 * 上传用户坐标
	 * @param userId
	 * @param lat
	 * @param lng
	 * @return
	 */
	public MessageUtil uploadCoordinate(int userId, Double lat, Double lng);
	
	/**
	 * 获取范围内的用户列表数据
	 * @param lat
	 * @param lng
	 * @return
	 */
	public MessageUtil getNearbyList(Double lat,Double lng,int userId);
	
	/**
	 * 获取附近主播列表
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil getAnthorDistanceList(int userId,int page,double lat,double lng);
	/**
	 * 获取用户信息
	 * @param userId
	 * @param coverSeeUserId
	 * @param lat
	 * @param lng
	 * @return
	 */
	public MessageUtil getUserDeta(int userId,int coverSeeUserId,double lat,double lng);

    MessageUtil uploadLocation(int userId, String city);
}
