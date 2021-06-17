package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yiliao.service.CommentService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.MessageUtil;

/**
 * 评论服务层实现类
 * @author Administrator
 *
 */
@Service("commentService")
public class CommentServiceImpl extends ICommServiceImpl implements CommentService{

	
	/*
	 * 用户评价
	 * (non-Javadoc)
	 * @see com.yiliao.service.app.CommentService#saveComment(int, int, int, java.lang.String)
	 */
	@Override
	public MessageUtil saveComment(int commUserId, int coverCommUserId,
			int commScore,String comment, String lables) {
		MessageUtil mu = null;
		try {
			String sql = "SELECT t_handImg,t_nickName,t_phone FROM t_user WHERE t_id = ?";
			Map<String, Object> userMap = this.getMap(sql, commUserId);
			
			sql = " INSERT INTO t_user_evaluation (t_user_id, t_user_hand, t_user_nick, t_anchor_id, t_comment, t_score, `t_create_time`) "
					+ "VALUES ( ?, ?, ?, ?, ?, ?, ?);";
			
			String nickNmae =null == userMap.get("t_nickName")?"聊友:"+userMap.get("t_phone").toString()
					.substring(userMap.get("t_phone").toString().length()-4):userMap.get("t_nickName").toString();
			//保存评价信息
			this.executeSQL(sql, commUserId
					,null == userMap.get("t_handImg")?"":userMap.get("t_handImg").toString(),nickNmae,
					coverCommUserId,comment,commScore,DateUtils.format(new Date(), DateUtils.FullDatePattern));
			
			//查询出当前人的最新评价记录 并取得评价编号
			sql = "SELECT t_id FROM t_user_evaluation WHERE t_user_id = ? ORDER BY t_create_time DESC LIMIT 0,1";
			
			Map<String, Object> t_id = this.getMap(sql, commUserId);
			
			if(null!=lables && !"".equals(lables.trim())){
				//分割评价标签 
				String[] split = lables.split(",");
				
				sql = "INSERT INTO t_discuss_record(t_label_id, t_evaluation_id) VALUES ( ?, ?) ";
				//循环把评价标签插入到数据库中
				for (int i = 0; i < split.length; i++) {
					this.executeSQL(sql, split[i]
							,Integer.parseInt(t_id.get("t_id").toString()));
				}
			}
			
			//修改主页表中的评分
			this.executeSQL("UPDATE t_home_table SET t_score = ? WHERE t_id = ? ", avgScore(coverCommUserId),coverCommUserId);
		
			mu = new MessageUtil(1, "评价成功!");
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}用户对主播进行评价{}",commUserId,coverCommUserId, e);
			mu = new  MessageUtil(0, "程序异常!");
		}
		return mu;
	}
	
	/**
	 * 计算平均分
	 * 
	 * @param userId
	 */
	public int avgScore(int userId) {

		String sql = "SELECT AVG(t_score) AS avgScore FROM t_user_evaluation WHERE t_anchor_id = ?";

		Map<String, Object> avgScore = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql, userId);

		Double avg = Double.valueOf(null == avgScore.get("avgScore") ? "5" : avgScore.get("avgScore").toString());

		return new BigDecimal(avg).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
	}

}
