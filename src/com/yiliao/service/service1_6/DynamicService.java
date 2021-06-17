package com.yiliao.service.service1_6;

import com.yiliao.util.MessageUtil;

import net.sf.json.JSONArray;

public interface DynamicService {

	/**
	 *  获取动态列表
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil  getDynamicList(int userId,int reqType,int page);
	
	/**
	 *  发布动态
	 * @param userId
	 * @param files
	 * @param title
	 * @param content
	 * @return
	 */
	MessageUtil releaseDynamic(int userId,String title,String content,String address,int isVisible,JSONArray files);
	
	/**
	 * 评论动态
	 * @param userId
	 * @param coverUserId
	 * @param comment
	 * @return
	 */
	MessageUtil discussDynamic(int userId,int coverUserId,String comment,int dynamicId);
	/**
	 * 点赞
	 * @param userId
	 * @param dynamicId
	 * @return
	 */
	MessageUtil giveTheThumbsUp(int userId,int dynamicId);
	/**
	 * 获取动态明细
	 * @param userId
	 * @param dynamicId
	 * @return
	 */
	MessageUtil getDynamicDetails(int userId,int dynamicId);
	
	/**
	 *  动态查看付费文件
	 * @param userId
	 * @param fileId
	 * @return
	 */
	MessageUtil dynamicPay(int userId,int fileId);
	/**
	 * 删除动态
	 * @param userId
	 * @param dynamicId
	 * @return
	 */
	MessageUtil delDynamic(int userId,int dynamicId);
	/**
	 * 获取评论列表
	 * @param userId
	 * @param dynamicId
	 * @return
	 */
	MessageUtil getCommentList(int userId,int dynamicId,int page);
	
	/**
	 * 删除评论
	 * @param userId
	 * @param commentId
	 * @return
	 */
	MessageUtil delComment(int userId,int commentId);
	
	/**
	 * 后台调用通知
	 * @param userId
	 * @param type
	 */
	void sendSocketNotice(int userId);
	
	/**
	 * 获取点赞列表
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getPraiseList(int userId,int dynamicId,int page);
	
	/**
	 * 获取用户是否有新的动态消息
	 * @param user
	 * @return
	 */
	MessageUtil getUserDynamicNotice(int userId);
	
	/**
	 * 获取当前用户最新评论消息
	 * @param userId
	 * @return
	 */
	MessageUtil getUserNewComment(int userId);
	
	/**
	 * 获取自己的动态列表
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getOwnDynamicList(int userId,int page);
	
	/**
	 * 查看个人动态
	 * @param userId
	 * @param coverUserId
	 * @param page
	 * @return
	 */
	MessageUtil getPrivateDynamicList(int userId,int coverUserId,int reqType,int page);
	 

}
