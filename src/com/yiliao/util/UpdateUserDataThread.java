package com.yiliao.util;

import org.apache.commons.lang.StringUtils;

import com.yiliao.domain.User;
import com.yiliao.service.ICommService;

public class UpdateUserDataThread  extends Thread{

	String idCardSql = "UPDATE t_user SET  t_idcard = ?,t_nickName = ?,t_onLine = 0 WHERE t_id= ? ";
	/** 用户编号*/
	private int userId;
	/** 用户昵称**/
	private String nickNane;
	/** 手机系统类型**/
	private String phoneType;
	
	ICommService iCommService  = (ICommService) SpringConfig.getInstance().getBean("iCommServiceImpl");
	
	public UpdateUserDataThread(int userId, String nickNane) {
		super();
		this.userId = userId;
		this.nickNane = nickNane;
	}
	
	public void run() {
		try {
			System.out.println("异步处理用户基础信息修改");
			
			int ID = userId+10000;
			
			iCommService.executeSQL(idCardSql, ID,StringUtils.isNotBlank(nickNane) ? nickNane : "聊友:" + ID,userId);
			//设置IOS用户永久在线
//			if("iPhone".equals(phoneType)) {
//				Thread.sleep(50);
//				iCommService.executeSQL("UPDATE t_user SET t_online_setup = 1,t_onLine = ? WHERE t_id = ? ", User.ONLINE_IDLE,userId);
//			}
			Thread.sleep(50);
			// 注册用户钱包余额
			iCommService.executeSQL("INSERT INTO t_balance (t_user_id, t_recharge_money, t_profit_money, t_share_money) VALUES (?, 0, 0, 0);", userId);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
