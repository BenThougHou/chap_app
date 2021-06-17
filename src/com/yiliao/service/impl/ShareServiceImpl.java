package com.yiliao.service.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yiliao.service.ActivityService;
import com.yiliao.service.ShareService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.ShortUrl;
import com.yiliao.util.SpringConfig;
import com.yiliao.util.SystemConfig;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service("shareService")
public class ShareServiceImpl extends ICommServiceImpl implements ShareService {

	/*
	 * 保存设备信息(non-Javadoc)
	 * 
	 * @see com.yiliao.service.ShareService#addShareInfo(int, java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
	@Override
	public void addShareInfo(int userId, String equipment, String system_moble, String ipAddress) {
		try {

			String sql = "INSERT INTO t_device (t_phone_type, t_system_version, t_ip_address, t_referee_id, t_is_use, t_create_time) VALUES (?, ?, ?, ?, ?, ?);";

			this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, equipment.trim(), system_moble.trim(),
					ipAddress.trim(), userId, 0, DateUtils.format(new Date(), DateUtils.FullDatePattern));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 添加分享记录(non-Javadoc)
	 * 
	 * @see com.yiliao.service.ShareService#addShareCount(int)
	 */
	@Override
	public MessageUtil addShareCount(int userId) {
		MessageUtil mu = null;
		try {
			String inSql = "INSERT INTO t_share_notes (t_user_id, t_target, t_date) VALUES (?,?,?) ";

			this.getFinalDao().getIEntitySQLDAO().executeSQL(inSql, userId, 1,
					DateUtils.format(new Date(), DateUtils.FullDatePattern));

			mu = new MessageUtil(1, "添加成功!");

			// 查询用户已分享次数
			String qSql = " SELECT count(t_id) AS total FROM t_share_notes WHERE t_user_id = ? ";
			Map<String, Object> totalMap = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(qSql,
					userId);

			mu.setM_object(totalMap.get("total"));
			// 获取用户的性别
			qSql = " SELECT t_id FROM t_user WHERE t_id = ?  AND t_sex = 0 ";
			List<Map<String, Object>> userMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql, userId);
			// 判断是否已经分享了3次 且用户性别必须为女
			if (Integer.parseInt(totalMap.get("total").toString()) >= 3 && !userMap.isEmpty()) {
				// 在次判断用户是否已经领取了奖品了
				qSql = "SELECT t_id  AS total FROM t_award_record WHERE t_user_id = ? ";
				List<Map<String, Object>> dataMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql, userId);
				// 用户未中过奖
				if (null == dataMap || dataMap.isEmpty()) {
					// 判断用户是否已经实名认证了
					qSql = "SELECT * FROM t_certification WHERE t_type = 0 and t_user_id = ? ";
					List<Map<String, Object>> identificationMap = this.getFinalDao().getIEntitySQLDAO()
							.findBySQLTOMap(qSql, userId);
					// 用户必须实名认证才能中奖
					if (!identificationMap.isEmpty()) {
						// 获取用户可参与的活动
						qSql = " SELECT t_id  FROM t_activity WHERE t_is_enable = 0 AND t_join_term = 3 ";
						List<Map<String, Object>> actMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql);
						if (!actMap.isEmpty()) {
							ActivityService activityService = (ActivityService) SpringConfig.getInstance()
									.getBean("activityService");
							activityService.shareRedPacket(userId,
									Integer.parseInt(actMap.get(0).get("t_id").toString()));
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("添加分享记录!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public Map<String, Object> getDownLoadUrl() {

		String qSql = "SELECT t_android_download,t_ios_download FROM t_system_setup; ";

		return this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(qSql);
	}

	@Override
	public MessageUtil getSpreadUrl(int userId) {
		try {
			return new MessageUtil(1, new HashMap<String, Object>() {
				private static final long serialVersionUID = 1L;
				{
					// 获取分线个链接
					List<Map<String, Object>> sqlList = getQuerySqlList("SELECT t_domain_name FROM t_domainnamepool order by rand() limit 1 ");

					if (null == sqlList || sqlList.isEmpty()) {
						sqlList = getQuerySqlList("SELECT t_domain_name FROM t_domainnamepool WHERE t_effect_type = 3 ");
					}

					StringBuffer sql = new StringBuffer();
					sql.append("SELECT t_img_path FROM t_spreed_img AS t1 JOIN ");
					sql.append("(SELECT ROUND(RAND() * ((SELECT MAX(t_id) FROM t_spreed_img)-(SELECT MIN(t_id) ");
					sql.append("FROM t_spreed_img))+(SELECT MIN(t_id) FROM t_spreed_img)) AS id) AS t2 ");
					sql.append("WHERE t1.t_id >= t2.id ");
					sql.append("ORDER BY t1.t_id LIMIT 1; ");
					// 获取图片的物理硬盘地址
					Map<String, Object> imgPath = getMap(sql.toString());

					put("backgroundPath", SystemConfig.getValue("spreed_img_url") + imgPath.get("t_img_path"));

					if (null == sqlList || sqlList.isEmpty()) {
						put("shareUrl", ShortUrl.shortUrl(SystemConfig.getValue("share_url") + userId));
					} else {
						String value = SystemConfig.getValue("share_url");
						put("shareUrl", ShortUrl.shortUrl(sqlList.get(0).get("t_domain_name")
								+ value.substring(value.indexOf("/share")) + userId));

					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			return new MessageUtil(0, "程序异常!");
		}
	}


	@Override
	public void getIMInfo(Map<String, Object> params) {
		if (params != null) {

			JSONArray jsonArray = JSONArray.fromObject(params.get("messages"));

			String inSql = "INSERT INTO t_im_log (t_idcard, t_user_name, t_cover_idcard, t_cover_name, t_content, t_create_time) VALUES ( ?, ?, ?, ?, ?, ?);";

			for (int i = 0; i < jsonArray.size(); i++) {

				JSONObject fromObject = JSONObject.fromObject(jsonArray.get(i));

				int idcard = fromObject.getInt("from_id");
				int coverIdCard = fromObject.getInt("target_id");
				// 发送内容
				String content = fromObject.getJSONObject("msg_body").getString("text");
				// 发送时间
				String dateTime = secondToDate(fromObject.getLong("msg_ctime"), "yyyy-MM-dd HH:mm:ss");

				this.executeSQL(inSql, idcard,
						getMap("SELECT t_nickName FROM t_user WHERE t_idcard = ?", idcard).get("t_nickName"),
						coverIdCard,
						getMap("SELECT t_nickName FROM t_user WHERE t_idcard = ? ", coverIdCard).get("t_nickName"),
						content, dateTime);
			}
		}
	}

	/**
	 * 秒转换为指定格式的日期
	 * 
	 * @param second
	 * @param patten
	 * @return
	 */
	private String secondToDate(long second, String patten) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(second);// 转换为毫秒
		Date date = calendar.getTime();
		SimpleDateFormat format = new SimpleDateFormat(patten);
		String dateString = format.format(date);
		return dateString;
	}
	
	@Override
	public Map<String, Object> getPacDownLoadUrl(String userId) {

		String qSql = "SELECT t_dow_url t_android_download FROM t_channel_pac_dow_log where t_user_id=? ";

		List<Map<String, Object>> querySqlList = this.getQuerySqlList(qSql, userId);
		
		if(querySqlList!=null&&!querySqlList.isEmpty()) {
			return querySqlList.get(0);
		}
		return null;
	}

	@Override
	public Map<String, Object> getDownLoadUrlList(int t_effect_type) {
		String qSql = "SELECT t_domain_name FROM t_domainnamepool_one where t_effect_type=? order by rand() limit 1 ";

		List<Map<String, Object>> querySqlList = this.getQuerySqlList(qSql,t_effect_type);
		
		if(querySqlList!=null&&!querySqlList.isEmpty()) {
			return querySqlList.get(0);
		}
		return null;
	}
}
