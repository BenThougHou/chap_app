package com.yiliao.evnet;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

/**
 * 短信发送
 * @author Administrator
 *
 */
public class PushSms extends ApplicationEvent {
 
	private static final long serialVersionUID = 1L;

	public PushSms(Map<String, String> smsMap) {
		super(smsMap);
	}

}
