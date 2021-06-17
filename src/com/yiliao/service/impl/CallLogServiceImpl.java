package com.yiliao.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yiliao.service.CallLogService;
import com.yiliao.util.MessageUtil;

@Service("callLogService")
public class CallLogServiceImpl extends ICommServiceImpl implements CallLogService {

	/**
	 * 获取用户通话记录
	 */
	@Override
	public MessageUtil getCallLog(int userId, int page) {
		try {
			StringBuffer body = new StringBuffer();
			body.append("SELECT u.t_id,u.t_nickName,u.t_handImg,u.t_role,u.t_phone,c.t_call_time,"
					+ "DATE_FORMAT(c.t_create_time,'%Y-%m-%d %T') AS t_create_time ,'1' AS callType  ");
			body.append("FROM t_call_log   c LEFT JOIN t_user u ON c.t_answer_user = u.t_id WHERE t_callout_user = ?");
			body.append(" UNION ");
			body.append("SELECT u.t_id,u.t_nickName,u.t_handImg,u.t_role,u.t_phone,c.t_call_time,DATE_FORMAT(c.t_create_time,'%Y-%m-%d %T') AS t_create_time ,'2' AS callType  ");
			body.append("FROM t_call_log c LEFT JOIN t_user u ON c.t_callout_user = u.t_id  WHERE t_answer_user = ? ");
			
			//获取总记录数
			Map<String, Object> toMap = this.getMap("SELECT COUNT(1) AS total FROM ("+body+ ") aa " , userId,userId);
			
			
			List<Map<String,Object>> sqlList = this.getQuerySqlList("SELECT * FROM ("+body + ") aa ORDER BY aa.t_create_time DESC LIMIT ?,10;",
					userId,userId,(page-1)*10);
			
			sqlList.forEach(s ->{
				if(null == s.get("t_nickName")) {
					s.put("t_nickName", "聊友:"+s.get("t_phone").toString().substring(s.get("t_phone").toString().length()-4));
				}
				//处理通话时间
				s.remove("t_phone");
			});
			
			
			return new MessageUtil(1, new HashMap<String,Object>(){{
				put("data", sqlList);
				put("pageCount",  Integer.parseInt(toMap.get("total").toString())%10 == 0?
						Integer.parseInt(toMap.get("total").toString())/10:
						Integer.parseInt(toMap.get("total").toString())/10+1);
			}});
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取通话记录异常!", userId,e);
			return new MessageUtil(0, "程序异常!");
		}
	}

}
