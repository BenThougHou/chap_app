package com.yiliao.service.impl.impl1_6;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.yiliao.domain.Dynamic;
import com.yiliao.domain.MessageEntity;
import com.yiliao.domain.WalletDetail;
import com.yiliao.evnet.PushMesgEvnet;
import com.yiliao.service.GoldComputeService;
import com.yiliao.service.impl.ICommServiceImpl;
import com.yiliao.service.service1_6.DynamicService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RedisUtil;
import com.yiliao.util.SpringConfig;
import com.yiliao.util.cache.CacheRemove;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("dynamicService")
public class DynamicServiceImpl extends ICommServiceImpl implements DynamicService {

	@Autowired
	private RedisUtil redis;

	// 金币扣除辅助service
	private GoldComputeService goldComputeService = (GoldComputeService) SpringConfig.getInstance()
			.getBean("goldComputeService");

	@Override
	public MessageUtil getDynamicList(int userId, int reqType, int page) {
		MessageUtil mu = null;
		try {
			logger.info(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));
			// 查询公开动态
			if (reqType == 0) {
//				Object dynamic = redis.get("APP_ALL_DYNAMIC_LIST_" + reqType);
//				Object dynamic_count = redis.get("APP_ALL_DYNAMIC_LIST_COUNT_" + reqType);
				List<Map<String, Object>> sqlList;
				Map<String, Object> total;
//				if (page != 1 || dynamic == null || null == dynamic_count) {
					// 得到总的记录数
					String cSql = " SELECT COUNT(t_id) AS total FROM t_dynamic WHERE t_auditing_type = 1 AND t_is_del = 0 AND t_is_visible != 1 ";
					total = this.getMap(cSql);

					StringBuffer qSql = new StringBuffer();
					qSql.append(
							"SELECT u.t_id,u.t_handImg,u.t_nickName,u.t_sex,u.t_age,d.t_id AS dynamicId,d.t_address,");
					qSql.append("UNIX_TIMESTAMP(d.t_create_time) AS t_create_time,d.t_content ");
					qSql.append("FROM t_dynamic d LEFT JOIN t_user u ON d.t_user_id = u.t_id ");
					qSql.append("WHERE  d.t_auditing_type = 1 AND d.t_is_del = 0 AND d.t_is_visible != 1 ");
					qSql.append("ORDER BY d.t_create_time DESC LIMIT ?,10 ");
					// 分页查询数据
					sqlList = this.getQuerySqlList(qSql.toString(), (page - 1) * 10);
					logger.info("循环查询开始-->{}", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));
//					if (page == 1) {
//						redis.set("APP_ALL_DYNAMIC_LIST_COUNT_" + reqType, JSON.toJSONString(total), 5L);
//						redis.set("APP_ALL_DYNAMIC_LIST_" + reqType, JSON.toJSONString(sqlList), 5L);
//					}
//				} else {
//					total = JSON.parseObject(dynamic_count.toString(), Map.class);
//					sqlList = JSON.parseObject(dynamic.toString(), ArrayList.class);
//				}

				sqlList.forEach(s -> {
					StringBuffer poolSql = new StringBuffer();
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'1' AS total_type FROM t_praise WHERE t_dynamic_id = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'2' AS total_type  FROM t_praise WHERE t_dynamic_id = ? AND t_praise_user = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'3' AS total_type FROM t_comment WHERE t_dynamic_id = ? and t_is_examine=1 ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'4' AS total_type FROM t_follow WHERE t_follow_id = ? AND t_cover_follow = ? ");

					List<Map<String, Object>> total_count = this.getQuerySqlList(poolSql.toString(), s.get("dynamicId"),
							s.get("dynamicId"), userId, s.get("dynamicId"), userId, s.get("t_id"));

					total_count.forEach(ta -> {
						switch (ta.get("total_type").toString()) {
						case "1":
							s.put("praiseCount", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "2":
							s.put("isPraise", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "3":
							s.put("commentCount", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "4":
							s.put("isFollow", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						}
					});

					List<Map<String, Object>> dynamicFiles = this.getQuerySqlList(
							"SELECT t_id,t_file_type,t_cover_img_url,t_file_url,t_gold,t_is_private,t_video_time FROM t_dynamic_file WHERE t_dynamic_id = ? ",
							s.get("dynamicId"));
					// 判断当前用户是否已经对当前动态的图片或者视频进行消费
					dynamicFiles.forEach(f -> {
						// 图片
						if (f.get("t_file_type").toString().equals("0")) {

							Map<String, Object> consume = this.getMap(
									"SELECT COUNT(t_id) AS total FROM t_order WHERE t_consume_score = ? AND t_consume = ?  AND t_cover_consume = ? AND t_consume_type = ? ;",
									f.get("t_id"), userId, s.get("t_id"), WalletDetail.CHANGE_CATEGOR_DYNAMIC_PHOTO);
							f.put("isConsume", consume.get("total"));
						} else { // 视频
							Map<String, Object> consume = this.getMap(
									"SELECT COUNT(t_id) AS total FROM t_order WHERE t_consume_score = ? AND t_consume = ? AND t_cover_consume = ? AND t_consume_type = ? ;",
									f.get("t_id"), userId, s.get("t_id"), WalletDetail.CHANGE_CATEGOR_DYNAMIC_VIDEO);
							f.put("isConsume", consume.get("total"));
						}

					});
					s.put("dynamicFiles", dynamicFiles);

				});

				mu = new MessageUtil(1, new HashMap<String, Object>() {
					{
						put("pageCount",
								Integer.parseInt(total.get("total").toString()) % 10 == 0
										? Integer.parseInt(total.get("total").toString()) / 10
										: Integer.parseInt(total.get("total").toString()) / 10 + 1);
						put("data", sqlList);
					}
				});

				logger.info("循环查询结束-->{}", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));

			} else { // 查询关注动态

				List<Map<String, Object>> sqlList;
				Map<String, Object> total;

					total = this.getMap(
							"SELECT COUNT(t_id) AS total FROM (SELECT t_cover_follow FROM t_follow  WHERE t_follow_id = ?) aa LEFT JOIN t_dynamic d ON aa.t_cover_follow = d.t_user_id WHERE d.t_auditing_type = 1 AND d.t_is_del = 0 ",
							userId);
					StringBuffer qSql = new StringBuffer();
					qSql.append(
							"SELECT  u.t_id,u.t_handImg,u.t_nickName,u.t_sex,u.t_age,d.t_id AS dynamicId,UNIX_TIMESTAMP(d.t_create_time) AS t_create_time,d.t_content,d.t_address ");
					qSql.append(
							"FROM (SELECT t_cover_follow FROM t_follow  WHERE t_follow_id = ? ) aa LEFT JOIN t_dynamic d ON aa.t_cover_follow = d.t_user_id ");
					qSql.append("LEFT JOIN t_user u ON d.t_user_id = u.t_id ");
					qSql.append("WHERE d.t_auditing_type = 1 AND d.t_is_del = 0  ");
					qSql.append("ORDER BY d.t_create_time DESC LIMIT ?,10 ");
					sqlList = this.getQuerySqlList(qSql.toString(), userId, (page - 1) * 10);
				sqlList.forEach(s -> {
					StringBuffer poolSql = new StringBuffer();
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'1' AS total_type FROM t_praise WHERE t_dynamic_id = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'2' AS total_type  FROM t_praise WHERE t_dynamic_id = ? AND t_praise_user = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'3' AS total_type FROM t_comment WHERE t_dynamic_id = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'4' AS total_type FROM t_follow WHERE t_follow_id = ? AND t_cover_follow = ? ");

					List<Map<String, Object>> total_count = this.getQuerySqlList(poolSql.toString(), s.get("dynamicId"),
							s.get("dynamicId"), userId, s.get("dynamicId"), userId, s.get("t_id"));

					total_count.forEach(ta -> {
						switch (ta.get("total_type").toString()) {
						case "1":
							s.put("praiseCount", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "2":
							s.put("isPraise", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "3":
							s.put("commentCount", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "4":
							s.put("isFollow", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						}
					});

					// 获取该动态是否有图片或者视频文件
					List<Map<String, Object>> dynamicFiles = this.getQuerySqlList(
							"SELECT t_id,t_file_type,t_cover_img_url,t_file_url,t_gold,t_is_private FROM t_dynamic_file WHERE t_dynamic_id = ? ",
							s.get("dynamicId"));
					// 判断当前用户是否已经对当前动态的图片或者视频进行消费
					dynamicFiles.forEach(f -> {
						Map<String, Object> consume = this.getMap(
								"SELECT COUNT(t_id) AS total FROM t_order WHERE t_consume_score = ? AND t_consume = ? ;",
								f.get("t_id"), userId);
						f.put("isConsume", consume.get("total"));
					});
					s.put("dynamicFiles", dynamicFiles);
				});

				mu = new MessageUtil(1, new HashMap<String, Object>() {
					{
						put("pageCount",
								Integer.parseInt(total.get("total").toString()) % 10 == 0
										? Integer.parseInt(total.get("total").toString()) / 10
										: Integer.parseInt(total.get("total").toString()) / 10 + 1);
						put("data", sqlList);
					}
				});
			}
			logger.info(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取动态列表异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 发布动态
	 */
	@Override
	public MessageUtil releaseDynamic(int userId, String title, String content, String address, int isVisible,
			JSONArray files) {
		MessageUtil mu = null;
		try {
			// 保存动态数据
			String inSql = "INSERT INTO t_dynamic (t_user_id, t_title, t_content,t_address,t_is_visible, t_see_count, t_is_del, t_auditing_type,t_create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?); ";

			int parKey = this.getFinalDao().getIEntitySQLDAO().saveData(inSql, userId, title, content, address,
					isVisible, 0, 0, 0, DateUtils.format(new Date(), DateUtils.FullDatePattern));

			// 获取秘钥信息
//			String qSql = " SELECT t_app_id,t_secret_id,t_secret_key,t_bucket FROM t_object_storage ";
//			Map<String, Object> querySqlList = this.getMap(qSql);

			String dynamicFileSql = "INSERT INTO t_dynamic_file (t_dynamic_id, t_file_type,t_cover_img_url,t_file_url, t_gold, t_file_id, t_is_private,t_is_del,t_see_count,t_create_time,t_video_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?);";

			for (Object obj : files) {
				JSONObject s = JSONObject.fromObject(obj);
				// 判断文件是图片还是视频 0.图片
				if (s.getString("fileType").equals("0")) {
//					String[] str = { s.get("fileUrl").toString() };

					// 调用鉴黄系统 鉴定封面是否违规
//					Map<String, Object> imagePorn = CheckImgUtil.imagePorn(str, querySqlList.get("t_app_id").toString(),
//							querySqlList.get("t_secret_id").toString(), querySqlList.get("t_secret_key").toString(),
//							querySqlList.get("t_bucket").toString());

//					List<String> pornUrl = (List<String>) imagePorn.get("pornUrl");
//					if (pornUrl.isEmpty()) {
						this.executeSQL(dynamicFileSql, parKey, Dynamic.FILE_TYPE_PHOTO, null, s.getString("fileUrl"),
								new BigDecimal(s.getString("gold")).setScale(2, BigDecimal.ROUND_DOWN), null,
								s.get("t_is_private"), 0, DateUtils.format(new Date(), DateUtils.FullDatePattern), 0);
//					}
				} else { // 视频
					this.executeSQL(dynamicFileSql, parKey, Dynamic.FILE_TYPE_VIDEO, s.getString("t_cover_img_url"),
							s.getString("fileUrl"),
							new BigDecimal(s.getString("gold")).setScale(2, BigDecimal.ROUND_DOWN),
							s.getString("fileId"), s.get("t_is_private"), 0,
							DateUtils.format(new Date(), DateUtils.FullDatePattern), s.getString("t_video_time"));
					// 调起鉴黄设置
//					try {
//						VidelSingUtil.yellowing(s.getString("fileId"), querySqlList.get("t_secret_id").toString(),
//								querySqlList.get("t_secret_key").toString());
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
				}
			}

			// 异步通知
			this.applicationContext
					.publishEvent(new PushMesgEvnet(new MessageEntity(userId, "您发布了新的动态!", 0, new Date())));

			mu = new MessageUtil(1, "动态已发布!等待审核!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}发布动态异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 评论动态
	 */
	@Override
	public MessageUtil discussDynamic(int userId, int coverUserId, String comment, int dynamicId) {
		MessageUtil mu = null;
		try {

			String inSql = " INSERT INTO t_comment (t_com_user_id, t_cover_user_id, t_com_type, t_comment, t_create_time, t_is_examine, t_dynamic_id,t_is_read) VALUES (?, ?, ?, ?, ?, ?, ?, 0);";
			// 标示对动态进行评论
			if (coverUserId == 0) {
				this.executeSQL(inSql, userId, coverUserId, 0, comment,
						DateUtils.format(new Date(), DateUtils.FullDatePattern), 0, dynamicId);
				// 查询动态发布人
//				Map<String, Object> map = this.getMap("SELECT t_user_id FROM t_dynamic WHERE t_id = ? ", dynamicId);
				// 增加评论记录数
//				saveCommentCount(Integer.parseInt(map.get("t_user_id").toString()));

			} else { // 对评论进行回复
				this.executeSQL(inSql, userId, coverUserId, 1, comment,
						DateUtils.format(new Date(), DateUtils.FullDatePattern), 0, dynamicId);
				// 增加评论记录数
//				saveCommentCount(userId);
			}
			mu = new MessageUtil(1, "评论成功!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}评论异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 增加评论记录数
	 * 
	 * @param userId
	 */
	private void saveCommentCount(int userId) {

		// 根据用户编号得到当前用户是否存在记录数
		String qSql = "SELECT t_id FROM t_comment_count WHERE t_user_id = ? ";
		List<Map<String, Object>> sqlList = this.getQuerySqlList(qSql, userId);
		// 如果为空 新增操作 否则更新操作
		if (sqlList.isEmpty()) {
			String inSql = " INSERT INTO t_comment_count (t_user_id, t_mesg_count, t_create_time) VALUES (?, ?, ?) ";
			this.executeSQL(inSql, userId, 1, DateUtils.format(new Date(), DateUtils.FullDatePattern));
		} else {
			String uSql = " UPDATE t_comment_count SET t_mesg_count=t_mesg_count + 1, t_create_time=? WHERE t_user_id = ? ";
			this.executeSQL(uSql, DateUtils.format(new Date(), DateUtils.FullDatePattern), userId);
		}
	}

	/**
	 * 删除评论记录数
	 * 
	 * @param userId
	 */
	public void delCommentCount(int userId) {
		try {
			String dSql = " DELETE FROM t_comment_count WHERE t_user_id = ? ";
			this.executeSQL(dSql, userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用户点赞
	 */
	@Override
	public MessageUtil giveTheThumbsUp(int userId, int dynamicId) {
		MessageUtil mu = null;
		try {

			// 获取有多少点赞
			List<Map<String, Object>> sqlList = this.getQuerySqlList(
					"SELECT *  FROM t_praise WHERE t_dynamic_id = ? AND t_praise_user = ?; ", dynamicId, userId);

			if (sqlList.isEmpty()) {
				String inSql = " INSERT INTO t_praise (t_praise_user, t_dynamic_id, t_create_time) VALUES (?, ?, ?); ";
				this.executeSQL(inSql, userId, dynamicId, DateUtils.format(new Date(), DateUtils.FullDatePattern));
			}
			mu = new MessageUtil(1, "点赞成功!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}点赞异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 获取动态明细
	 */
	@Override
	public MessageUtil getDynamicDetails(int userId, int dynamicId) {
		MessageUtil mu = null;
		try {

			StringBuffer qSql = new StringBuffer();
			qSql.append("SELECT u.t_nickName,u.t_id,u.t_phone,u.t_sex,t_age,d.t_id AS dynamicId,");
			qSql.append("d.t_content,UNIX_TIMESTAMP(d.t_create_time) AS t_create_time ");
			qSql.append("FROM t_dynamic d LEFT JOIN t_user u ON d.t_user_id = u.t_id ");
			qSql.append("WHERE d.t_id = ? ");
			// 获取动态
			Map<String, Object> dynamic = this.getMap(qSql.toString(), dynamicId);

			// 获取该动态是否有图片或者视频文件
			List<Map<String, Object>> dynamicFiles = this.getQuerySqlList(
					"SELECT t_id,t_file_type,t_file_url,t_gold,t_is_private FROM t_dynamic_file WHERE t_dynamic_id = ? ",
					dynamicId);
			// 判断当前用户是否已经对当前动态的图片或者视频进行消费
			dynamicFiles.forEach(f -> {
				Map<String, Object> consume = this.getMap(
						"SELECT COUNT(t_id) AS total FROM t_order WHERE t_consume_score = ? AND t_consume = ? ;",
						f.get("t_id"), userId);
				f.put("isConsume", consume.get("total"));
			});
			dynamic.put("dynamicFiles", dynamicFiles);

			// 获取有多少点赞
			Map<String, Object> praise = this.getMap("SELECT COUNT(t_id) AS total FROM t_praise WHERE t_dynamic_id = ?",
					dynamicId);
			dynamic.put("praiseCount", praise.get("total"));

			// 获取有多少评论
			Map<String, Object> commentCount = this
					.getMap("SELECT COUNT(t_id) AS total FROM t_comment WHERE t_dynamic_id = ? ", dynamicId);
			dynamic.put("commentCount", commentCount.get("total"));

			// 获取当前用户是否关注动态用户
			Map<String, Object> follow = this.getMap(
					"SELECT COUNT(t_id) AS total FROM t_follow WHERE t_follow_id = ? AND t_cover_follow = ? ", userId,
					dynamic.get("t_id"));
			dynamic.put("isFollow", follow.get("total"));

			if (null == dynamic.get("t_nickName")) {
				dynamic.put("t_nickName", "聊友:"
						+ dynamic.get("t_phone").toString().substring(dynamic.get("t_phone").toString().length() - 4));
			}

			dynamic.remove("t_phone");

			// 返回数据
			mu = new MessageUtil(1, dynamic);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取动态明细异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 付费查看动态私密文件
	 */
	@Override
	public synchronized MessageUtil dynamicPay(int userId, int fileId) {
		MessageUtil mu = null;
		try {
			// 判断当前用户是否是VIP
			if (!getUserIsVip(userId)) {
				// 获取动态发布人
				Map<String, Object> dynamicUser = this.getMap(
						"SELECT d.t_user_id,f.t_file_type,f.t_gold FROM t_dynamic_file f LEFT JOIN t_dynamic d ON f.t_dynamic_id = d.t_id WHERE f.t_id = ? ",
						fileId);
				// 判断用户是否已经支付过了
				if (!queryOrderExits(userId, Integer.parseInt(dynamicUser.get("t_user_id").toString()), fileId,
						Integer.parseInt(dynamicUser.get("t_file_type").toString()) == 0
								? WalletDetail.CHANGE_CATEGOR_DYNAMIC_PHOTO
								: WalletDetail.CHANGE_CATEGOR_DYNAMIC_VIDEO)) {

					// 插入订单 切返回订单编号
					int orderId = this.getOrderId();
					// 扣除消费者需要消费的金币
					if (goldComputeService.userConsume(userId,
							Integer.parseInt(dynamicUser.get("t_file_type").toString()) == 0
									? WalletDetail.CHANGE_CATEGOR_DYNAMIC_PHOTO
									: WalletDetail.CHANGE_CATEGOR_DYNAMIC_VIDEO,
							new BigDecimal(dynamicUser.get("t_gold").toString()), orderId)) {

						this.saveOrder(orderId, userId, Integer.parseInt(dynamicUser.get("t_user_id").toString()),
								fileId,
								Integer.parseInt(dynamicUser.get("t_file_type").toString()) == 0
										? WalletDetail.CHANGE_CATEGOR_DYNAMIC_PHOTO
										: WalletDetail.CHANGE_CATEGOR_DYNAMIC_VIDEO,
								new BigDecimal(dynamicUser.get("t_gold").toString()));
						// 分配用户的消费的金币
						goldComputeService.distribution(new BigDecimal(dynamicUser.get("t_gold").toString()), userId,
								Integer.parseInt(dynamicUser.get("t_user_id").toString()),
								Integer.parseInt(dynamicUser.get("t_file_type").toString()) == 0
										? WalletDetail.CHANGE_CATEGOR_DYNAMIC_PHOTO
										: WalletDetail.CHANGE_CATEGOR_DYNAMIC_VIDEO,
								orderId);
						// 查看该资料是否还需需要进行存储
						savePrivate(userId, fileId);

						mu = new MessageUtil();
						mu.setM_istatus(1);
						mu.setM_strMessage("消费成功!");
					} else {
						mu = new MessageUtil();
						mu.setM_istatus(-1);
						mu.setM_strMessage("余额不足!请充值.");
					}
				} else {
					return new MessageUtil(2, "已经消费过");
				}
			} else {
				return new MessageUtil(2, "VIP用户");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}付费查看文件异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 获取用户是否是VIP用户
	 * 
	 * @param userId
	 * @return
	 */
	private boolean getUserIsVip(int userId) {
		try {

			String vSql = "SELECT t_is_vip FROM t_user WHERE t_id = ?";

			Map<String, Object> vipMap = this.getMap(vSql, userId);

			if ("0".equals(vipMap.get("t_is_vip").toString())) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取用户是否是VIP异常", e);
		}

		return false;
	}

	/**
	 * 查询本次订单是否已经存在支付过的类型
	 * 
	 * @param consume
	 * @param cover_consume
	 * @param consume_score
	 * @param consume_type
	 * @return fals没有记录 true已有记录
	 */
	private boolean queryOrderExits(int consume, int cover_consume, int consume_score, int consume_type) {
		String sql = "SELECT * FROM t_order WHERE t_consume = ?  AND t_cover_consume = ? AND t_consume_type = ? ";

		if (consume_type == WalletDetail.CHANGE_CATEGOR_DYNAMIC_PHOTO
				|| consume_type == WalletDetail.CHANGE_CATEGOR_DYNAMIC_VIDEO) {
			sql = sql + " AND t_consume_score =  " + consume_score;
		}

		List<Map<String, Object>> dataMap = this.getQuerySqlList(sql, consume, cover_consume, consume_type);

		return null == dataMap ? false : dataMap.isEmpty() ? false : true;
	}

	/**
	 * 插入订单记录
	 * 
	 * @param t_id
	 * @param consume
	 * @param cover_consume
	 * @param consume_score
	 * @param consume_type
	 * @param amount
	 */
	private int saveOrder(int t_id, int consume, int cover_consume, int consume_score, int consume_type,
			BigDecimal amount) {

		String sql = "INSERT INTO t_order (t_id,t_consume, t_cover_consume, t_consume_type, t_consume_score, t_amount, t_create_time) VALUES (?, ?, ?, ?, ?, ?, ?)";

		return this.getFinalDao().getIEntitySQLDAO().saveData(sql, t_id, consume, cover_consume, consume_type,
				consume_score, amount, DateUtils.format(new Date(), DateUtils.FullDatePattern));
	}

	/**
	 * 生成下一条消费的订单Id
	 * 
	 * @return
	 */
	public int getOrderId() {
		List<Map<String, Object>> arr = this.getQuerySqlList("SELECT t_id FROM t_order ORDER BY t_id DESC LIMIT 1;");
		return arr.isEmpty() ? 1 : (Integer) arr.get(0).get("t_id") + 1;
	}

	/**
	 * 用户私藏 用户消费的
	 * 
	 * @param consume
	 * @param dynamicId
	 */
	private void savePrivate(int consume, int dynamicId) {
		// 查询需要保存的数据是否已经存在了
		String sql = "SELECT * FROM t_private_collection WHERE t_user_id = ? AND t_album_id = ?";
		List<Map<String, Object>> dataLsit = this.getQuerySqlList(sql, consume, dynamicId);
		// 判断该数据是否已经存在
		if (null == dataLsit || !dataLsit.isEmpty()) {
			// 保存数据
			sql = " INSERT INTO t_private_collection (t_user_id,t_album_id) VALUES ( ?, ?)";
			this.executeSQL(sql, consume, dynamicId);
		}
	}

	/**
	 * 删除动态
	 */
	@Override
	@CacheRemove(cacheKey = "#APP_ALL_DYNAMIC_LIST#")
	public MessageUtil delDynamic(int userId, int dynamicId) {
		MessageUtil mu = null;
		try {

			String upSql = " UPDATE t_dynamic SET t_is_del = 1 WHERE t_id = ? AND t_user_id = ?  ";
			this.executeSQL(upSql, dynamicId, userId);
			mu = new MessageUtil(1, "动态已删除!");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}删除动态异常!", userId);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 获取评论列表
	 */
	@Override
	public MessageUtil getCommentList(int userId, int dynamicId, int page) {
		MessageUtil mu = null;
		try {
			// 查询总记录数
			Map<String, Object> total = this.getMap(
					"SELECT COUNT(t_id) AS total FROM t_comment WHERE t_dynamic_id = ? AND t_is_examine = 1 ",
					dynamicId);
			// 计算出总的多少页
			int pageCount = Integer.parseInt(total.get("total").toString()) % 10 == 0
					? Integer.parseInt(total.get("total").toString()) / 10
					: Integer.parseInt(total.get("total").toString()) / 10 + 1;

			StringBuffer qSql = new StringBuffer();
			qSql.append("SELECT * FROM ( ");
			qSql.append(
					"SELECT * FROM (SELECT u.t_id,u.t_handImg,u.t_nickName,u.t_phone,u.t_sex,UNIX_TIMESTAMP(c.t_create_time) AS t_create_time,c.t_id AS comId,c.t_comment,'1' AS comType ");
			qSql.append("FROM t_comment c LEFT JOIN t_user u ON c.t_com_user_id = u.t_id ");
			qSql.append(
					"WHERE t_dynamic_id = ? AND t_is_examine = 1 AND t_com_user_id = ? ORDER BY c.t_create_time DESC) t1 ");
			qSql.append("UNION ");
			qSql.append(
					"SELECT * FROM (SELECT u.t_id,u.t_handImg,u.t_nickName,u.t_phone,u.t_sex,UNIX_TIMESTAMP(c.t_create_time) AS t_create_time,c.t_id AS comId,c.t_comment,'2' AS comType ");
			qSql.append("FROM t_comment c LEFT JOIN t_user u ON c.t_com_user_id = u.t_id ");
			qSql.append(
					"WHERE t_dynamic_id = ? AND t_is_examine = 1 AND t_com_user_id <> ? ORDER BY c.t_create_time DESC ) t2 ");
			qSql.append(") aa LIMIT ?,10");

			List<Map<String, Object>> sqlList = this.getQuerySqlList(qSql.toString(), dynamicId, userId, dynamicId,
					userId, (page - 1) * 10);

			sqlList.forEach(s -> {
				if (null == s.get("t_nickName")) {
					s.put("t_nickName",
							"聊友:" + s.get("t_phone").toString().substring(s.get("t_phone").toString().length() - 4));
				}
				s.remove("t_phone");
			});

			// 当自己查看评论列表的时候 修改所有已审核的评论为已读状态
			this.executeSQL(
					"UPDATE t_comment SET t_is_read= 1 WHERE t_cover_user_id = ? AND t_dynamic_id = ? AND t_is_examine = 1 ",
					userId, dynamicId);

			mu = new MessageUtil(1, new HashMap<String, Object>() {
				{
					put("pageCount", pageCount);
					put("data", sqlList);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取评论列表异常!", userId);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 删除评论
	 */
	@Override
	@CacheRemove(cacheKey = "#APP_ALL_DYNAMIC_LIST#")
	public MessageUtil delComment(int userId, int commentId) {
		MessageUtil mu = null;
		try {

			String delSql = "DELETE FROM t_comment WHERE t_com_user_id = ? AND t_id = ?  ";
			int executeSQL = this.executeSQL(delSql, userId, commentId);
			if (executeSQL > 0) {
				mu = new MessageUtil(1, "已删除!");
			} else {
				mu = new MessageUtil(-1, "未找到要删除的记录!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}删除评论异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 发送通知
	 */
	@Override
	public void sendSocketNotice(int userId) {
		try {

//			logger.info("---动态进行推送---");
//			IoSession session = UserIoSession.getInstance().getMapIoSession(userId);
//			if (null != session) {
//				DynamicRes dr = new DynamicRes();
//				dr.setMid(Mid.dynamicRes);
//				session.write(JSONObject.fromObject(dr).toString());
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取点赞列表
	 */
	@Override
	public MessageUtil getPraiseList(int userId, int dynamicId, int page) {
		MessageUtil mu = null;
		try {
			// 获取多少记录
			Map<String, Object> total = this
					.getMap("SELECT COUNT(t_id) AS total FROM t_praise  WHERE t_dynamic_id = ? ", dynamicId);

			Integer pageSize = Integer.parseInt(total.get("total").toString());

			// 查询记录 每页记录 20条
			StringBuffer qSql = new StringBuffer();
			qSql.append("SELECT u.t_id,u.t_handImg,u.t_nickName,u.t_phone ");
			qSql.append("FROM t_praise p LEFT JOIN t_user u ON p.t_praise_user = u.t_id ");
			qSql.append("WHERE t_dynamic_id = ? LIMIT ?,20 ");

			List<Map<String, Object>> sqlList = this.getQuerySqlList(qSql.toString(), dynamicId, (page - 1) * 10);

			sqlList.forEach(s -> {
				if (null == s.get("t_nickName")) {
					s.put("t_nickName",
							"聊友:" + s.get("t_phone").toString().substring(s.get("t_phone").toString().length() - 4));
				}
				s.remove("t_phone");
			});

			mu = new MessageUtil(1, new HashMap<String, Object>() {
				{
					put("pageCount", pageSize % 20 == 0 ? pageSize / 20 : pageSize / 20 + 1);
					put("data", sqlList);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取点赞列表异常!", userId, e);
			mu = new MessageUtil(0, "查询异常!");
		}
		return mu;
	}

	/**
	 * 获取用户的通知消息
	 */
	@Override
	public MessageUtil getUserDynamicNotice(int userId) {
		MessageUtil mu = null;
		try {

			// 获取当前用户是用户还是主播
			Map<String, Object> userRole = this.getMap("SELECT t_role FROM t_user WHERE t_id = ? ", userId);
			// 普通用户
			if (0 == Integer.parseInt(userRole.get("t_role").toString())) {
				Map<String, Object> total = this.getMap(
						"SELECT COUNT(t_id) AS total FROM (SELECT t_cover_follow FROM t_follow  WHERE t_follow_id = ?) aa LEFT JOIN t_dynamic d ON aa.t_cover_follow = d.t_user_id WHERE d.t_auditing_type = 1 AND d.t_is_del = 0 AND d.t_is_visible = 0",
						userId);

				mu = new MessageUtil(1, Arrays.asList(new HashMap<String, Object>() {
					{
						put("t_role", userRole.get("t_role"));
						put("t_mesg_count", total.get("total"));
					}
				}));
				// 主播
			} else {

				List<Map<String, Object>> sqlList = this.getQuerySqlList(
						"SELECT u.t_role,c.t_mesg_count FROM t_comment_count c LEFT JOIN  t_user u ON c.t_user_id = u.t_id WHERE c.t_user_id = ? ",
						userId);

				mu = new MessageUtil(1, sqlList);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取新的动态消息异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getUserNewComment(int userId) {
		MessageUtil mu = null;
		try {

			StringBuffer body = new StringBuffer();
			body.append("SELECT u.t_id,u.t_nickName,u.t_phone,u.t_handImg,c.t_comment,");
			body.append("UNIX_TIMESTAMP(c.t_create_time) AS t_create_time,c.t_dynamic_id ");
			body.append("FROM t_comment c LEFT JOIN t_user u ON c.t_com_user_id = u.t_id ");
			body.append("WHERE c.t_is_examine = 1 AND c.t_is_read = 0 AND c.t_cover_user_id = ? ");
			body.append("ORDER BY c.t_create_time DESC ");

			List<Map<String, Object>> sqlList = this.getQuerySqlList(body.toString(), userId);

			sqlList.forEach(s -> {
				// 根据动态编号得到动态到底是文字还是图片
				List<Map<String, Object>> files = this.getQuerySqlList(
						"SELECT t_file_type,t_cover_img_url,t_file_url FROM t_dynamic_file WHERE t_dynamic_id = ? LIMIT 1;",
						s.get("t_dynamic_id"));
				// 文件类型动态
				if (!files.isEmpty()) {
					s.put("dynamic_type", files.get(0).get("t_file_type"));
					s.put("t_cover_img_url", files.get(0).get("t_cover_img_url"));
					s.put("dynamic_com", files.get(0).get("t_file_url"));
				} else {
					// 文字内容动态
					List<Map<String, Object>> sql_com = this
							.getQuerySqlList("SELECT t_content FROM t_dynamic WHERE t_id = ? ", s.get("t_dynamic_id"));

					if (!sql_com.isEmpty()) {
						s.put("dynamic_type", -1);
						s.put("dynamic_com", sql_com.get(0).get("t_content"));
					}
				}

				if (null == s.get("t_nickName")) {
					s.put("t_nickName",
							"聊友:" + s.get("t_phone").toString().substring(s.get("t_phone").toString().length() - 4));
				}
				s.remove("t_phone");

				// 当自己查看评论列表的时候 修改所有已审核的评论为已读状态
				this.executeSQL(
						"UPDATE t_comment SET t_is_read = 1 WHERE t_cover_user_id = ? AND t_dynamic_id = ? AND t_is_examine = 1 ",
						userId, s.get("t_dynamic_id"));

			});

			// 删除评论统计表中的数据
			this.executeSQL(" DELETE FROM t_comment_count WHERE t_user_id = ? ", userId);

			mu = new MessageUtil(1, sqlList);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("{}获取最新评论消息异常!", userId, e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 获取自己的动态列表
	 */
	@Override
	public MessageUtil getOwnDynamicList(int userId, int page) {

		try {

			String cSql = " SELECT COUNT(t_id) AS total FROM t_dynamic WHERE t_auditing_type = 1 AND t_is_del = 0 AND t_user_id = ? ";
			Map<String, Object> total = this.getMap(cSql, userId);

			StringBuffer qSql = new StringBuffer();
			qSql.append("SELECT u.t_id,u.t_handImg,u.t_nickName,u.t_sex,u.t_age,d.t_id AS dynamicId,d.t_address,");
			qSql.append("UNIX_TIMESTAMP(d.t_create_time) AS t_create_time,d.t_content ");
			qSql.append("FROM t_dynamic d LEFT JOIN t_user u ON d.t_user_id = u.t_id ");
			qSql.append("WHERE  d.t_auditing_type = 1 AND d.t_is_del = 0 AND d.t_user_id = ? ");
			qSql.append("ORDER BY d.t_create_time DESC LIMIT ?,10 ");
			// 分页查询数据
			List<Map<String, Object>> sqlList = this.getQuerySqlList(qSql.toString(), userId, (page - 1) * 10);

			logger.info("循环查询开始-->{}", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));

			sqlList.forEach(s -> {

				StringBuffer poolSql = new StringBuffer();
				poolSql.append("SELECT COUNT(t_id) AS total,'1' AS total_type FROM t_praise WHERE t_dynamic_id = ? ");
				poolSql.append("UNION ");
				poolSql.append(
						"SELECT COUNT(t_id) AS total,'2' AS total_type  FROM t_praise WHERE t_dynamic_id = ? AND t_praise_user = ? ");
				poolSql.append("UNION ");
				poolSql.append("SELECT COUNT(t_id) AS total,'3' AS total_type FROM t_comment WHERE t_dynamic_id = ? ");
				poolSql.append("UNION ");
				poolSql.append(
						"SELECT COUNT(t_id) AS total,'4' AS total_type FROM t_follow WHERE t_follow_id = ? AND t_cover_follow = ? ");

				List<Map<String, Object>> total_count = this.getQuerySqlList(poolSql.toString(), s.get("dynamicId"),
						s.get("dynamicId"), userId, s.get("dynamicId"), userId, s.get("t_id"));

				total_count.forEach(ta -> {
					switch (ta.get("total_type").toString()) {
					case "1":
						s.put("praiseCount", null == ta.get("total") ? 0 : ta.get("total"));
						break;
					case "2":
						s.put("isPraise", null == ta.get("total") ? 0 : ta.get("total"));
						break;
					case "3":
						s.put("commentCount", null == ta.get("total") ? 0 : ta.get("total"));
						break;
					case "4":
						s.put("isFollow", null == ta.get("total") ? 0 : ta.get("total"));
						break;
					}
				});

				List<Map<String, Object>> dynamicFiles = this.getQuerySqlList(
						"SELECT t_id,t_file_type,t_cover_img_url,t_file_url,t_gold,t_is_private,t_video_time FROM t_dynamic_file WHERE t_dynamic_id = ? ",
						s.get("dynamicId"));
				// 判断当前用户是否已经对当前动态的图片或者视频进行消费
				dynamicFiles.forEach(f -> {
					Map<String, Object> consume = this.getMap(
							"SELECT COUNT(t_id) AS total FROM t_order WHERE t_consume_score = ? AND t_consume = ? ;",
							f.get("t_id"), userId);
					f.put("isConsume", consume.get("total"));
				});
				s.put("dynamicFiles", dynamicFiles);

			});

			return new MessageUtil(1, new HashMap<String, Object>() {
				{
					put("pageCount",
							Integer.parseInt(total.get("total").toString()) % 10 == 0
									? Integer.parseInt(total.get("total").toString()) / 10
									: Integer.parseInt(total.get("total").toString()) / 10 + 1);
					put("data", sqlList);
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("{}获取自己的评论异常!", userId, e);
			return new MessageUtil(0, "程序异常!");
		}
	}

	@Override
	public MessageUtil getPrivateDynamicList(int userId, int coverUserId, int reqType, int page) {
		MessageUtil mu = null;
		try {

			logger.info(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));
			// 查询公开动态
			if (reqType == 0) {
				// 得到总的记录数
				String cSql = " SELECT COUNT(t_id) AS total FROM t_dynamic WHERE t_auditing_type = 1 AND t_is_del = 0 AND t_is_visible = 0 AND t_user_id = ? ";
				Map<String, Object> total = this.getMap(cSql, coverUserId);

				StringBuffer qSql = new StringBuffer();
				qSql.append("SELECT u.t_id,u.t_handImg,u.t_nickName,u.t_sex,u.t_age,d.t_id AS dynamicId,d.t_address,");
				qSql.append("UNIX_TIMESTAMP(d.t_create_time) AS t_create_time,d.t_content ");
				qSql.append("FROM t_dynamic d LEFT JOIN t_user u ON d.t_user_id = u.t_id ");
				qSql.append(
						"WHERE  d.t_auditing_type = 1 AND d.t_is_del = 0 AND d.t_is_visible = 0 AND d.t_user_id = ? ");
				qSql.append("ORDER BY d.t_create_time DESC LIMIT ?,10 ");
				// 分页查询数据
				List<Map<String, Object>> sqlList = this.getQuerySqlList(qSql.toString(), coverUserId, (page - 1) * 10);

				logger.info("循环查询开始-->{}", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));

				sqlList.forEach(s -> {

					StringBuffer poolSql = new StringBuffer();
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'1' AS total_type FROM t_praise WHERE t_dynamic_id = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'2' AS total_type  FROM t_praise WHERE t_dynamic_id = ? AND t_praise_user = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'3' AS total_type FROM t_comment WHERE t_dynamic_id = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'4' AS total_type FROM t_follow WHERE t_follow_id = ? AND t_cover_follow = ? ");

					List<Map<String, Object>> total_count = this.getQuerySqlList(poolSql.toString(), s.get("dynamicId"),
							s.get("dynamicId"), userId, s.get("dynamicId"), userId, s.get("t_id"));

					total_count.forEach(ta -> {
						switch (ta.get("total_type").toString()) {
						case "1":
							s.put("praiseCount", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "2":
							s.put("isPraise", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "3":
							s.put("commentCount", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "4":
							s.put("isFollow", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						}
					});

					List<Map<String, Object>> dynamicFiles = this.getQuerySqlList(
							"SELECT t_id,t_file_type,t_cover_img_url,t_file_url,t_gold,t_is_private,t_video_time FROM t_dynamic_file WHERE t_dynamic_id = ? ",
							s.get("dynamicId"));
					// 判断当前用户是否已经对当前动态的图片或者视频进行消费
					dynamicFiles.forEach(f -> {
						Map<String, Object> consume = this.getMap(
								"SELECT COUNT(t_id) AS total FROM t_order WHERE t_consume_score = ? AND t_consume = ? ;",
								f.get("t_id"), userId);
						f.put("isConsume", consume.get("total"));
					});
					s.put("dynamicFiles", dynamicFiles);

				});

				mu = new MessageUtil(1, new HashMap<String, Object>() {
					{
						put("pageCount",
								Integer.parseInt(total.get("total").toString()) % 10 == 0
										? Integer.parseInt(total.get("total").toString()) / 10
										: Integer.parseInt(total.get("total").toString()) / 10 + 1);
						put("data", sqlList);
					}
				});

				logger.info("循环查询结束-->{}", DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));

			} else { // 查询关注动态

				// 获取当前人是否关注主播
				Map<String, Object> followUser = this.getMap(
						"SELECT COUNT(t_id) AS total FROM t_follow WHERE t_follow_id = ? AND t_cover_follow = ? ",
						userId, coverUserId);
				// 如果未关注主播 直接返回空数据
				if ("0".equals(followUser.get("total").toString())) {
					return new MessageUtil(1, new HashMap<String, Object>() {
						{
							put("pageCount", 0);
							put("data", new ArrayList<Map<String, Object>>());
						}
					});
				}

				Map<String, Object> total = this.getMap(
						"SELECT COUNT(t_id) AS total FROM t_dynamic d WHERE d.t_auditing_type = 1 AND d.t_is_del = 0 AND d.t_is_visible = 0 AND t_user_id = ? ",
						coverUserId);

				StringBuffer qSql = new StringBuffer();
				qSql.append(
						"SELECT  u.t_id,u.t_handImg,u.t_nickName,u.t_sex,u.t_age,d.t_id AS dynamicId,UNIX_TIMESTAMP(d.t_create_time) AS t_create_time,d.t_content,d.t_address ");
				qSql.append("FROM  t_dynamic d LEFT JOIN t_user u ON d.t_user_id = u.t_id ");
				qSql.append(
						"WHERE d.t_auditing_type = 1 AND d.t_is_del = 0 AND d.t_is_visible = 1 AND d.t_user_id = ? ");
				qSql.append("ORDER BY d.t_create_time DESC LIMIT ?,10 ");

				List<Map<String, Object>> sqlList = this.getQuerySqlList(qSql.toString(), coverUserId, (page - 1) * 10);

				sqlList.forEach(s -> {

					StringBuffer poolSql = new StringBuffer();
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'1' AS total_type FROM t_praise WHERE t_dynamic_id = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'2' AS total_type  FROM t_praise WHERE t_dynamic_id = ? AND t_praise_user = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'3' AS total_type FROM t_comment WHERE t_dynamic_id = ? ");
					poolSql.append("UNION ");
					poolSql.append(
							"SELECT COUNT(t_id) AS total,'4' AS total_type FROM t_follow WHERE t_follow_id = ? AND t_cover_follow = ? ");

					List<Map<String, Object>> total_count = this.getQuerySqlList(poolSql.toString(), s.get("dynamicId"),
							s.get("dynamicId"), userId, s.get("dynamicId"), userId, s.get("t_id"));

					total_count.forEach(ta -> {
						switch (ta.get("total_type").toString()) {
						case "1":
							s.put("praiseCount", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "2":
							s.put("isPraise", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "3":
							s.put("commentCount", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						case "4":
							s.put("isFollow", null == ta.get("total") ? 0 : ta.get("total"));
							break;
						}
					});

					// 获取该动态是否有图片或者视频文件
					List<Map<String, Object>> dynamicFiles = this.getQuerySqlList(
							"SELECT t_id,t_file_type,t_cover_img_url,t_file_url,t_gold,t_is_private FROM t_dynamic_file WHERE t_dynamic_id = ? ",
							s.get("dynamicId"));
					// 判断当前用户是否已经对当前动态的图片或者视频进行消费
					dynamicFiles.forEach(f -> {
						Map<String, Object> consume = this.getMap(
								"SELECT COUNT(t_id) AS total FROM t_order WHERE t_consume_score = ? AND t_consume = ? ;",
								f.get("t_id"), userId);
						f.put("isConsume", consume.get("total"));
					});
					s.put("dynamicFiles", dynamicFiles);
				});

				mu = new MessageUtil(1, new HashMap<String, Object>() {
					{
						put("pageCount",
								Integer.parseInt(total.get("total").toString()) % 10 == 0
										? Integer.parseInt(total.get("total").toString()) / 10
										: Integer.parseInt(total.get("total").toString()) / 10 + 1);
						put("data", sqlList);
					}
				});
			}
			logger.info(DateUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss:SSS"));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("{}获取{}的动态列表异常!", userId, coverUserId);
		}
		return mu;
	}

}
