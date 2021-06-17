package com.yiliao.service;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.yiliao.util.MessageUtil;

public interface LoginService {
	
	
	
	
	public MessageUtil getLongSetUpList();
	
	
	/**
	 * 获取验证码登陆
	 */
	public void sendPhoneVerificationCode(String phone,String verifyCode,int resType,String smsCode,HttpServletResponse response);
	
	/**
	 * 用户账号密码登陆
	 * @param phone
	 * @param password
	 * @return
	 */
	public MessageUtil userLogin(String phone,String password);
	/**
	 * 用户登录
	 * @param phone
	 * @param pwd
	 * @return
	 */
	public MessageUtil login(String phone,String smsCode,String t_phone_type,String t_system_version,String t_ip_address,String deviceNumber,int shareUserId);
	/**
	 * 更新手机号
	 * @param phone
	 * @param smsCode
	 * @return
	 */
	public MessageUtil updatePhone(Integer userId,String phone,String smsCode);
	
	/**
	 * 修改用户性别
	 * @param userId
	 * @param sex
	 * @return
	 */
	public MessageUtil upateUserSex(int userId , int sex);
	
	/**
	 * 修改用户在先状态
	 * @param userId
	 * @param type
	 * @return
	 */
	public MessageUtil updateAnchorOnline(int userId,int type);
	
	
	/**
	 * 微信登陆
	 * @param openId
	 * @param nickName
	 * @param handImg
	 * @param city
	 * @param sex
	 * @return
	 */
	public MessageUtil weixinLogin(String openId,String nickName,String handImg,String city,String t_phone_type,String t_system_version,String t_ip_address,String deviceNumber,int shareUserId);
	
	/**
	 * qq登陆
	 * @param openId
	 * @param nickName
	 * @param handImg
	 * @param city
	 * @param sex
	 * @return
	 */
	public MessageUtil qqLogin(String openId, String nickName, String handImg,String city,String t_phone_type,String t_system_version,String t_ip_address,String deviceNumber,int shareUserId);
	
	/**
	 * 登出
	 * @param userId
	 * @return
	 */
	public MessageUtil logout(int userId);
	
	/**
	 * 获取新用户推送消息
	 * @param userId
	 * @return
	 */
	public MessageUtil getPushMsg(int userId);
	
	/**
	 * 获取最新版本
	 * @return
	 */
	public MessageUtil getNewVersion();
	
	/**
	 * 更新最后登陆时间
	 * @param userId
	 */
	public void upLoginTime(int userId);
	
	/**
	 * socket 断开  修改用户状态
	 * @param userId
	 */
	public void socketBreak(int userId);
	/**
	 * 启动修改用户状态
	 */
	public void startUpOnLine();
	
	/**
	 * 获取用户是否被封号
	 * @param userId
	 */
	public Map<String, Object> getUserIsDisable(int userId);
	/**
	 * IOS获取最新版本
	 * @return
	 */
	public MessageUtil getIosVersion();
	
	/**
	 * 手动绑定推广人
	 * @param userId
	 * @param idCard
	 * @return
	 */
	MessageUtil uniteIdCard(int userId,int idCard);
	
    /**
     * 发送模拟消息
     * @param userId
     */
	void sendSimulationVideo(int userId);
	
	/**
	 * 注册账号
	 * @param phone
	 * @param password
	 * @param code
	 * @return
	 */
	MessageUtil register(String phone,String password,String smsCode,String t_phone_type,String t_system_version,String t_ip_address,String deviceNumber,int shareUserId);
	
	/**
	 * 修改密码
	 * @param phone
	 * @param password
	 * @param smsCode
	 * @return
	 */
	MessageUtil upPassword(String phone,String password,String smsCode);
	/**
	 * 根据tokenId和token匹配用户和token是否同一人
	 * @param tokenId 用户编号
	 * @param token token
	 * @return true:验证成功  false:验证失败
	 */
	Map<String, Object> getUserVerifyToken(int tokenId,String token);

	/**
	 * 非vip首次登陆获取私信数
	 * @param userId 登陆人id
	 * @return
	 */
	MessageUtil privateChatNumber(int userId);


	/**
	 * 打招呼
	 * @param userId  用户id
	 * @param anchorUserId 主播id
	 * @return
	 */
	MessageUtil greet(int userId,int anchorUserId);
}
