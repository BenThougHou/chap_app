package com.yiliao.service;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.yiliao.util.MessageUtil;

public interface SystemService {

	/**
	 * 获取私密照片收费设置
	 * @return
	 */
	MessageUtil getPrivatePhotoMoney();
	
	/**
	 * 获取私密视频收费设置
	 * @return
	 */
	MessageUtil getPrivateVideoMoney();
	
	/**
	 * 获取主播收费设置
	 * @return
	 */
	MessageUtil getAnthorChargeList();
	
	/**
	 * 获取认证
	 * @return
	 */
	MessageUtil getIdentificationWeiXin();
	/**
	 * 获取客服QQ
	 * @param userId
	 * @return
	 */
	MessageUtil getServiceQQ(int userId);
	
	/**
	 * 获取帮助列表
	 * @param userId
	 * @return
	 */
	MessageUtil getHelpContre(int userId);
	/**
	 * 加载预览图
	 * @param response
	 */
	void onloadGlanceOver(int userId,HttpServletResponse response);
	
	/**
	 * 获取速配提示消息
	 * @return
	 */
	Map<String, Object>  getSpreedTipsMsg();
		
		
	
}
