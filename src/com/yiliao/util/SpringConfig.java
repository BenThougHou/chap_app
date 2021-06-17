package com.yiliao.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public final class SpringConfig implements ApplicationContextAware{
	
	private  static ApplicationContext ctx = null;

	public static synchronized  ApplicationContext getInstance() {
		return ctx;
	}

	public void setApplicationContext(ApplicationContext ac)
			throws BeansException {
		ctx=ac;
	}
}
