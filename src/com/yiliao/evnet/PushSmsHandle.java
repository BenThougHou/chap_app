package com.yiliao.evnet;

import com.yiliao.util.IndiaSendSMSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 短信发送
 * @author Administrator
 *
 */
@Component(value="PushSmsHandle")
public class PushSmsHandle implements ApplicationListener<PushSms> {
 
	Logger logger = LoggerFactory.getLogger(getClass());


	@Override
	public void onApplicationEvent(PushSms event) {
		 
		@SuppressWarnings("unchecked")
		Map<String, String> smsPhone  = (Map<String, String>) event.getSource();
		logger.info("异步发送短信验证码");


		String result = IndiaSendSMSUtil.sendIndiaSms(smsPhone.get("phone"), smsPhone.get("smsCode"));
		logger.info("india sms result:{}",result);
		
		/*if(null == smsPhone.get("sms_type")) {
			//发送短信
			SendSMSUtil.sendSms(smsPhone.get("phone").toString(), smsPhone.get("smsCode").toString());
		}else
			//腾讯短信
			if(smsPhone.get("sms_type").equals("0")) {
				SendSMSUtil.sendQQSMS(smsPhone.get("appid").toString(), smsPhone.get("appkey").toString(), Integer.parseInt(smsPhone.get("templateId")), smsPhone.get("smsSign").toString(), smsPhone.get("phone").toString(), smsPhone.get("smsCode").toString());
				//阿里云
			}else if(smsPhone.get("sms_type").equals("1")) {
				SendSMSUtil.sendAliPaySMS(smsPhone.get("appid"), smsPhone.get("appkey"), smsPhone.get("templateId"), smsPhone.get("smsSign"), smsPhone.get("phone"), smsPhone.get("smsCode"));
				//网易云
			}else if(smsPhone.get("sms_type").equals("2")) {
				SendSMSUtil.sendNetEaseSMS(smsPhone.get("appid"), smsPhone.get("appkey"), Integer.parseInt(smsPhone.get("templateId")), smsPhone.get("smsSign"), smsPhone.get("phone"), smsPhone.get("smsCode"));
			}*/
		
	}


}
