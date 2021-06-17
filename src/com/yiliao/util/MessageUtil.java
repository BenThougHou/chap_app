package com.yiliao.util;

/**
 * 
 * 2012-3-28 封装提示信息类
 * 
 */
public class MessageUtil {

	private String m_strMessage; // 提示信息的消息

	private Integer m_istatus; // 提示信息的状态(0表示失败/1表示成功)

	private Object m_object; // 提示信息传递的参数信息

	
	public MessageUtil() {
		// TODO Auto-generated constructor stub
	}

	public MessageUtil(Integer m_istatus,String m_strMessage) {
		this.m_istatus = m_istatus;
		this.m_strMessage = m_strMessage;
	}

 

	public MessageUtil(Integer m_istatus, Object m_object) {
		super();
		this.m_istatus = m_istatus;
		this.m_object = m_object;
	}

	public MessageUtil(Integer m_istatus,String m_strMessage, Object m_object) {
		this.m_strMessage = m_strMessage;
		this.m_istatus = m_istatus;
		this.m_object = m_object;
	}

    public String getM_strMessage() {
		return m_strMessage;
	}

	public void setM_strMessage(String mStrMessage) {
		m_strMessage = mStrMessage;
	}

	public Integer getM_istatus() {
		return m_istatus;
	}

	public void setM_istatus(Integer mIstatus) {
		m_istatus = mIstatus;
	}

	public Object getM_object() {
		return m_object;
	}

	public void setM_object(Object mObject) {
		m_object = mObject;
	}

}
