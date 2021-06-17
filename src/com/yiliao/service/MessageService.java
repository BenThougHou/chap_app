package com.yiliao.service;

import com.yiliao.domain.MessageEntity;
import com.yiliao.util.MessageUtil;

public interface MessageService {

	/**
	 * 获取未读消息数
	 * @param userId
	 * @return
	 */
    public MessageUtil getUnreadMessage(int userId);
    
    /**
     * 分页获取 用户的消息列表
     * @param userId
     * @param page
     * @return
     */
    public MessageUtil getMessageList(int userId,int page);
    
    /**
     * 把消息设置为已读
     * @param messageId
     * @return
     */
    public MessageUtil setupRead(int messageId);
    /**
     * 删除消息
     * @param messageId
     * @return
     */
    public MessageUtil delMessage(int messageId);
    
    /**
     * 发送消息
     * @param entity
     */
    public void  pushMessage(MessageEntity entity);
}
