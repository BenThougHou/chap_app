package com.yiliao.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.yiliao.service.ActivityService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.SystemConfig;

/**
 * 活动抽奖
 * @author Administrator
 *
 */
@Service("activityService")
public class ActivityServiceImpl extends ICommServiceImpl implements ActivityService {

	@Override
	public synchronized void shareRedPacket(int userId,int activityId) {
		try {
			//根据活动编号得到那个奖品明细
			String qSql = "SELECT t_id,t_prize_size,t_surplus_number FROM t_activity_detail WHERE t_activity_id = ? AND t_is_join = 0";
			
			List<Map<String, Object>> prizeData = this.getQuerySqlList(qSql, activityId);
			
			List<Integer> arr = new ArrayList<Integer>();
			
			prizeData.forEach(s ->{
				//取得奖励剩余名额
				int Quota = Integer.parseInt(s.get("t_surplus_number").toString());
				
				for (int i = 0; i < Quota; i++) {
					arr.add(Integer.parseInt(s.get("t_id").toString()));
				}
			});
			
//			for(Map<String, Object> m : prizeData){
//				//取得奖励剩余名额
//				int Quota = Integer.parseInt(m.get("t_surplus_number").toString());
//				
//				for (int i = 0; i < Quota; i++) {
//					arr.add(Integer.parseInt(m.get("t_id").toString()));
//				}
//			}
			//判断是否还存在奖励
			if(!arr.isEmpty()){
				//打乱数据
				Collections.shuffle(arr);
				//
			    Random random = new Random();
			    int nextInt = random.nextInt(arr.size());
			    
			    prizeData.forEach(s ->{

					//得到用户以中的奖项
					if(arr.get(nextInt) == Integer.parseInt(s.get("t_id").toString())){
						//计算用户已中奖的金币
						int gold = 0;
						
						String prizeSize = s.get("t_prize_size").toString();	
	                    //查找奖项值是否在区间
	                    if(prizeSize.indexOf(",") > 0){
	                    	String[] str = prizeSize.split(",");
	                    	gold = random.nextInt(Integer.parseInt(str[1]))%(Integer.parseInt(str[1])-Integer.parseInt(str[0])+1) + Integer.parseInt(str[0]);
	                    }else{
	                    	gold = Integer.parseInt(s.get("t_prize_size").toString());
	                    }
	                    
	                    logger.info("当前{}用户中奖了{}个金币",userId,gold);
						
	                    //把数据插入到数据库中
	                    String inSql = "INSERT INTO t_award_record (t_user_id, t_activity_id, t_prizedetai_id, t_award_gold, t_award_time) VALUES (?,?,?,?,?) ";
	                    this.executeSQL(inSql, userId,activityId,Integer.parseInt(s.get("t_id").toString()),gold,DateUtils.format(new Date(), DateUtils.FullDatePattern));
					    
	                    //判断剩余奖项
	                    if((Integer.parseInt(s.get("t_surplus_number").toString()) -1) == 0){
	                    	//更新当前奖项剩余数量为0 且不在参与抽奖了
	                    	String uSql = "UPDATE t_activity_detail SET t_surplus_number = 0,t_is_join = 1 WHERE t_id = ? ";
	                    	this.executeSQL(uSql, s.get("t_id"));
	                    }else{
	                    	//当前奖项名额减一
	                    	String uSql = "UPDATE t_activity_detail SET t_surplus_number = t_surplus_number -1 WHERE t_id = ? ";
	                    	this.executeSQL(uSql, s.get("t_id"));
	                    }
	                    
	                    //把数据插入到红包记录中
	                	String  sql = "INSERT INTO t_redpacket_log (t_hair_userId, t_receive_userId, t_redpacket_content, t_redpacket_gold, t_redpacket_draw, t_redpacket_type, t_create_time) VALUES (?, ?, ?, ?, ?, ?, ?);";

	            		this.executeSQL(sql, 0,userId,"收到官方奖励的新人红包"+gold+"个金币",
	            				gold,0,2,DateUtils.format(new Date(), DateUtils.FullDatePattern));
	                    
	                    //给APP推送用户有新红包到达
	    				HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "sendRedPackegNotice.do","userId=" + userId);
					}
				
			    });
				 
//				for(Map<String, Object> m : prizeData){
//					//得到用户以中的奖项
//					if(arr.get(nextInt) == Integer.parseInt(m.get("t_id").toString())){
//						//计算用户已中奖的金币
//						int gold = 0;
//						
//						String prizeSize = m.get("t_prize_size").toString();	
//	                    //查找奖项值是否在区间
//	                    if(prizeSize.indexOf(",") > 0){
//	                    	String[] str = prizeSize.split(",");
//	                    	gold = random.nextInt(Integer.parseInt(str[1]))%(Integer.parseInt(str[1])-Integer.parseInt(str[0])+1) + Integer.parseInt(str[0]);
//	                    }else{
//	                    	gold = Integer.parseInt(m.get("t_prize_size").toString());
//	                    }
//	                    
//	                    logger.info("当前{}用户中奖了{}个金币",userId,gold);
//						
//	                    //把数据插入到数据库中
//	                    String inSql = "INSERT INTO t_award_record (t_user_id, t_activity_id, t_prizedetai_id, t_award_gold, t_award_time) VALUES (?,?,?,?,?) ";
//	                    this.executeSQL(inSql, userId,activityId,Integer.parseInt(m.get("t_id").toString()),gold,DateUtils.format(new Date(), DateUtils.FullDatePattern));
//					    
//	                    //判断剩余奖项
//	                    if((Integer.parseInt(m.get("t_surplus_number").toString()) -1) == 0){
//	                    	//更新当前奖项剩余数量为0 且不在参与抽奖了
//	                    	String uSql = "UPDATE t_activity_detail SET t_surplus_number = 0,t_is_join = 1 WHERE t_id = ? ";
//	                    	this.executeSQL(uSql, m.get("t_id"));
//	                    }else{
//	                    	//当前奖项名额减一
//	                    	String uSql = "UPDATE t_activity_detail SET t_surplus_number = t_surplus_number -1 WHERE t_id = ? ";
//	                    	this.executeSQL(uSql, m.get("t_id"));
//	                    }
//	                    
//	                    //把数据插入到红包记录中
//	                	String  sql = "INSERT INTO t_redpacket_log (t_hair_userId, t_receive_userId, t_redpacket_content, t_redpacket_gold, t_redpacket_draw, t_redpacket_type, t_create_time) VALUES (?, ?, ?, ?, ?, ?, ?);";
//
//	            		this.executeSQL(sql, 0,userId,"收到宜聊官方奖励的新人红包"+gold+"个金币",
//	            				gold,0,2,DateUtils.format(new Date(), DateUtils.FullDatePattern));
//	                    
//	                    //给APP推送用户有新红包到达
//	                    //socket推送
//	    				NewRedPacketRes newRedP = new NewRedPacketRes();
//	    				newRedP.setMid(Mid.noticeNewRedPacketRes);
//	    				
//	    			    IoSession ioSession = UserIoSession.getInstance().getMapIoSession(userId);
//	    				
//	    			    if(null != ioSession) {
//	    			    	ioSession.write(JSONObject.fromObject(newRedP).toString());
//	    			    }
//					}
//				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("发放分享红包异常!", e);
		}
	}
	 

}
