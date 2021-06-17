package com.yiliao.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.deser.impl.CreatorCandidate.Param;

import net.sf.json.JSONObject;

/**
 * 
 * CopyRright (c)2016-版权所有:
 * 
 * @项目工程名 WXManage
 * @Module ID <(模块)类编号，可以引用系统设计中的类编号> Comments <对此类的描述，可以引用系统设计中的描述>
 * @JDK 版本(version) JDK1.6.45
 * @命名空间 com.yiliao.util
 * @作者(Author) 
 * @创建日期 2016年4月14日 下午3:38:03
 * @修改人
 * @修改时间 <修改日期，格式:YYYY-MM-DD> 修改原因描述：
 * @Version 版本号 V1.0
 * @类名称 HttpContentUtil
 * @描述 TODO(调用网络接口)
 */
public class HttpContentUtil {
	public static Logger logger = LoggerFactory.getLogger(HttpContentUtil.class);

	public static String urlresult(String url) {
		String result = "";
		try {
			URL submit = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) submit.openConnection();
			connection.setRequestMethod("POST"); // 以Post方式提交表单，默认get方式
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setDoOutput(true);
			// 设置是否从httpUrlConnection读入，默认情况下是true;
			connection.setDoInput(true);
			// Post 请求不能使用缓存
			connection.setUseCaches(false);
			connection.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
			in.close();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	final static String CREATE_API = "https://dwz.cn/admin/v2/create";
	final static String TOKEN = "92644aefff898919f890bc940cd79921"; // TODO:设置Token

	public static void main(String[] args) {

		
	}

	/**
      * 创建短网址
      *
      * @param longUrl
      *            长网址：即原网址
      *        termOfValidity
      *            有效期：1-year或long-term
      * @return  成功：短网址
      *          失败：返回空字符串
      */
     public static String createShortUrl(String params) {
    	 
 
         BufferedReader reader = null;
         try {
             // 创建连接
             URL url = new URL(CREATE_API);
             HttpURLConnection connection = (HttpURLConnection) url.openConnection();
             connection.setDoOutput(true);
             connection.setDoInput(true);
             connection.setUseCaches(false);
             connection.setInstanceFollowRedirects(true);
             connection.setRequestMethod("POST"); // 设置请求方式
             connection.setRequestProperty("Content-Type", "application/json"); // 设置发送数据的格式
             connection.setRequestProperty("Token", TOKEN); // 设置发送数据的格式");
 
             // 发起请求
             connection.connect();
             OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), "UTF-8"); // utf-8编码
             out.append(params);
             out.flush();
             out.close();
             // 读取响应
             reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
             String line;
             String res = "";
             while ((line = reader.readLine()) != null) {
                 res += line;
             }
             reader.close();
             return ""; // TODO：自定义错误信息
         } catch (IOException e) {
             // TODO
             e.printStackTrace();
         }
         return ""; // TODO：自定义错误信息
     }
}