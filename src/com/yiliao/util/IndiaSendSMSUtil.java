package com.yiliao.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.text.MessageFormat;
import java.util.Map;

public class IndiaSendSMSUtil {


	public static void main(String[] args) {
		System.out.println(IndiaSendSMSUtil.sendIndiaSms("18238831810", "1234"));
	}


	public static String  sendIndiaSms(String phone, String smsCode){
		long timestamp = System.currentTimeMillis();
		phone = "0091"+ phone;
		String appkey = SystemConfig.getValue("india_sms_appkey");
		String appsecret = SystemConfig.getValue("india_sms_appsecret");
		String appcode = SystemConfig.getValue("india_sms_india_sms_appcode");
		String url = SystemConfig.getValue("india_sms_url");
		String msg = SystemConfig.getValue("india_sms_msg");
		url = url+"?appkey={appkey}&appcode={appcode}&appsecret={appsecret}&timestamp={timestamp}&sign={sign}&uid={uid}&phone={phone}&msg={msg}&extend={extend}";
		msg = MessageFormat.format(msg,smsCode);
		String sign = Md5Util.encodeByMD5ByLow(new StringBuilder(appkey).append(appsecret).append(timestamp).toString()).toLowerCase();
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map> request = new HttpEntity(null, httpHeaders);
		return new RestTemplate().postForObject(url, request, String.class,appkey,appcode,
				appsecret,timestamp,sign,"",phone,msg,"");
	}

}
