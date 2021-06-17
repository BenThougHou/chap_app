package com.yiliao.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LoginInfo {

	/** 用户编号 */
	private int userId;
	/** 用户角色 */
	private int t_role;
	/** 用户是否VIP */
	private int t_is_vip;
	/** 登陆人性别*/
	private int t_sex;
	/** 上一次发送消息时间*/
	private long loginTime;
	/** 发送时间集合 */
	private List<Integer> times;
	/** 用于存储已经推送过的主播 */
	private List<Integer> anchor = Collections
			.synchronizedList(new ArrayList<Integer>());

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getT_role() {
		return t_role;
	}

	public void setT_role(int t_role) {
		this.t_role = t_role;
	}

	public int getT_is_vip() {
		return t_is_vip;
	}

	public void setT_is_vip(int t_is_vip) {
		this.t_is_vip = t_is_vip;
	}

	public List<Integer> getTimes() {
		return times;
	}

	public void setTimes(List<Integer> times) {
		this.times = times;
	}

	public List<Integer> getAnchor() {
		return anchor;
	}

	public void setAnchor(List<Integer> anchor) {
		this.anchor = anchor;
	}

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	public int getT_sex() {
		return t_sex;
	}

	public void setT_sex(int t_sex) {
		this.t_sex = t_sex;
	}

	
}
