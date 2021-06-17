package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yiliao.domain.LoginInfo;
import com.yiliao.domain.WalletDetail;
import com.yiliao.service.HomePageService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RedisUtil;
import com.yiliao.util.SystemConfig;
import com.yiliao.util.cache.Cache;

import net.sf.json.JSONObject;

/**
 * 主页服务层
 * 
 * @author Administrator
 * 
 */
@Service("homePageService")
public class HomePageServiceImpl extends ICommServiceImpl implements HomePageService {
	@Autowired
	private RedisUtil redisUtill;

	/**
	 * app主页
	 * 
	 * @param userId    请求人编号
	 * @param page      页码
	 * @param queryType 类型 -1:全部 0.女 1.男
//	 * @param condition 查询条件(昵称 or ID号)
//	 * @param response
	 */
	@Override
	public MessageUtil getHomePageList(int userId, int page, Integer queryType) {
		MessageUtil mu = null;
		try {
			//queryType  0.新人1.推荐2.活跃.3.女神 4 一键匹配
			
			List<Map<String, Object>> dataList=null;
			int pageCount=0;
			// 获取数据
			if(queryType==4) {
				//上一次选聊获取用户
				dataList = getQuerySqlList("SELECT h.*,a.t_addres_url,'' labels FROM t_home_table h "
						+ " inner join t_album a on a.t_user_id=h.t_id and t_file_type=1 and t_auditing_type=1 and t_is_del=0  "
						+ " WHERE h.t_anchor_select=1 and h.t_online!=1   GROUP BY " + 
						  "	h.t_id  ORDER BY  RAND() LIMIT 3 ");
				if(!dataList.isEmpty()) {
					StringBuffer sb = new StringBuffer();
					for (Map<String, Object> map : dataList) {
						sb.append(map.get("t_id")+",");
					}
					redisUtill.set("SELECT_CHAT_ANOTHER_LIST_"+userId, sb.toString().substring(0, sb.length()-1), 30L);
				}
				// 统计总记录数
				pageCount = 0;
			} else {
				
				dataList = getQuerySqlList("SELECT h.*,ifnull(GROUP_CONCAT(la.t_label_name),'') labels,if(a.t_id is null,0,1) t_album_first,ifnull(a.t_addres_url,'') t_addres_url  FROM t_home_table h "
						+ " left join t_user_label l on l.t_user_id=h.t_id "
						+ " left join t_label la on la.t_id=l.t_lable_id "
						+ " left join t_album a on a.t_user_id = h.t_id and t_file_type=1 and t_auditing_type=1 and t_is_first=1  "
						+ " WHERE t_anchor_type = ? GROUP BY " + 
						"	h.t_id  ORDER BY "
						+ "t_online ASC,"
						+ "t_sort ASC,"
						+ "t_operate_time desc,"
						+ "t_score DESC LIMIT ?,10 ",queryType,(page - 1) * 10);
				// 统计总记录数
				Map<String, Object> total = this.getMap("SELECT COUNT(t_id) AS total FROM t_home_table WHERE t_anchor_type = ? ",queryType);
				pageCount = Integer.parseInt(total.get("total").toString()) % 10 == 0
						? Integer.parseInt(total.get("total").toString()) / 10
						: Integer.parseInt(total.get("total").toString()) / 10 + 1;
			}
			
			dataList.forEach(s ->{
				// 2.5.5 不使用视频，只显示图片
				s.put("t_is_public",s.get("t_album_first"));
				s.put("t_state", s.get("t_online"));
			});
			
			mu = new MessageUtil();
			mu.setM_istatus(1);
			HashMap<String,Object> hashMap = new HashMap<String, Object>();
			hashMap.put("pageCount", pageCount);
			hashMap.put("data", dataList);
			mu.setM_object(hashMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取主播列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
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

	/*
	 * 获取主播播放页数据 (non-Javadoc)
	 * 
	 * @see com.yiliao.service.app.HomePageService#getAnchorPlayPage(int)
	 */
	@Override
	public MessageUtil getAnchorPlayPage(int consumeUserId, int coverConsumeUserId, Integer albumId,
			Integer queryType) {
		MessageUtil mu = null;
		try {
			Map<String, Object> data_map = null;

			if (null == queryType || queryType == 0) { // 查询相册

				if (null != albumId && 0 != albumId) {
					data_map = this.getMap(
							"SELECT t_id,t_video_img ,t_addres_url ,t_see_count,t_title,t_user_id FROM t_album  WHERE t_id = ? AND t_file_type = 1 LIMIT 1 ",
							albumId);
					}
				else {
					data_map = this.getMap(
							"SELECT a.t_id,a.t_video_img ,a.t_addres_url,a.t_see_count,a.t_title,a.t_user_id FROM "
							+ "t_album a "
							+ " WHERE t_is_first = 1 AND t_file_type = 1 and t_auditing_type=1 AND t_user_id = ? LIMIT 1 ",
							coverConsumeUserId);
					}
				data_map.put("score", avgScore(coverConsumeUserId));
				// 相册查看次数+1
				this.executeSQL(" UPDATE t_album SET t_see_count = t_see_count+1 WHERE t_id = ?  ",
						data_map.get("t_id"));

				data_map.remove("t_id");
			} else if (queryType == 1) { // 查询动态

				data_map = this.getMap(
						"SELECT df.t_cover_img_url AS t_video_img,df.t_file_url "
						+ " AS t_addres_url,df.t_see_count,ifnull(d.t_title,'') t_title,d.t_user_id,df.t_dynamic_id FROM t_dynamic_file df "
						+ " left join t_dynamic d on d.t_id =df.t_dynamic_id WHERE df.t_id = ? AND df.t_file_type = 1 LIMIT 1 ",
						albumId);
				// 获取用户编号
//				data_map.put("t_user_id",
//						this.getMap("SELECT t_user_id  FROM t_dynamic WHERE t_id = ? ", data_map.get("t_dynamic_id"))
//								.get("t_user_id"));
				data_map.remove("t_dynamic_id");
				// 跟新
				this.executeSQL(" UPDATE t_dynamic_file SET t_see_count = t_see_count+1 WHERE t_id = ? ", albumId);
			}

			int user_id = Integer.parseInt(data_map.get("t_user_id").toString());
			// 装载用户信息
			data_map.putAll(this.getMap("SELECT t_sex,t_handImg,t_nickName,t_age,t_city,t_weixin,t_onLine  FROM t_user WHERE t_id = ? ",
					user_id));
			// 装载主播的视频收费和微信收费
			
			 List<Map<String, Object>> querySqlList = this.getQuerySqlList("SELECT t_video_gold AS videoGold,t_weixin_gold,t_voice_gold FROM t_anchor_setup WHERE t_user_id = ? ",
					user_id);
			if(querySqlList!=null&&querySqlList.size()>0) {
				data_map.putAll(querySqlList.get(0));
			}
			// 装载主播的评分
			data_map.putAll(this.getMap("SELECT ifnull(AVG(t_score),0) AS t_score FROM t_user_evaluation WHERE t_user_id = ? ", user_id));

			Map<String, Object> laud = this.getMap("SELECT COUNT(t_id) AS totalLaud FROM t_user_laud WHERE t_cover_user_id = ? ", user_id);

			Random random = new Random();
			int s = random.nextInt(10000) % (10000 - 5000 + 1) + 5000;
			// 总的点赞数
			data_map.put("laudtotal", Integer.parseInt(laud.get("totalLaud").toString()) + s);
			//粉丝数
			data_map.put("fans",this.getMap("select count(1) count from t_follow where t_cover_follow=?", coverConsumeUserId).get("count"));
			
			// 获取该用户是否已经查看过微信号了
			String sql = "SELECT * FROM t_order WHERE t_consume_type = 6 AND t_consume = ? AND t_cover_consume = ?";
			List<Map<String, Object>> isWeiXin = this.getQuerySqlList(sql,consumeUserId, coverConsumeUserId);

			data_map.put("isSee", null == isWeiXin ? 0 : isWeiXin.isEmpty() ? 0 : 1);
			
			if(null == isWeiXin || isWeiXin.isEmpty()) {
				data_map.put("t_weixin", null == data_map.get("t_weixin")?"":"*******");
			}

			// 当前用户是否对发布者进行点赞
			sql = "SELECT * FROM t_user_laud WHERE t_laud_user_id = ? AND t_cover_user_id = ? ";

			List<Map<String, Object>> laudList = this.getQuerySqlList(sql,consumeUserId, coverConsumeUserId);

			data_map.put("isLaud", laudList.isEmpty() ? 0 : 1);

			// 获取用户是否关注主播
			sql = " SELECT * FROM t_follow WHERE t_follow_id = ? AND t_cover_follow = ?  ";

			List<Map<String, Object>> followList = this.getQuerySqlList(sql,consumeUserId, coverConsumeUserId);

			data_map.put("isFollow", followList.isEmpty() ? 0 : 1);
			
			//获取主播是否正在大房间进行直播
			List<Map<String, Object>> bigRooms = this.getQuerySqlList("SELECT t_is_debut,t_room_id,t_chat_room_id FROM t_big_room_man WHERE t_user_id = ?", coverConsumeUserId);
			if(null == bigRooms || bigRooms.isEmpty())
				data_map.put("bigRoomData", new HashMap<String, Object>());
			else 
				data_map.put("bigRoomData", bigRooms.get(0));

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(data_map);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取主播列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 获取banner列表(non-Javadoc)
	 * 
	 * @see com.yiliao.service.app.HomePageService#getBannerList()
	 */
	@Override
	@Cache(cacheKey="#APP_BANNER_LIST_0#",setArguments = false,expire = 3*60L)
	public MessageUtil getBannerList() {
		MessageUtil mu = null;
		try {
			String sql = "SELECT t_img_url,t_link_url FROM t_banner WHERE t_is_enable = 0 AND t_type = 0 ";

			List<Map<String, Object>> findBySQLTOMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(findBySQLTOMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取banner列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getVideoList(int page, int userId, int queryType) {
		MessageUtil mu = null;
		try {
			StringBuffer sb = new StringBuffer();
			sb.append(
					"SELECT d.t_id,d.t_title,d.t_user_id,d.t_video_img,d.t_addres_url,d.t_is_private,d.t_money,u.t_nickName,u.t_handImg  ");
			sb.append("FROM t_album d LEFT JOIN t_user u ON d.t_user_id = u.t_id ");
			sb.append("WHERE t_file_type = 1 AND t_is_del = 0 AND d.t_auditing_type = 1 AND u.t_role = 1 ");
			if (queryType > -1) {
				sb.append(" AND t_is_private = ").append(queryType);
			}
			sb.append(" ORDER BY d.t_id DESC LIMIT ?,10;");

			List<Map<String, Object>> dataList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sb.toString(),
					(page - 1) * 10);

			String sql = "SELECT COUNT(d.t_id) AS total FROM t_album d LEFT JOIN t_user u ON d.t_user_id = u.t_id WHERE t_file_type = 1 AND t_is_del = 0 AND d.t_auditing_type = 1 AND u.t_role = 1 ";
			if (queryType > -1) {
				sql = sql + " AND t_is_private = " + queryType;
			}

			Map<String, Object> total = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql);

			int pageCount = Integer.parseInt(total.get("total").toString()) % 10 == 0
					? Integer.parseInt(total.get("total").toString()) / 10
					: Integer.parseInt(total.get("total").toString()) / 10 + 1;

			// 查询当前数据是否已经被查看过了
			sql = "SELECT * FROM t_order WHERE t_consume = ?  AND t_cover_consume = ? AND t_consume_type = ?  AND t_consume_score = ?";

			for (Map<String, Object> m : dataList) {

				List<Map<String, Object>> findBySQLTOMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql,
						userId, Integer.parseInt(m.get("t_user_id").toString()),
						WalletDetail.CHANGE_CATEGORY_PRIVATE_VIDEO, Integer.parseInt(m.get("t_id").toString()));

				if (null == findBySQLTOMap || findBySQLTOMap.isEmpty()) {
					m.put("is_see", 0);
				} else {
					m.put("is_see", 1);
				}
			}

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(new HashMap<String,Object>(){{
				put("data", dataList);
				put("pageCount", pageCount);
			}});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取视频列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 获取同城数据(non-Javadoc)
	 * 
	 * @see com.yiliao.service.HomePageService#getCityWideList(int, int)
	 */
	@Override
	public MessageUtil getCityWideList(int page, int userId) {
		MessageUtil mu = null;
		try {

			// sql.append("SELECT * FROM (");

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT 	* FROM 	( ");
			sql.append("SELECT 	u.t_id,u.t_cover_img,u.t_onLine as t_state,u.t_nickName,u.t_autograph,'1' AS t_user_type,u.t_city ");
			sql.append("FROM  t_user u ");
			sql.append("LEFT JOIN t_certification c ON c.t_user_id = u.t_id ");
			sql.append("WHERE	u.t_role = 1 AND u.t_disable = 0 AND u.t_cover_img IS NOT NULL ");
			sql.append("AND u.t_city = (SELECT t_city FROM t_user WHERE t_id = ? ) ");
			sql.append("AND u.t_sex <> ( SELECT t_sex FROM t_user WHERE t_id = ? ) ");
			sql.append("AND c.t_certification_type = 1 ");
			sql.append("GROUP BY u.t_id ");
			sql.append("UNION ");
			sql.append("SELECT u.t_id,u.t_cover_img,u.t_onLine as t_state,u.t_nickName,u.t_autograph,'2' AS t_user_type,u.t_city ");
			sql.append("FROM t_user u ");
			sql.append("LEFT JOIN t_virtual v ON v.t_user_id = u.t_id ");
			sql.append("WHERE u.t_role = 1 AND u.t_disable = 0 AND u.t_cover_img IS NOT NULL ");
			sql.append("AND u.t_city = ( SELECT t_city FROM t_user WHERE t_id = ? ) ");
			sql.append("AND u.t_sex <> ( SELECT t_sex FROM t_user WHERE t_id = ? ) ");
			sql.append("AND v.t_user_id IS NOT NULL ");
			sql.append("GROUP BY u.t_id ");
			sql.append(" ) AS aa ");
			sql.append("UNION ALL ");
			sql.append("SELECT * FROM ( ");
			sql.append("SELECT u.t_id,u.t_cover_img,u.t_onLine as t_state,u.t_nickName,u.t_autograph,'1' AS t_user_type,u.t_city ");
			sql.append("FROM t_user u ");
			sql.append("LEFT JOIN t_certification c ON c.t_user_id = u.t_id ");
			sql.append("WHERE u.t_role = 1 AND u.t_disable = 0 AND u.t_cover_img IS NOT NULL ");
			sql.append("AND (u.t_city is not NULL OR u.t_city <>  ( SELECT  t_city  FROM  t_user  WHERE  t_id = ?  )) ");
			sql.append("AND u.t_sex <> ( SELECT t_sex FROM t_user WHERE t_id = ? ) ");
			sql.append("AND c.t_certification_type = 1 ");
			sql.append("GROUP BY u.t_id ");
			sql.append("UNION ");
			sql.append("SELECT u.t_id,u.t_cover_img,u.t_onLine as t_state,u.t_nickName,u.t_autograph,'2' AS t_user_type,u.t_city ");
			sql.append("FROM t_user u ");
			sql.append("LEFT JOIN t_virtual v ON v.t_user_id = u.t_id ");
			sql.append("WHERE u.t_role = 1 AND u.t_disable = 0 AND u.t_cover_img IS NOT NULL ");
			sql.append("AND (u.t_city is not NULL OR u.t_city <>  ( SELECT  t_city  FROM  t_user  WHERE  t_id = ?  )) ");
			sql.append("AND u.t_sex <> ( SELECT t_sex FROM t_user WHERE t_id = ? ) ");
			sql.append("AND v.t_user_id IS NOT NULL ");
			sql.append("GROUP BY u.t_id ");
			sql.append(") AS bb ");

			List<Map<String, Object>> dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(
					"SELECT * FROM (" + sql.toString() + ") AS cc  LIMIT ?,10", userId, userId, userId, userId, userId,
					userId, userId, userId, (page - 1) * 10);

			Map<String, Object> total = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(
					"SELECT COUNT(1) AS total FROM (" + sql.toString() + ") AS cc ", userId, userId, userId, userId,
					userId, userId, userId, userId);

			int pageCount = Integer.parseInt(total.get("total").toString()) % 10 == 0
					? Integer.parseInt(total.get("total").toString()) / 10
					: Integer.parseInt(total.get("total").toString()) / 10 + 1;

			// 得到用户是否存在免费视频
			for (Map<String, Object> m : dataMap) {
				// 得到该主播是否存在免费的公共视频
				Map<String, Object> userVideo = this.getMap("SELECT count(t_id) AS total  FROM t_album WHERE t_user_id = ? AND t_file_type=1 AND t_is_private = 0 AND t_is_del = 0 ",
						Integer.parseInt(m.get("t_id").toString()));
				// 是否存在公共视频
				m.put("t_is_public", Integer.parseInt(userVideo.get("total").toString()) > 0 ? 1 : 0);

				m.remove("t_user_type");

			}

			mu = new MessageUtil(1,new HashMap<String,Object>(){{
				put("pageCount", pageCount);
				put("data", dataMap);
			}});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取同城异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 获取搜索列表(non-Javadoc)
	 * 
	 * @see com.yiliao.service.HomePageService#getSearchList(int, java.lang.String)
	 */
	@Override
	public MessageUtil getSearchList(int userId, int page, String condition) {
		MessageUtil mu = null;
		try {

			StringBuffer bodySql = new StringBuffer();

			bodySql.append("SELECT t_id,t_nickName,t_phone,t_handImg,t_role,t_onLine,t_idcard ");
			bodySql.append("FROM t_user WHERE t_disable = 0 AND ( t_nickName LIKE '%" + condition
					+ "%' OR t_idcard LIKE '%" + condition + "%') ORDER BY t_onLine ASC LIMIT ?,10;");

			// 获取列表
			List<Map<String, Object>> sqlList = this.getQuerySqlList(bodySql.toString(), (page - 1) * 10);

			// 获取总记录数
			Map<String, Object> total = getMap(
					"SELECT COUNT(t_id) AS total FROM t_user WHERE t_disable = 0 AND ( t_nickName LIKE '%" + condition
							+ "%' OR t_idcard LIKE '%" + condition + "%' ) ");

			// 得到总页数
			int pageCount = Integer.parseInt(total.get("total").toString()) % 10 == 0
					? Integer.parseInt(total.get("total").toString()) / 10
					: Integer.parseInt(total.get("total").toString()) / 10 + 1;

			sqlList.forEach(s -> {
				if (1 == Integer.parseInt(s.get("t_role").toString())) {
					// 得到该主播是否存在免费的公共视频
					Map<String, Object> userVideo = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(
							"SELECT count(t_id) AS total  FROM t_album WHERE t_user_id = ? AND t_file_type=1  AND t_is_del = 0 AND t_auditing_type = 1 and t_is_first=1  ",
							Integer.parseInt(s.get("t_id").toString()));
					// 是否存在公共视频
					s.put("t_is_public", Integer.parseInt(userVideo.get("total").toString()) > 0 ? 1 : 0);
				} else {
					s.put("t_is_public", 0);
				}
				// 判断用户是否有昵称
				if (null == s.get("t_nickName")) {
					s.put("t_nickName",
							"聊友:" + s.get("t_phone").toString().substring(s.get("t_phone").toString().length() - 4));
				}
				s.remove("t_phone");
			});

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(new HashMap<String, Object>() {
				{
					put("pageCount", pageCount);
					put("data", sqlList);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取搜索列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 主播获取用户
	 */
	@Override
	public MessageUtil getOnLineUserList(int userId, int page, String search, Integer searchType) {
		MessageUtil mu = null;
		try {

			return new MessageUtil(1, new HashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					long start = System.currentTimeMillis();


					// 获取粉丝数量
					FutureTask<Integer> dataCount = new FutureTask<>(new Callable<Integer>() {
						@Override
						public Integer call() throws Exception {

							Map<String, Object> map = getMap("SELECT COUNT(t_id) AS total FROM t_user WHERE t_role = 0  AND t_sex = 1 ");

							if (null == map || map.isEmpty()) {
								return 0;
							}
							return Integer.valueOf(map.get("total").toString());
						}
					});

					pool.submit(dataCount);
					put("pageCount", dataCount.get() % 10 == 0 ? dataCount.get() / 10 : dataCount.get() / 10 + 1);

					// 获取数据列表
					FutureTask<List<Map<String, Object>>> daTask = new FutureTask<>(
							new Callable<List<Map<String, Object>>>() {
								@Override
								public List<Map<String, Object>> call() throws Exception {

									StringBuffer sql = new StringBuffer();
									sql.append("SELECT u.t_autograph,ifnull(u.t_height,170) t_height,u.t_vocation,ifnull(u.t_age,18) t_age,  ");
									sql.append("u.t_id,u.t_nickName,u.t_role,u.t_handImg,u.t_sex,u.t_phone,u.t_constellation,u.t_is_vip,u.t_is_svip,u.t_onLine,aa.balance,DATE_FORMAT(u.t_create_time,'%Y-%m-%d %T') AS t_create_time ");
									sql.append("FROM ( ");
									sql.append("SELECT SUM(b.t_profit_money + b.t_recharge_money + b.t_share_money) AS balance,b.t_user_id FROM t_balance b	GROUP BY b.t_user_id ");
									sql.append(") aa LEFT JOIN t_user u ON aa.t_user_id = u.t_id ");
									sql.append("WHERE 1 = 1 AND u.t_role = 0  AND u.t_sex = 1 ");
									sql.append("ORDER BY u.t_onLine ASC,aa.balance DESC ");
									sql.append("LIMIT ?,10");

									List<Map<String,Object>> sqlList = getQuerySqlList(sql.toString(), (page - 1) * 10);
									
									// 获取充值金币 用于分配用户的充值级别
									String qSql = "SELECT SUM(r.t_recharge_money) AS money FROM t_recharge  r WHERE r.t_user_id = ? AND r.t_order_state = 1";
									
									sqlList.forEach(s -> {
										if (null == s.get("t_nickName")) {
											s.put("t_nickName","聊友:" + s.get("t_phone").toString().substring(s.get("t_phone").toString().length() - 4));
										}
										s.remove("t_phone");
										// 金币档
										s.put("goldfiles", HomePageServiceImpl.goldFiles(new BigDecimal(s.get("balance").toString()).intValue()));

										s.remove("t_create_time");
										s.remove("balance");

										List<Map<String, Object>> regList = getQuerySqlList(qSql, s.get("t_id"));

										if (null == regList || regList.isEmpty() || null == regList.get(0).get("money")) {
											s.put("grade", HomePageServiceImpl.grade(0));
										} else {
											s.put("grade", HomePageServiceImpl.grade(new BigDecimal(regList.get(0).get("money").toString()).intValue()));
										}
									});
									
									return sqlList;
								}
							});
					
					pool.submit(daTask);
					
					put("data", daTask.get());
					
					logger.info("耗时:{}",System.currentTimeMillis()-start);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("主播获取粉丝列表.", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	public static int goldFiles(int gold) {
		if (gold <= 500) {
			return 1;
		} else if (501 <= gold && gold <= 1000) {
			return 2;
		} else if (1001 <= gold && gold <= 2000) {
			return 3;
		} else if (2001 <= gold && gold <= 3000) {
			return 4;
		} else {
			return 5;
		}
	}

	/** 充值档 */
	public static int grade(int money) {

		if (money <= 1000) {
			return 1;
		} else if (money > 1000 && money <= 10000) {
			return 2;
		} else if (money >= 10001) {
			return 3;
		}
		return 0;
	}

	/**
	 * 获取魅力排行榜
	 */
	@Override
    @Cache(cacheKey="#APP_CHARM_LEADERBOARD_LIST_&queryType#",setArguments = false,expire = 30L)
	public MessageUtil getGlamourList(int userId, int queryType) {
		MessageUtil mu = null;
		try {

			StringBuffer body = new StringBuffer();
			body.append("SELECT * FROM (");
			body.append("SELECT SUM(d.t_value) AS gold,u.t_id,u.t_nickName,u.t_idcard,u.t_role,u.t_handImg,u.t_phone ");
			body.append("FROM t_wallet_detail d LEFT JOIN  t_user u ON d.t_user_id=u.t_id ");
			body.append("WHERE d.t_change_type = 0 AND d.t_change_category  BETWEEN 1 AND 6 AND u.t_disable = 0 ");
			body.append("AND  d.t_change_time BETWEEN ? AND ? ");
			body.append("GROUP BY u.t_id ");
			body.append(") aa ORDER BY aa.gold DESC LIMIT ? ");

			List<Map<String, Object>> dataMap = null;

			switch (queryType) {
			case 1:

				String today = DateUtils.getToday(new Date());

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(body.toString(), today + " 00:00:00",
						today + " 23:59:59", getRetNumber(1));

				break;
			case 2:

				Map<String, String> toWeek = DateUtils.getToWeek(new Date());

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(body.toString(),
						toWeek.get("monday") + " 00:00:00", toWeek.get("sunday") + " 23:59:59", getRetNumber(1));

				break;
			case 3:

				String toMonth = DateUtils.getToMonth(new Date());

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(body.toString(),
						DateUtils.getFirstDayOfMonth(0, Integer.parseInt(toMonth)),
						DateUtils.getLastDayOfMonth(0, Integer.parseInt(toMonth)), getRetNumber(1));

				break;
			default:

				StringBuffer qSql = new StringBuffer();
				qSql.append("SELECT * FROM (");
				qSql.append("SELECT SUM(d.t_value) AS gold,u.t_id,u.t_idcard,u.t_role,u.t_nickName,u.t_handImg,u.t_phone ");
				qSql.append(
						"FROM t_wallet_detail d LEFT JOIN  t_user u ON d.t_user_id=u.t_id WHERE d.t_change_type = 0 AND d.t_change_category  BETWEEN 1 AND 6 AND u.t_disable = 0 ");
				qSql.append("GROUP BY u.t_id ");
				qSql.append(") aa ORDER BY aa.gold DESC LIMIT ? ");

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), getRetNumber(1));

				break;
			}

			for (Map<String, Object> m : dataMap) {
				if (null == m.get("t_nickName")) {
					m.put("t_nickName",
							"聊友:" + m.get("t_phone").toString().substring(m.get("t_phone").toString().length()));
				}
				m.remove("t_phone");
				m.put("gold", new BigDecimal(m.get("gold").toString()).intValue());
			}

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(dataMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取魅力排行榜异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 获取消费排行榜(non-Javadoc)
	 * 
	 * @see com.yiliao.service.HomePageService#getConsumeList(int, int)
	 */
	@Override
    @Cache(cacheKey="#APP_CONSUMPTION_LEADERBOARD_LIST_&queryType#",setArguments = false,expire = 30L)
	public MessageUtil getConsumeList(int userId, int queryType) {
		MessageUtil mu = null;
		try {

			StringBuffer qSql = new StringBuffer();
			qSql.append("SELECT * FROM ( ");
			qSql.append("SELECT SUM(d.t_value) AS gold,u.t_id,u.t_idcard,u.t_role,u.t_nickName,u.t_handImg,u.t_phone  ");
			qSql.append(
					"FROM t_wallet_detail d LEFT JOIN  t_user u ON d.t_user_id=u.t_id WHERE d.t_change_type = 1 AND (d.t_change_category !=8 AND d.t_change_category  BETWEEN 1 AND 9 ) AND u.t_disable = 0 ");
			qSql.append(" AND  d.t_change_time BETWEEN ? AND ? ");
			qSql.append("GROUP BY u.t_id ");
			qSql.append(") aa ORDER BY aa.gold DESC LIMIT ?");

			List<Map<String, Object>> dataMap = null;

			switch (queryType) {
			case 1:

				String lastDay = DateUtils.getToday(new Date());

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), lastDay + " 00:00:00",
						lastDay + " 23:59:59", getRetNumber(2));

				break;
			case 2:

				Map<String, String> lastWeek = DateUtils.getToWeek(new Date());

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(),
						lastWeek.get("monday") + " 00:00:00", lastWeek.get("sunday") + " 23:59:59", getRetNumber(2));

				break;
			case 3:

				String month = DateUtils.getToMonth(new Date());

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(),
						DateUtils.getFirstDayOfMonth(0, Integer.parseInt(month)),
						DateUtils.getLastDayOfMonth(0, Integer.parseInt(month)), getRetNumber(2));

				break;
			default:

				qSql = new StringBuffer();

				qSql.append("SELECT * FROM ( ");
				qSql.append("SELECT SUM(d.t_value) AS gold,u.t_id,u.t_idcard,u.t_role,u.t_nickName,u.t_handImg,u.t_phone ");
				qSql.append(
						"FROM t_wallet_detail d LEFT JOIN  t_user u ON d.t_user_id=u.t_id WHERE d.t_change_type = 1 AND (d.t_change_category !=8 AND d.t_change_category  BETWEEN 1 AND 9 ) AND u.t_disable = 0 ");
				qSql.append("GROUP BY u.t_id ");
				qSql.append(") aa ORDER BY aa.gold DESC LIMIT ? ");

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), getRetNumber(2));

				break;
			}

			// 处理用户没有昵称的数据
			for (Map<String, Object> m : dataMap) {
				if (null == m.get("t_nickName")) {
					m.put("t_nickName",
							"聊友:" + m.get("t_phone").toString().substring(m.get("t_phone").toString().length() - 4));
				}
				m.remove("t_phone");
				m.put("gold", new BigDecimal(m.get("gold").toString()).intValue());
			}

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(dataMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取消费排行榜异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/*
	 * 获取豪礼排行榜(non-Javadoc)
	 * 
	 * @see com.yiliao.service.HomePageService#getCourtesyList(int, int)
	 */
	@Override
    @Cache(cacheKey="#APP_GIFT_LEADERBOARD_LIST_&queryType#",setArguments = false,expire = 30L)
	public MessageUtil getCourtesyList(int userId, int queryType) {
		MessageUtil mu = null;
		try {

			StringBuffer qSql = new StringBuffer();
			qSql.append("SELECT * FROM ( ");
			qSql.append("SELECT SUM(d.t_value) AS gold,u.t_id,u.t_idcard,u.t_role,u.t_nickName,u.t_handImg,u.t_phone  ");
			qSql.append(
					"FROM t_wallet_detail d LEFT JOIN  t_user u ON d.t_user_id=u.t_id WHERE d.t_change_type = 0 AND (d.t_change_category =7 OR d.t_change_category  = 9 ) AND u.t_disable = 0 ");
			qSql.append(" AND  d.t_change_time BETWEEN ? AND ? ");
			qSql.append(" GROUP BY u.t_id ");
			qSql.append(") aa ORDER BY aa.gold DESC LIMIT ?");
			List<Map<String, Object>> dataMap = null;
			switch (queryType) {
			case 1:

				String lastDay = DateUtils.getToday(new Date());
				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), lastDay + " 00:00:00",
						lastDay + " 23:59:59", getRetNumber(3));
				break;
			case 2:

				Map<String, String> lastWeek = DateUtils.getToWeek(new Date());

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(),
						lastWeek.get("monday") + " 00:00:00", lastWeek.get("sunday") + " 23:59:59", getRetNumber(3));

				break;
			case 3:

				String month = DateUtils.getToMonth(new Date());

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(),
						DateUtils.getFirstDayOfMonth(0, Integer.parseInt(month)),
						DateUtils.getLastDayOfMonth(0, Integer.parseInt(month)), getRetNumber(3));

				break;
			default:

				qSql = new StringBuffer();

				qSql.append("SELECT * FROM ( ");
				qSql.append("SELECT SUM(d.t_value) AS gold,u.t_id,u.t_idcard,u.t_role,u.t_nickName,u.t_handImg,u.t_phone ");
				qSql.append(
						"FROM t_wallet_detail d LEFT JOIN  t_user u ON d.t_user_id=u.t_id WHERE d.t_change_type = 0 AND (d.t_change_category =7 OR d.t_change_category  = 9 ) AND u.t_disable = 0 ");
				qSql.append("GROUP BY u.t_id ");
				qSql.append(") aa ORDER BY aa.gold DESC LIMIT ? ");

				dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), getRetNumber(3));

				break;
			}

			// 处理用户没有昵称的数据
			for (Map<String, Object> m : dataMap) {
				if (null == m.get("t_nickName")) {
					m.put("t_nickName",
							"聊友:" + m.get("t_phone").toString().substring(m.get("t_phone").toString().length()));
				}
				m.remove("t_phone");
				m.put("gold", new BigDecimal(m.get("gold").toString()).intValue());
			}

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(dataMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取豪礼排行榜异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 获取返回多少条数据
	 * 
	 * @param type 1.魅力榜 2.消费榜 3.豪礼榜
	 * @return
	 */
	public int getRetNumber(int type) {

		String qSql = " SELECT * FROM t_ranking_control ";

		List<Map<String, Object>> dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql);

		switch (type) {
		case 1:
			return Integer.parseInt(dataMap.get(0).get("t_charm_number").toString());
		case 2:
			return Integer.parseInt(dataMap.get(0).get("t_consumption_number").toString());
		default:
			return Integer.parseInt(dataMap.get(0).get("t_courtesy_number").toString());
		}
	}

	@Override
	public MessageUtil getAnchorProfitDetail(int userId, int queryType) {
		MessageUtil mu = null;
		try {

			StringBuffer qSql = new StringBuffer();
			qSql.append(
					"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 1 AND t_change_time BETWEEN ? AND ?  GROUP BY t_user_id ");
			qSql.append("UNION ");
			qSql.append(
					"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 2 AND t_change_time BETWEEN ? AND ? GROUP BY t_user_id ");
			qSql.append("UNION ");
			qSql.append(
					"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 3 AND t_change_time BETWEEN ? AND ? GROUP BY t_user_id ");
			qSql.append("UNION ");
			qSql.append(
					"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 4 AND t_change_time BETWEEN ? AND ? GROUP BY t_user_id ");
			qSql.append("UNION ");
			qSql.append(
					"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 5 AND t_change_time BETWEEN ? AND ? GROUP BY t_user_id ");
			qSql.append("UNION ");
			qSql.append(
					"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 6 AND t_change_time BETWEEN ? AND ? GROUP BY t_user_id ");

			List<Map<String, Object>> dataList = null;

			switch (queryType) {
			case 1:
				String lastDay = DateUtils.getYesterday(new Date());
				dataList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), userId,
						lastDay + " 00:00:00", lastDay + " 23:59:59", userId, lastDay + " 00:00:00",
						lastDay + " 23:59:59", userId, lastDay + " 00:00:00", lastDay + " 23:59:59", userId,
						lastDay + " 00:00:00", lastDay + " 23:59:59", userId, lastDay + " 00:00:00",
						lastDay + " 23:59:59", userId, lastDay + " 00:00:00", lastDay + " 23:59:59");

				break;
			case 2:

				Map<String, String> lastWeek = DateUtils.getLastWeek(new Date());

				dataList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), userId,
						lastWeek.get("monday") + " 00:00:00", lastWeek.get("sunday") + " 23:59:59", userId,
						lastWeek.get("monday") + " 00:00:00", lastWeek.get("sunday") + " 23:59:59", userId,
						lastWeek.get("monday") + " 00:00:00", lastWeek.get("sunday") + " 23:59:59", userId,
						lastWeek.get("monday") + " 00:00:00", lastWeek.get("sunday") + " 23:59:59", userId,
						lastWeek.get("monday") + " 00:00:00", lastWeek.get("sunday") + " 23:59:59", userId,
						lastWeek.get("monday") + " 00:00:00", lastWeek.get("sunday") + " 23:59:59");

				break;
			case 3:
				String month = DateUtils.getLastMonth(new Date());

				dataList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), userId,
						DateUtils.getFirstDayOfMonth(new Date().getYear(), Integer.parseInt(month)),
						DateUtils.getLastDayOfMonth(new Date().getYear(), Integer.parseInt(month)), userId,
						DateUtils.getFirstDayOfMonth(new Date().getYear(), Integer.parseInt(month)),
						DateUtils.getLastDayOfMonth(new Date().getYear(), Integer.parseInt(month)), userId,
						DateUtils.getFirstDayOfMonth(new Date().getYear(), Integer.parseInt(month)),
						DateUtils.getLastDayOfMonth(new Date().getYear(), Integer.parseInt(month)), userId,
						DateUtils.getFirstDayOfMonth(new Date().getYear(), Integer.parseInt(month)),
						DateUtils.getLastDayOfMonth(new Date().getYear(), Integer.parseInt(month)), userId,
						DateUtils.getFirstDayOfMonth(new Date().getYear(), Integer.parseInt(month)),
						DateUtils.getLastDayOfMonth(new Date().getYear(), Integer.parseInt(month)), userId,
						DateUtils.getFirstDayOfMonth(new Date().getYear(), Integer.parseInt(month)),
						DateUtils.getLastDayOfMonth(new Date().getYear(), Integer.parseInt(month)));

				break;

			default:

				qSql = new StringBuffer();
				qSql.append(
						"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 1 GROUP BY t_user_id ");
				qSql.append("UNION ");
				qSql.append(
						"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 2 GROUP BY t_user_id ");
				qSql.append("UNION ");
				qSql.append(
						"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 3 GROUP BY t_user_id ");
				qSql.append("UNION ");
				qSql.append(
						"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 4 GROUP BY t_user_id ");
				qSql.append("UNION ");
				qSql.append(
						"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 5 GROUP BY t_user_id ");
				qSql.append("UNION ");
				qSql.append(
						"SELECT SUM(t_value) AS gold,t_change_category FROM t_wallet_detail WHERE t_user_id = ? AND t_change_type = 0 AND  t_change_category = 6 GROUP BY t_user_id ");

				dataList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql.toString(), userId, userId, userId,
						userId, userId, userId);

				break;
			}

			List<Integer> arr = Arrays.asList(WalletDetail.CHANGE_CATEGORY_TEXT, WalletDetail.CHANGE_CATEGORY_VIDEO,
					WalletDetail.CHANGE_CATEGORY_PRIVATE_PHOTO, WalletDetail.CHANGE_CATEGORY_PRIVATE_VIDEO,
					WalletDetail.CHANGE_CATEGORY_PHONE, WalletDetail.CHANGE_CATEGORY_WEIXIN);

			// 迭代 判断数据是否存在
			for (Integer in : arr) {
				boolean isOk = false;
				for (Map<String, Object> m : dataList) {
					if (Integer.parseInt(m.get("t_change_category").toString()) == in) {
						isOk = true;
					}
					m.put("gold", new BigDecimal(m.get("gold").toString()).intValue());
				}
				if (!isOk) {
					dataList.add(new HashMap<String, Object>() {
						{
							put("gold", 0);
							put("t_change_category", in);
						}
					});
				}
			}

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(dataList);
		} catch (Exception e) {
			logger.error("获取主播个人收益明细异常!{}", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getStyleSetUp() {
		MessageUtil mu = null;
		try {

			String qSql = " SELECT t_mark FROM t_style_setup WHERE t_state = 1 ";

			List<Map<String, Object>> dataList = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql);

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(dataList.isEmpty() ? null : dataList.get(0));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取风格异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getHomeNominateList(int userId, int page) {
		MessageUtil mu = null;
		try {

			StringBuffer homeSql = new StringBuffer();
			homeSql.append("SELECT * FROM (");
			homeSql.append(" SELECT u.t_id,u.t_cover_img,u.t_onLine t_state,u.t_handImg,u.t_nickName,u.t_age,u.t_city,u.t_vocation,u.t_autograph,s.t_video_gold,AVG(IFNULL(e.t_score,5)) AS t_score ,'1' AS t_user_type,sp.t_is_nominate,sp.t_sort  ");
			homeSql.append(" FROM t_user u LEFT JOIN t_user_evaluation e ON e.t_user_id = u.t_id ");
			homeSql.append(" LEFT JOIN t_certification c ON c.t_user_id = u.t_id  LEFT JOIN t_anchor_setup s ON  s.t_user_id = u.t_id");
			homeSql.append(" LEFT JOIN t_spread sp ON sp.t_user_id = u.t_id ");
			homeSql.append(" WHERE u.t_role = 1 AND u.t_onLine != 2 AND u.t_sex = 0 AND u.t_disable = 0  AND u.t_cover_img is not null AND c.t_certification_type =1 ");
			homeSql.append(" AND sp.t_is_nominate = 1 ");
			homeSql.append(" GROUP BY u.t_id ");
			homeSql.append(" UNION ");
			homeSql.append(" SELECT u.t_id,u.t_cover_img,u.t_onLine t_state,u.t_handImg,u.t_nickName,u.t_age,u.t_city,u.t_vocation,u.t_autograph,s.t_video_gold,AVG(IFNULL(e.t_score,5)) AS t_score ,'2' AS t_user_type,sp.t_is_nominate,sp.t_sort ");
			homeSql.append(" FROM t_user u LEFT JOIN t_user_evaluation e ON e.t_user_id = u.t_id  ");
			homeSql.append(" LEFT JOIN t_virtual v ON v.t_user_id = u.t_id  LEFT JOIN t_anchor_setup s ON  s.t_user_id = u.t_id");
			homeSql.append(" LEFT JOIN t_spread sp ON sp.t_user_id = u.t_id ");
			homeSql.append("  WHERE u.t_role = 1 AND u.t_onLine != 2  AND u.t_sex = 0  AND u.t_disable = 0  AND u.t_cover_img is not null  AND v.t_user_id IS NOT NULL ");
			homeSql.append(" AND sp.t_is_nominate = 1 ");
			homeSql.append(" GROUP BY u.t_id ");
			homeSql.append(" ) AS  aa  WHERE aa.t_id !=0 ORDER BY aa.t_sort ASC,aa.t_state ASC,aa.t_user_type ASC, aa.t_score DESC ");

			List<Map<String, Object>> dataList = this.getQuerySqlList(homeSql.toString());
		 
			// 计算评分
			for (Map<String, Object> m : dataList) {
				// 得到该主播是否存在免费的公共视频
				Map<String, Object> userVideo = this.getMap(
						"SELECT count(t_id) AS total  FROM t_album WHERE t_user_id = ? AND t_file_type=1 AND t_is_private = 0 AND t_is_del = 0 AND  t_auditing_type = 1 ",
						Integer.parseInt(m.get("t_id").toString()));
				// 是否存在公共视频
				m.put("t_is_public", Integer.parseInt(userVideo.get("total").toString()) > 0 ? 1 : 0);
				m.put("t_score", avgScore(Integer.parseInt(m.get("t_id").toString())));
			}

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(new HashMap<String,Object>(){{
				put("data", dataList);
			}});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取推荐主播列表异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getTryCompereList(int userId, int page) {
		MessageUtil mu = null;
		try {
			StringBuffer homeSql = new StringBuffer();
			homeSql.append("SELECT * FROM (");
			homeSql.append(" SELECT u.t_id,u.t_cover_img,u.t_onLine t_state,u.t_handImg,u.t_nickName,u.t_age,");
			homeSql.append(" u.t_city,s.t_video_gold,AVG(IFNULL(e.t_score,5)) AS t_score ,'1' AS t_user_type ");
			homeSql.append(" FROM t_user u ");
			homeSql.append(" LEFT JOIN t_user_evaluation e ON e.t_user_id = u.t_id ");
			homeSql.append(" LEFT JOIN t_certification c ON c.t_user_id = u.t_id  ");
			homeSql.append(" LEFT JOIN t_anchor_setup s ON  s.t_user_id = u.t_id ");
			homeSql.append(" LEFT JOIN t_free_anthor f ON  f.t_user_id = u.t_id ");
			homeSql.append(" LEFT JOIN t_virtual v ON v.t_user_id = u.t_id ");
			// AND u.t_onLine = 0
			homeSql.append(
					" WHERE u.t_role = 1 AND u.t_sex = 0 AND u.t_disable = 0  AND u.t_cover_img is not null AND (c.t_certification_type =1 OR v.t_user_id IS NOT NULL) AND f.t_id is not null ");
			homeSql.append(
					"  AND s.t_video_gold <= ( SELECT e.t_gold FROM t_enroll e ORDER BY e.t_gold DESC LIMIT 1) ");
			homeSql.append(" GROUP BY u.t_id ");
			homeSql.append(" ) AS  aa   ORDER BY aa.t_state ASC,aa.t_user_type ASC, aa.t_score DESC LIMIT ?,10 ;");

			List<Map<String, Object>> dataList = this.getQuerySqlList(homeSql.toString(), (page - 1) * 10);

			StringBuffer countSql = new StringBuffer();
			countSql.append("SELECT count(aa.t_id) as total FROM (");
			countSql.append(" SELECT u.t_id,u.t_cover_img,u.t_onLine t_state,u.t_handImg,u.t_nickName,u.t_age,u.t_city,");
			countSql.append(" AVG(IFNULL(e.t_score,5)) AS t_score ,'1' AS t_user_type ");
			countSql.append(" FROM t_user u");
			countSql.append(" LEFT JOIN t_user_evaluation e ON e.t_user_id = u.t_id ");
			countSql.append(" LEFT JOIN t_certification c ON c.t_user_id = u.t_id ");
			countSql.append(" LEFT JOIN t_anchor_setup s ON  s.t_user_id = u.t_id ");
			countSql.append(" LEFT JOIN t_free_anthor f ON  f.t_user_id = u.t_id ");
			countSql.append(" LEFT JOIN t_virtual v ON v.t_user_id = u.t_id ");
			countSql.append(
					" WHERE u.t_role = 1  AND u.t_sex = 0 AND u.t_disable = 0 AND u.t_cover_img is not null AND (c.t_certification_type =1 OR v.t_user_id IS NOT NULL) AND f.t_id is not null ");
			countSql.append(
					" AND s.t_video_gold <= ( SELECT e.t_gold FROM t_enroll e ORDER BY e.t_gold DESC LIMIT 1) ");
			countSql.append(" GROUP BY u.t_id ");
			countSql.append(" ) AS  aa ;");

			Map<String, Object> totalMap = this.getMap(countSql.toString());

			int pageCount = Integer.parseInt(totalMap.get("total").toString()) % 10 == 0
					? Integer.parseInt(totalMap.get("total").toString()) / 10
					: Integer.parseInt(totalMap.get("total").toString()) / 10 + 1;
			// compute avg score
			for (Map<String, Object> m : dataList) {
				// get compere whether exist public video
				Map<String, Object> userVideo = this.getMap(
						"SELECT count(t_id) AS total  FROM t_album WHERE t_user_id = ? AND t_file_type=1 AND t_is_private = 0 AND t_is_del = 0 AND t_auditing_type = 1 ",
						Integer.parseInt(m.get("t_id").toString()));
				m.put("t_is_public", Integer.parseInt(userVideo.get("total").toString()) > 0 ? 1 : 0);
				m.put("t_score", avgScore(Integer.parseInt(m.get("t_id").toString())));
			}

			mu = new MessageUtil(1,new HashMap<String,Object>(){{
				put("pageCount", pageCount);
				put("data", dataList);
			}});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取试看主播列表异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getNewCompereList(int userId, int page) {
		MessageUtil mu = null;
		try {
			StringBuffer homeSql = new StringBuffer();
			homeSql.append("SELECT * FROM (");
			homeSql.append(" SELECT u.t_id,u.t_cover_img,u.t_onLine t_state,u.t_handImg,u.t_nickName,u.t_age,u.t_autograph,");
			homeSql.append(" u.t_city,s.t_video_gold,AVG(IFNULL(e.t_score,5)) AS t_score ,'1' AS t_user_type ");
			homeSql.append(" FROM t_user u  ");
			homeSql.append(" LEFT JOIN t_user_evaluation e ON e.t_user_id = u.t_id ");
			homeSql.append(" LEFT JOIN t_certification c ON c.t_user_id = u.t_id  ");
			homeSql.append(" LEFT JOIN t_anchor_setup s ON  s.t_user_id = u.t_id ");
			homeSql.append(
					" WHERE u.t_role = 1  AND u.t_sex = 0 AND u.t_disable = 0  AND u.t_cover_img is not null AND c.t_certification_type =1 ");
			homeSql.append(" AND u.t_create_time > '").append(DateUtils.getSevenDaysBefore()).append("'");
			homeSql.append(" GROUP BY u.t_id ");
			homeSql.append(" ) AS  aa   ORDER BY aa.t_state ASC,aa.t_user_type ASC, aa.t_score DESC LIMIT ?,10 ;");

			List<Map<String, Object>> dataList = this.getQuerySqlList(homeSql.toString(), (page - 1) * 10);

			StringBuffer countSql = new StringBuffer();
			countSql.append("SELECT count(aa.t_id) as total FROM (");
			countSql.append(" SELECT u.t_id,u.t_cover_img,u.t_onLine t_state,u.t_handImg,u.t_nickName,u.t_age,u.t_city,");
			countSql.append(" AVG(IFNULL(e.t_score,5)) AS t_score ,'1' AS t_user_type ");
			countSql.append(" FROM t_user u  ");
			countSql.append(" LEFT JOIN t_user_evaluation e ON e.t_user_id = u.t_id ");
			countSql.append(" LEFT JOIN t_certification c ON c.t_user_id = u.t_id ");
			countSql.append(
					" WHERE u.t_role = 1  AND u.t_sex = 0 AND u.t_disable = 0 AND u.t_cover_img is not null AND c.t_certification_type =1 ");
			countSql.append(" AND u.t_create_time > '").append(DateUtils.getSevenDaysBefore()).append("'");
			countSql.append(" GROUP BY u.t_id ");
			countSql.append(" ) AS  aa ;");

			Map<String, Object> totalMap = this.getMap(countSql.toString());

			int pageCount = Integer.parseInt(totalMap.get("total").toString()) % 10 == 0
					? Integer.parseInt(totalMap.get("total").toString()) / 10
					: Integer.parseInt(totalMap.get("total").toString()) / 10 + 1;
			// compute avg score
			for (Map<String, Object> m : dataList) {
				// get compere whether exist public video
				Map<String, Object> userVideo = this.getMap(
						"SELECT count(t_id) AS total  FROM t_album WHERE t_user_id = ? AND t_file_type=1 AND t_is_private = 0 AND t_is_del = 0 AND t_auditing_type = 1 ",
						Integer.parseInt(m.get("t_id").toString()));
				m.put("t_is_public", Integer.parseInt(userVideo.get("total").toString()) > 0 ? 1 : 0);
				m.put("t_score", avgScore(Integer.parseInt(m.get("t_id").toString())));
			}

			mu = new MessageUtil(1, new HashMap<String,Object>() {{
				put("pageCount", pageCount);
				put("data", dataList);
			}});

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取新主播列表异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	@Cache(cacheKey="#APP_BANNER_LIST_1#",setArguments = false,expire = 3*60L)
	public MessageUtil getIosBannerList() {
		MessageUtil mu = null;
		try {

			String sql = "SELECT t_img_url,t_link_url FROM t_banner WHERE t_is_enable = 0 AND t_type = 1 ";

			List<Map<String, Object>> findBySQLTOMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql);

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(findBySQLTOMap);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取banner列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 主播置顶
	 * @param userId 用户id
	 * @return
	 */
	@Override
	public MessageUtil operatingTopping(int userId) {
		try {
			// 查询主播是否置顶
			List<Map<String, Object>> sqlList = getQuerySqlList("SELECT t_anchor_type FROM t_home_table WHERE t_id = ? and t_operate_status=1 ",userId);
			if (null != sqlList && !sqlList.isEmpty()) {
				return new MessageUtil(-1, "已被置顶,请稍后再试.");
			}
			
			// 查询当前需要调整的用户是否确认为10分钟一起调整的
		     sqlList = getQuerySqlList("SELECT t_anchor_type FROM t_home_table "
		     		+ "WHERE t_id = ? AND (NOW() > DATE_ADD(t_operate_time,INTERVAL 10 MINUTE) or t_operate_time is null );",
					userId);
			if (null == sqlList || sqlList.isEmpty()) {
				return new MessageUtil(-1, "10分钟内只允许置顶一次.");
			}
			
			// 修改同一类别主播的配排序值
			executeSQL("UPDATE t_home_table SET t_sort = t_sort+1 WHERE t_anchor_type = ?  ", sqlList.get(0).get("t_anchor_type"));
			// 修改当前用户的排序值
			executeSQL("UPDATE t_home_table SET t_sort = 1,t_operate_time = ?  WHERE t_id = ?",
					DateUtils.format(new Date(), DateUtils.FullDatePattern), userId);
			return new MessageUtil(1, "操作成功!");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new MessageUtil(0, "系统异常");
	}

	/**
	 * 用户置顶
	 * @param userId 用户id
	 * @return
	 */
	@Override
	public MessageUtil userTopping(int userId) {
		try {
			// 查询主播是否置顶
			List<Map<String, Object>> sqlList = getQuerySqlList("select t_id from t_user where t_id = ? and t_operate_status=1 ",userId);
			if (null != sqlList && !sqlList.isEmpty()) {
				return new MessageUtil(-1, "已被置顶,请稍后再试.");
			}

			// 查询当前需要调整的用户是否确认为10分钟一起调整的
		     sqlList = getQuerySqlList("SELECT * FROM t_user WHERE t_id = ? AND (NOW() > DATE_ADD(t_operate_time,INTERVAL 10 MINUTE) or t_operate_time is null )",
					userId);
			if (null == sqlList || sqlList.isEmpty()) {
				return new MessageUtil(-1, "10分钟内只允许置顶一次.");
			}

			// 修改男用户排序数值
			executeSQL("UPDATE t_user SET t_sort = t_sort+1  WHERE t_sex = 1");
			// 修改当前用户的排序值
			executeSQL("UPDATE t_user SET t_sort = 1,t_operate_time = ?  WHERE t_id = ?",
					DateUtils.format(new Date(), DateUtils.FullDatePattern), userId);
			return new MessageUtil(1, "操作成功!");

		} catch (Exception e) {
			e.printStackTrace();
		}
		return new MessageUtil(0, "系统异常");
	}


	@Override
	public MessageUtil getSelectCharAnother(int userId,int anotherId) {
		MessageUtil mu = null;
		try {
			List<Map<String, Object>> dataList=null;
			// 获取数据
			//上一次选聊获取用户
			String string = redisUtill.get("SELECT_CHAT_ANOTHER_LIST_"+userId);
			if(string==null) {
				string="";
			}
			dataList = getQuerySqlList("SELECT h.*,a.t_addres_url,'' labels FROM t_home_table h "
					+ " inner join t_album a on a.t_user_id=h.t_id and t_file_type=1 and t_auditing_type=1 and t_is_del=0  "
					+ " WHERE h.t_anchor_select=1 and h.t_online!=1  and h.t_id not in ("+string+")  GROUP BY " + 
					  "	h.t_id  ORDER BY  RAND() LIMIT 1 ");
			if(!dataList.isEmpty()) {
				StringBuffer sb = new StringBuffer();
				String[] split = string.split(",");
				List<String> arrList = Arrays.asList(split); 
				List<String> mlist = new ArrayList(arrList);
				mlist.remove(anotherId+"");
				mlist.add(dataList.get(0).get("t_id").toString());
				for (String str : mlist) {
					sb.append(str+",");
				}
				redisUtill.set("SELECT_CHAT_ANOTHER_LIST_"+userId, sb.toString().substring(0, sb.length()-1), 30L);
			}
			// 统计总记录数
			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(dataList.isEmpty()?null:dataList.get(0));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取主播列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}
	
}
