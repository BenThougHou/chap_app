package com.yiliao.service.impl;

import com.google.zxing.WriterException;
import com.yiliao.service.SystemService;
import com.yiliao.util.FileDonloadUtil;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.ShortUrl;
import com.yiliao.util.SystemConfig;
import com.yiliao.util.ZxingUtils;

import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Service("systemService")
public class SystemServiceImpl extends ICommServiceImpl implements SystemService {


	@Override
	public MessageUtil getPrivatePhotoMoney() {
		MessageUtil mu = new MessageUtil();
		try {
			// 查询私密照片收费
			Map<String, Object> map = this.getMap("SELECT t_extract_ratio FROM t_extract WHERE t_project_type = 9 ");

			mu = new MessageUtil(1, map.get("t_extract_ratio"));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取私密照片收费异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getPrivateVideoMoney() {
		MessageUtil mu = new MessageUtil();
		try {
			// 查询私密视频收费
			Map<String, Object> map = this.getMap("SELECT t_extract_ratio FROM t_extract WHERE t_project_type = 10 ");

			mu = new MessageUtil(1, map.get("t_extract_ratio"));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取私密照片收费异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getAnthorChargeList() {
		MessageUtil mu = new MessageUtil();
		try {

			// 查询私密视频收费
			List<Map<String, Object>> sqlList = this.getQuerySqlList(
					"SELECT t_project_type,t_extract_ratio FROM t_extract WHERE t_project_type BETWEEN 5 AND 8 or t_project_type=12 or t_project_type=13 ");

			mu = new MessageUtil(1, sqlList);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取私密照片收费异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getIdentificationWeiXin() {
		MessageUtil mu = null;
		try {
			// 查询私密视频收费
			Map<String, Object> map = this.getMap("SELECT t_extract_ratio FROM t_extract WHERE t_project_type = 11 ");

			mu = new MessageUtil(1, map.get("t_extract_ratio"));

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取私密照片收费异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	/**
	 * 获取客服QQ和客服微信
	 */
	@Override
	public MessageUtil getServiceQQ(int userId) {
		try {

			List<Map<String, Object>> sqlList = this.getQuerySqlList("SELECT t_service_qq,t_service_wx FROM t_system_setup");

			if (!sqlList.isEmpty()) {
				return new MessageUtil(1, sqlList.get(0).get("t_service_qq"));
			}
			return new MessageUtil(-1, "客服QQ存在!");
		} catch (Exception e) {
			e.printStackTrace();
			return new MessageUtil(0, "程序异常!");
		}
	}

	@Override
	public MessageUtil getHelpContre(int userId) {
		try {

			List<Map<String, Object>> sqlList = this.getQuerySqlList(
					"SELECT t_title,t_content,DATE_FORMAT(t_create_time,'%Y-%m-%d') AS t_create_time FROM t_help_center ORDER BY t_sort ASC ");

			return new MessageUtil(1, sqlList);
		} catch (Exception e) {
			e.printStackTrace();
			return new MessageUtil(0, "程序异常!");
		}
	}


	/**
	 * 生成预览图
	 */
	@Override
	public void onloadGlanceOver(int userId, HttpServletResponse response) {
		try {

			StringBuffer sql = new StringBuffer();
			sql.append("SELECT t_img_path FROM t_spreed_img AS t1 JOIN ");
			sql.append("(SELECT ROUND(RAND() * ((SELECT MAX(t_id) FROM t_spreed_img)-(SELECT MIN(t_id) ");
			sql.append("FROM t_spreed_img))+(SELECT MIN(t_id) FROM t_spreed_img)) AS id) AS t2 ");
			sql.append("WHERE t1.t_id >= t2.id ");
			sql.append("ORDER BY t1.t_id LIMIT 1; ");
			// 获取图片的物理硬盘地址
			Map<String, Object> imgPath = getMap(sql.toString());

			List<Map<String, Object>> sqlList = getQuerySqlList(
					"SELECT t_domain_name FROM t_domainnamepool ORDER BY RAND() LIMIT 1");
			String img_path = "";
			if (null == sqlList || sqlList.isEmpty()) {
				img_path = SystemConfig.getValue("share_url") + userId;
			} else {
				String value = SystemConfig.getValue("share_url");
				img_path = sqlList.get(0).get("t_domain_name") + value.substring(value.indexOf(":9008")) + userId;
			}
			// 生成短连接
			img_path =  ShortUrl.shortUrl(img_path);

			// 二维码宽度
			int width = 300;
			// 二维码高度
			int height = 300;
			BufferedImage zxingImage = null;
			try {
				// 二维码图片流
				zxingImage = ZxingUtils.enQRCode(img_path, width, height);
			} catch (WriterException e) {
				e.printStackTrace();
			}
			// 背景图片地址
			String backgroundPath = "";
			if (Config().indexOf("Windows") > -1) {
				backgroundPath = SystemConfig.getValue("windows_speed_fileUrl") + imgPath.get("t_img_path");
			} else {
				backgroundPath = SystemConfig.getValue("centos_speed_fileUrl") + imgPath.get("t_img_path");
			}
			// 判断文件是否存在
			File file = new File(backgroundPath);

			if (!file.exists()) {
				logger.info("--文件不存在--");
				logger.info("图片地址->{}", backgroundPath);
				file.getParentFile().mkdir();
				FileDonloadUtil.downloadPicture(SystemConfig.getValue("spreed_img_url") + imgPath.get("t_img_path"),
						backgroundPath);
			}

			InputStream inputStream = null;
			OutputStream os = null;
			try {
				// 合成二维码和背景图
				BufferedImage image = ZxingUtils.drawImage(backgroundPath, zxingImage, 225, 774);
				// 绘制文字
//	            Font font = new Font("微软雅黑", Font.BOLD, 35);
//	            String text = "17000";
//	            image = ZxingUtils.drawString(image, text, 375, 647,font,new Color(244,254,189));
				// 图片转inputStream
				inputStream = ZxingUtils.bufferedImageToInputStream(image);

				String fielName = imgPath.get("t_img_path").toString().split("/")[1];

				response.setHeader("Content-Disposition", "attachment;fileName=" + fielName);

				os = response.getOutputStream();

				int count = 0;
				byte[] buffer = new byte[1024 * 8];
				while ((count = inputStream.read(buffer)) != -1) {
					os.write(buffer, 0, count);
					os.flush();
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				inputStream.close();
				os.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 判断操作系统
	 */
	public static String Config() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress().toString(); // 获取本机ip
			String hostName = addr.getHostName().toString(); // 获取本机计算机名称
			Properties props = System.getProperties();
			return props.getProperty("os.name");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Centos";
	}

	@Override
	public Map<String, Object> getSpreedTipsMsg() {
		try {
			// 获取速配提示消息
			List<Map<String, Object>> spreedTipsMsgList = this
					.getQuerySqlList("SELECT t_spreed_hint FROM t_system_setup");

			if (!spreedTipsMsgList.isEmpty()) {
				return spreedTipsMsgList.get(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
