package com.yiliao.service;

import com.yiliao.util.MessageUtil;

public interface CallLogService {

	/**
	 * 获取通话记录
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getCallLog(int userId,int page);
	
}
