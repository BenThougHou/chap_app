package com.yiliao.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色 常量
 * @author Administrator
 *
 */
public class User {

	/** 角色类型 0.普通用户 */
	public static final int  ROLE_TYPE_USER= 0;
	/** 角色类型 1.主播*/
	public static final int  ROLE_TYPE_ANCHOR = 1;
	
	/**是否封号 0.未封号*/
	public static final int  DISABLE_NO = 0;
	/**是否封号 1.已封号*/
	public static final int  DISABLE_YES = 1;
	
	/**是否VIP 0.是*/
	public static final int IS_VIP_YES = 0;
	/**是否VIP 1.否*/
	public static final int IS_VIP_NO =  1;
	
	/**是否勿扰 0.否*/
	public static final int IS_NOT_DISTURB = 0;
	/**是否勿扰 0.是*/
	public static final int IS_YES_DISTURB = 1;
	
	/**是否在线 0.空闲*/
	public static final int ONLINE_IDLE = 0;
	/**是否在线 1.忙碌*/
	public static final int ONLINE_BE_BUSY = 1;
	/**是否在线 2.离线*/
	public static final int ONLINE_OFF_LINE = 2;
    /**虚拟用户*/	
	public static List<Integer> vritaulUser = new ArrayList<>();
	
	
}
