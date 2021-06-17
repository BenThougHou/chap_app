package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yiliao.domain.entity.Follow;
import com.yiliao.util.ModelToSQL;
import org.springframework.stereotype.Service;

import com.yiliao.service.FollowService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.MessageUtil;

/**
 * 关注相关服务实现类
 * 
 * @author Administrator
 * 
 */
@Service("followService")
public class FollowServiceImpl extends ICommServiceImpl implements
		FollowService {


	/*
	 * 分页获取浏览记录 (non-Javadoc)
	 * 
	 * @see com.yiliao.service.app.FollowService#getFollowList(int, int)
	 */
	@Override
	public MessageUtil getFollowList(int userId, int page) {
		MessageUtil mu = null;
		try {

			String sql = "SELECT u.t_id,u.t_cover_img,u.t_handImg,u.t_nickName,u.t_age,u.t_city,u.t_autograph, u.t_onLine t_state,f.t_cover_follow FROM t_follow f LEFT JOIN t_user u ON f.t_cover_follow = u.t_id   WHERE f.t_follow_id = ? ORDER BY u.t_onLine ASC,f.t_create_time DESC LIMIT ?,10 ";

			List<Map<String, Object>> dataList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql, userId,(page-1)*10);
			
			//统计总数据
			sql = "SELECT count(f.t_id) AS total FROM t_follow f LEFT JOIN t_user u ON f.t_cover_follow = u.t_id WHERE f.t_follow_id = ? ";
			
			Map<String, Object> total = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql, userId);
			
			int pageCount = Integer.parseInt(total.get("total")
					.toString()) % 10 == 0 ? Integer.parseInt(total.get(
					"total").toString()) / 10
					: Integer.parseInt(total.get("total")
							.toString()) / 10 + 1;
					
			for(Map<String, Object> m : dataList){
				m.put("avgScore", null==m.get("t_cover_follow")?5:
					this.avgScore(Integer.parseInt(m.get("t_cover_follow").toString())));
			}
			
			
			mu = new MessageUtil();
			mu.setM_object(new HashMap<String,Object>(){{
				put("pageCount", pageCount);
				put("data", dataList);
			}});
			mu.setM_istatus(1);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取浏览记录异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	
	/**
	 * 计算平均分
	 * @param userId
	 */
	public int avgScore(int userId){
		
		String sql = "SELECT AVG(t_score) AS avgScore FROM t_user_evaluation WHERE t_anchor_id = ?";
		
		Map<String, Object> avgScore = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql, userId);
		
		Double avg= Double.valueOf(null ==avgScore.get("avgScore")?"5":avgScore.get("avgScore").toString());
		
		
		return new BigDecimal(avg).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
	}

	/**
	 * 添加关注 用户A ==》用户B
	 * @param follow
	 * @return
	 */
	@Override
	public MessageUtil saveFollow(Follow follow) {
		MessageUtil mu = null;
		try {

			// 查询我关注的指定用户
//			List<Map<String,Object>> folowList = querFollows(follow.getT_follow_id(),follow.getT_cover_follow());
			// 查询指定用户是否关注我
//			List<Map<String,Object>> coverFollowList = querFollows(follow.getT_cover_follow(),follow.getT_follow_id());

//			if (folowList.isEmpty() && coverFollowList.isEmpty()){
//				// 如指定用户未关注我，我也未关注指定用户[我关注]
//				follow.setType(1);
//				follow.setT_create_time(DateUtils.format(new Date(),DateUtils.defaultDatePattern));
//				this.executeSQL(ModelToSQL.getInsertSql("t_follow", Follow.class,follow));
//			}else if(!coverFollowList.isEmpty() && folowList.isEmpty()){
//				// 指定用户关注了我，但我未关注指定用户,则修改关注数据为相互关注
//				follow.setType(2);
//				this.executeSQL(ModelToSQL.getUpdateSql("t_follow", Follow.class,follow));
//			}
			follow.setType(1);
			follow.setT_create_time(DateUtils.format(new Date(),DateUtils.defaultDatePattern));
			this.executeSQL(ModelToSQL.getInsertSql("t_follow", Follow.class,follow));
			mu = new MessageUtil(1, "关注成功!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}关注{}异常!",follow.getT_follow_id(),follow.getT_cover_follow(),e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 *  根据条件查询关注信息
	 * @param t_follow_id 关注人id
	 * @param t_cover_follow 被关注人id
	 * @return
	 */
	private List<Map<String,Object>> querFollows(int t_follow_id,int t_cover_follow){
		return this.getQuerySqlList("SELECT t_id FROM t_follow  WHERE t_follow_id = ? AND t_cover_follow = ?", t_follow_id,t_cover_follow);
	}


	/**
	 * 删除关注
	 * @param followId
	 * @param coverFollow
	 * @param type
	 * @return
	 */
	@Override
	public MessageUtil delFollow(int followId,int coverFollow,int type) {
		MessageUtil mu = null;
		try {
			String sql = null;
			sql = "DELETE FROM t_follow WHERE t_follow_id = ? and t_cover_follow = ?";

            this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, followId,coverFollow);
            
            mu = new MessageUtil(1, "取消关注成功!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}取消关注异常!", followId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}


	/*
	 * 获取用户是否关注指定用户(non-Javadoc)
	 * @see com.yiliao.service.FollowService#getSpecifyUserFollow(int, int)
	 */
	@Override
	public MessageUtil getSpecifyUserFollow(int userId, int coverFollow) {
		try {
			List<Map<String,Object>> sqlList = this.getQuerySqlList("SELECT t_id FROM t_follow  WHERE t_follow_id = ? AND t_cover_follow = ?", userId,coverFollow);
			//用户未关注过
			
			if(sqlList.isEmpty()) {
				return new MessageUtil(1,0);
			}else {
				return new MessageUtil(1, 1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取是否关注{}异常!",userId,coverFollow,e);
			return new MessageUtil(0, "程序异常!");
		}
	}

}
