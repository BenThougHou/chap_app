package com.yiliao.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yiliao.domain.MessageEntity;
import com.yiliao.service.MessageService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.MessageUtil;

/**
 * 消息服务层
 * 
 * @author Administrator
 * 
 */
@Service("messageService")
public class MessageServiceImpl extends ICommServiceImpl implements
		MessageService {


	/*
	 * 统计未读消息数
	 * (non-Javadoc)
	 * @see com.yiliao.service.app.MessageService#getUnreadMessage(int)
	 */
	@Override
	public MessageUtil getUnreadMessage(int userId) {
		MessageUtil mu = null;

		try {
			
			String sql = "SELECT count(t_id) as total FROM t_message WHERE t_user_id = ? AND t_is_see = 0 ";
			
			Map<String, Object> toMap = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql, userId);
			
			List<Map<String,Object>> sqlList = this.getQuerySqlList("SELECT t_id,t_message_content,t_is_see,DATE_FORMAT(t_create_time,'%Y-%m-%d %T') AS t_create_time FROM t_message WHERE t_user_id = ? ORDER BY t_create_time DESC LIMIT 1;", userId);
			

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(new HashMap<String,Object>() {{
				put("totalCount", toMap.get("total"));
				put("data",sqlList.isEmpty() ? new HashMap<String,Object>():sqlList.get(0));
			}});
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("统计未读消息数异常!", e);
			mu = new MessageUtil();
		}

		return mu;
	}

	/*
	 * 分页获取消息列表
	 * (non-Javadoc)
	 * @see com.yiliao.service.app.MessageService#getMessageList(int, int)
	 */
	@Override
	public MessageUtil getMessageList(int userId, int page) {
		MessageUtil mu = null;
		try {

			//得到数据列表
			String sql = "SELECT t_id,t_message_content,t_is_see,DATE_FORMAT(t_create_time,'%Y-%m-%d %T') AS t_create_time FROM t_message WHERE t_user_id = ? ORDER BY t_create_time DESC LIMIT ?,10;";
			
			List<Map<String, Object>> dataList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql, userId,(page-1)*10);
			
			//统计总记录数
			sql = "SELECT count(t_id) AS total FROM t_message WHERE t_user_id = ? ";
			
			Map<String, Object> totalMap = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql, userId);
			
			int pageCount = Integer.parseInt(totalMap.get("total")
					.toString()) % 10 == 0 ? Integer.parseInt(totalMap.get(
					"total").toString()) / 10 : Integer.parseInt(totalMap
					.get("total").toString()) / 10 + 1;
			//装载结果
			mu = new MessageUtil(1,new HashMap<String,Object>(){{
				put("pageCount", pageCount);
				put("data", dataList);
			}});
		 
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("统计未读消息数异常!", e);
			mu = new MessageUtil();
		}

		return mu;
	}

	/*
	 * 把消息设置为已读(non-Javadoc)
	 * 
	 * @see com.yiliao.service.app.MessageService#setupRead(int)
	 */
	@Override
	public MessageUtil setupRead(int userId) {
		MessageUtil mu = null;

		try {

			String sql = "UPDATE t_message SET t_is_see=1 WHERE t_user_id= ? ";

			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, userId);

			mu = new MessageUtil(1, "设置成功!");

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("消息设置为已读异常!", e);
			mu = new MessageUtil(0,"程序异常!");
		}
		return mu;
	}

	/*
	 * 删除消息(non-Javadoc)
	 * @see com.yiliao.service.app.MessageService#delMessage(int)
	 */
	@Override
	public MessageUtil delMessage(int messageId) {
		MessageUtil mu = null;
		try {
			
			String sql = "DELETE FROM t_message WHERE t_id = ?";
			
			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, messageId);
			
			mu =  new MessageUtil(1, "删除成功!");
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除消息异常!", e);
			mu = new MessageUtil(0,"程序异常!");
		}
		return null;
	}

	
	/*
	 * 发送消息
	 * (non-Javadoc)
	 * @see com.yiliao.service.MessageService#pushMessage(com.yiliao.domain.MessageEntity)
	 */
	@Override
	public void pushMessage(MessageEntity entity) {
		try {
			
			//存储消息
			String messageSql = "INSERT INTO t_message (t_user_id, t_message_content, t_create_time, t_is_see) VALUES (?, ?, ?, ?);";
			
			this.executeSQL(messageSql, 
					entity.getT_user_id(),
					entity.getT_message_content(),
					DateUtils.format(entity.getT_create_time(), DateUtils.FullDatePattern),
					entity.getT_is_see());
			
//			this.applicationContext.publishEvent(new PushMesgEvnet(entity));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

}
