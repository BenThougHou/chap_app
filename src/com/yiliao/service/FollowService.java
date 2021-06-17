package com.yiliao.service;

import com.yiliao.domain.entity.Follow;
import com.yiliao.util.MessageUtil;

public interface FollowService {
	
	
	/**
	 * 分页获取浏览记录列表
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil getFollowList(int userId,int page);
	
	/**
	 * 添加关注
	 * @param followUserId
	 * @param coverFollowUserId
	 * @return
	 */
	public MessageUtil  saveFollow(Follow follow);
	
	
	/***
	 * 删除关注
	 * @param followId
	 * @return
	 */
	public MessageUtil delFollow(int followId,int coverFollow,int type);
	
	/**
	 * 获取当前用户是否关注指定用户
	 * @param userId
	 * @param coverFollow
	 * @return
	 */
	public MessageUtil getSpecifyUserFollow(int userId,int coverFollow);

}
