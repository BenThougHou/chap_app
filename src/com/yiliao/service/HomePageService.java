package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface HomePageService {
	
	
	/**
	 * 获取主页列表
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil  getHomePageList(int userId,int page,Integer albumId);
	
	/**
	 * 获取推荐主播
	 * @param userId
	 * @param page
	 * @param albumId
	 * @return
	 */
	public MessageUtil  getHomeNominateList(int userId,int page);
	
	
	/**
	 * Obtain play page
	 * @param userId
	 * @return
	 */
	public MessageUtil getAnchorPlayPage(int consumeUserId,int coverConsumeUserId,Integer albumId,Integer queryType);
	
	/**
	 * 获取banner列表
	 * @return
	 */
	public MessageUtil getBannerList();
	
	/**
	 * IOS获取banner列表
	 * @param userId
	 * @return
	 */
	public MessageUtil getIosBannerList();
	/**
	 * 获取视频列表
	 * @param page
	 * @return
	 */
	public MessageUtil getVideoList(int page,int userId,int queryType);
	
	/**
	 * 获取同城
	 * @param page
	 * @param userId
	 * @return
	 */
	public MessageUtil getCityWideList(int page ,int userId);
	
	/**
	 * 获取搜索列表
	 * @param page
	 * @param condition
	 * @return
	 */
	public MessageUtil getSearchList(int userId,int page,String condition);
	
	/**
	 * 在线用户
	 * @param userId
	 * @param page
	 * @param search
	 * @return
	 */
	public MessageUtil getOnLineUserList(int userId,int page,String search,Integer searchType);
	
	/**
	 * 获取魅力排行榜
	 * @param userId
	 * @param queryType
	 * @return
	 */
	public MessageUtil getGlamourList(int userId,int queryType);

	/**
	 * 获取消费排行榜
	 * @param userId
	 * @param queryType
	 * @return
	 */
	public MessageUtil getConsumeList(int userId,int queryType);
	
	/**
	 * 获取豪礼排行榜
	 * @param userId
	 * @param queryType
	 * @return
	 */
	public MessageUtil getCourtesyList(int userId,int queryType);
	
	/**
	 * 获取主播个人收益明细
	 * @param userId
	 * @return
	 */
	public MessageUtil getAnchorProfitDetail(int userId,int queryType);
	
	/**
	 * 获取风格设置
	 * @return
	 */
	MessageUtil getStyleSetUp();
	
	/**
	 * get try compere list
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil getTryCompereList(int userId,int page);
	
	/**
	 * get new compere list
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil getNewCompereList(int userId,int page);
	public MessageUtil getSelectCharAnother(int userId,int anotherId);

	public MessageUtil operatingTopping(int userId);

	/**
	 * 男用户置顶
	 * @param userId
	 * @return
	 */
	MessageUtil userTopping(int userId);
}
