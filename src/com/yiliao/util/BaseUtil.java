package com.yiliao.util;

import java.util.Calendar;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class BaseUtil {

	private static String ffmpeg_path = SystemConfig.getValue("realPath") + "/CloudFile/tool/ffmpeg.exe";

	public HttpSession getSession() {

		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder
				.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();

		return request.getSession();
	}

	/**
	 * 判断对象是否为空
	 * 
	 * @param obj -参数对象
	 * @return boolean -true:表示对象为空;false:表示对象为非空
	 */
	public static boolean isEmpty(Object obj) {
		return obj == null || obj.toString().equalsIgnoreCase("null") || obj.toString().length() == 0;
	}

	/**
	 * 获取年月日路径
	 * 
	 * @return
	 */
	public static String getTimeUrl() {

		Calendar cal = Calendar.getInstance();// 使用日历类
		int year = cal.get(Calendar.YEAR);// 得到年
		int month = cal.get(Calendar.MONTH) + 1;// 得到月，因为从0开始的，所以要加1
		int day = cal.get(Calendar.DAY_OF_MONTH);// 得到天

		return year + "/" + month + "/" + day;
	}

	/**
	 * 获取6位随机数
	 * 
	 * @return
	 */
	public static int randomNum() {

		Random r = new Random();
		int num = r.nextInt(900000) + 100000;

		return num;
	}

	/**
	 * 截取文件名后缀
	 */
	public static String getExtention(String fileName) {
		int pos = fileName.lastIndexOf(".");
		return fileName.substring(pos + 1);
	}

	/**
	 * 短信条数
	 * 
	 * @param smsVarNum
	 * @return
	 */
	public static int smsNum(int smsVarNum) {

		int smsNum = 0;

		if ((smsVarNum % 65) == 0) {

			smsNum = (smsVarNum / 65);

		} else {

			smsNum = (smsVarNum / 65) + 1;
		}

		return smsNum;
	}

	/**
	 * 判断号码是不是手机号码
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isMobileNO(String mobiles) {
		Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(17[6-8])|(18[0-9]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	/**
	 * 判断号码是不是联通手机号码
	 * 
	 * @param mobiles
	 * @return
	 */
	public static boolean isUnicomMobileNO(String mobiles) {
		Pattern p = Pattern.compile("^((13[0-2])|(15[5-6])|(176)|(18[5-6]))\\d{8}$");
		Matcher m = p.matcher(mobiles);
		return m.matches();
	}

	/**
	 * 验证参数是否传递
	 * 
	 * @param objects
	 * @return
	 */
	public static boolean params(Object... objects) {

		for (Object obj : objects) {
			if (null == obj) {
				return false;
			} else if (obj instanceof Integer && ((Integer) obj).intValue() <= 0) {
				return false;
			} else if (obj instanceof String && StringUtils.isBlank((String) obj)) {
				return false;
			} else if (obj instanceof Double && ((Double) obj).doubleValue() <= 0.0) {
				return false;
			} else if (obj instanceof Float && ((Float) obj).floatValue() <= 0.0) {
				return false;
			} else if (obj instanceof Long && ((Long) obj).longValue() <= 0) {
				return false;
			}
		}
		return true;
	}

}
