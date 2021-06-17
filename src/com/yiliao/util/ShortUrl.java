package com.yiliao.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import net.sf.json.JSONObject;

public class ShortUrl {
	/**
	 * 生成短连接地址
	 * 
	 * @param url
	 * @return
	 */
	public static String shortUrl(String url) {
		String short_key = SystemConfig.getValue("short_key");
		if(null==short_key||short_key.equals("")) {
			return url;
		}
		StringBuffer body = new StringBuffer();
		body.append("http://suo.im/api.htm?");
		body.append("format=json").append("&");
		try {
			body.append("url=").append(URLEncoder.encode(url, "UTF-8")).append("&");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		body.append("key=").append(short_key);
		body.append("&expireDate=").append("2030-12-30");

		System.out.println(body.toString());

		JSONObject httpClentString = HttpUtil.httpConnection(body.toString());

		System.out.println(httpClentString.get("url"));

		return httpClentString.getString("url");
	}
}
