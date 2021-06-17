package com.yiliao.util;


import java.util.Properties;


/**
 * 2013-01-05
 * 配置文件类
 */
public class SystemConfig {

	//private final static String CONFIG_PATH = "config/systemConfig.xml"; //得到配置文件的路径
	
	private static Properties m_pro = null;                          //定义Properties对象
	//静态方法
	static {
		m_pro = new Properties();
		try {
			m_pro.loadFromXML(SystemConfig.class.getResourceAsStream("/systemConfig.xml"));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	
		
	}

	/**
	 * 2013-01-05
	 * @param 配置文件的key
	 * @return配置文件的值
	 */
	public static String getValue(String pstrkey) {
		return m_pro.getProperty(pstrkey);	 
	}
	
	public static Object setProperty(String key, String value){
		
		return m_pro.setProperty(key, value);
	}
}
