package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface AlbumService {
	/**
	 * 获取我的相册列表
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil getMyPhotoList(int userId,int page);
	
	/**
	 * 添加照片进入相册
	 * @param userId
	 * @param t_title
	 * @param url
	 * @param type
	 * @param t_is_private
	 * @return
	 */
	public MessageUtil addMyPhotoAlbum(int userId,String t_title,String video_img,String fileId,String url,int type,int gold);

	/**
	 * 获取视频或者图片列表
	 * @param seeUserId
	 * @param coverUserId
	 * @param page 分页参数
	 * @param t_file_type 文件类型 0.图片 1.视频
	 * @return
	 */
	 MessageUtil getDynamicList(int seeUserId,int coverUserId,int page,Integer t_file_type);
	/**
	 * 删除相册图片
	 * @param photoId
	 * @return
	 */
	public MessageUtil delMyPhoto(int photoId,int userId);
	
	/**
	 * 1.4版 获取用户全年的图片
	 * @param userId
	 * @return
	 */
	public MessageUtil getMyAnnualAlbum(int userId,int t_file_type,int page);
	
	/**
	 * 1.4版  获取用户月相册(分页)
	 * @param userId
	 * @param year
	 * @param month
	 * @param page
	 * @return
	 */
	public MessageUtil getMyMonthAlbum(int userId,int year,int month,int page);

	public MessageUtil setFirstAlbum(int userId, int albumId);
	
}
