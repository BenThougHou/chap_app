package com.yiliao.domain;

import java.util.Date;

public class SmsDescribe {

	/**手机号*/
	private String phone;
	/**验证码 */
	private String smsCode;
	/**发送时间*/
	private Date time;
	/** 图形验证码 */
	private String imgVerify;
	
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getSmsCode() {
		return smsCode;
	}
	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}
 
	public String getImgVerify() {
		return imgVerify;
	}
	public void setImgVerify(String imgVerify) {
		this.imgVerify = imgVerify;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	
	
	
	

}
