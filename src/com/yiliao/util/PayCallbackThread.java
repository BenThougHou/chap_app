package com.yiliao.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yiliao.service.ConsumeService;

public class PayCallbackThread extends Thread {
	
	Logger logger = LoggerFactory.getLogger(getClass());

	ConsumeService consumeService  = (ConsumeService) SpringConfig.getInstance().getBean("consumeService");

	public int userId;

	public PayCallbackThread(int userId) {
		super();
		this.userId = userId;
	}

	@Override
	public void run() {
		consumeService.asynchronousProcessing(userId);
	}
	 
}
