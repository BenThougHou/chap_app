package com.yiliao.control;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.domain.SmsDescribe;
import com.yiliao.service.LoginService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.PrintUtil;
import com.yiliao.util.RSACoderUtil;
import com.yiliao.util.RedisUtil;
import com.yiliao.util.ValidateCodeUtil;

import net.sf.json.JSONObject;

/**
 * 登录相关接口
 * 
 * @author Administrator
 * 
 */

@Controller
@RequestMapping("app")
public class AppLoginControl {

	@Autowired
	private LoginService loginAppService;

	@Autowired
	RedisUtil redisUtil;

	/**
	 * 获取登陆设置列表
	 */
	@RequestMapping(value = { "getLongSetUpList" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getLongSetUpList() {

		return this.loginAppService.getLongSetUpList();
	}

	/**
	 * 生产验证码
	 * 
	 * @param request
	 * @param response
	 */
	@RequestMapping(value = { "getVerify" })
	@ResponseBody
	public void getVerify(HttpServletRequest req, HttpServletResponse response) {
		// 进行解密
//		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(req.getParameter("phone"))) {
			// 返回数据
			PrintUtil.printWri(new MessageUtil(-500, "服务器拒绝执行请求!"), response);
			return;
		}
		response.setContentType("image/jpeg");// 设置相应类型,告诉浏览器输出的内容为图片
		response.setHeader("Pragma", "No-cache");// 设置响应头信息，告诉浏览器不要缓存此内容
		response.setHeader("Cache-Control", "no-cache");
		response.setCharacterEncoding("UTF-8");
		response.setDateHeader("Expire", 0);
		new ValidateCodeUtil().getRandcode(req.getParameter("phone").toString(), response, redisUtil);
	}

	/**
	 * 验证 图形验证码是否正确
	 * 
	 * @param phone
	 * @param verifyCode
	 * @return
	 */
	@RequestMapping(value = "getVerifyCodeIsCorrect", method = { RequestMethod.GET, RequestMethod.POST })
	@ResponseBody
	public MessageUtil getVerifyCodeIsCorrect(HttpServletRequest req) {

		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getString("phone"), param.getString("verifyCode"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		SmsDescribe sd = (SmsDescribe) JSONObject.toBean(
				JSONObject.fromObject(redisUtil.getKeyObject("verify_" + param.getString("phone"))), SmsDescribe.class);
		if (null == sd) {
			return new MessageUtil(-1, "匹配失败!");
		} else if (sd.getImgVerify().equals(param.getString("verifyCode"))) {
			return new MessageUtil(1, "匹配成功!");
		} else {
			return new MessageUtil(-1, "匹配失败!");
		}
	}

	/**
	 * 发送验证码
	 * 
	 * @param phone
	 * @param resType  1.登陆请求 2.绑定手机号请求  3.根据手机号发送验证码
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "sendPhoneVerificationCode" }, method = { RequestMethod.POST })
	@ResponseBody
	public void sendPhoneVerificationCode(HttpServletRequest req, HttpServletResponse response) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (param.getInt("resType") == 1
				&& !BaseUtil.params(param.getString("phone"), param.getOrDefault("verifyCode", ""))) {
			// 返回数据
			PrintUtil.printWri(new MessageUtil(-500, "服务器拒绝执行请求!"), response);
			return;
		} else if (param.getInt("resType") == 2 && !BaseUtil.params(param.getString("phone"))) {
			PrintUtil.printWri(new MessageUtil(-500, "服务器拒绝执行请求!"), response);
			return;
		}
		// 产生验证码
		int smsCode = new Random().nextInt(9000) + 1000;

		this.loginAppService.sendPhoneVerificationCode(param.getString("phone"),
				param.getOrDefault("verifyCode", "").toString(), param.getInt("resType"), smsCode + "", response);
		// 返回数据
//		PrintUtil.printWri(mu, response);
	}

	/**
	 * 账号密码注册
	 * 
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "register", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil register(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("phone", ""), param.getOrDefault("password", ""),
				param.getOrDefault("smsCode", ""), param.getOrDefault("deviceNumber", ""),
				param.getOrDefault("t_phone_type", ""), param.getOrDefault("t_system_version", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		
		boolean redisLimitRegister = redisLimitRegister(param.get("phone"));
		if(redisLimitRegister) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.loginAppService.register(param.getString("phone"), param.getString("password"),
				param.getString("smsCode"), param.getString("t_phone_type"), param.getString("t_system_version"),
				null == param.get("ip") ? "" : param.getString("ip"), param.getString("deviceNumber"),
				param.containsKey("shareUserId") ? param.getInt("shareUserId") : 0);
	}

	private boolean redisLimitRegister(Object ip) {
		String string = redisUtil.get("register_"+ip);
		if(string!=null) {
			Long currentTimeMillis=Long.valueOf(System.currentTimeMillis()) ;
			Long parseLong = Long.valueOf(string);
			if(currentTimeMillis-parseLong<2000) {
				return true;
			}
			redisUtil.set("register_"+ip,currentTimeMillis+"",1L);
			return false;
		} else {
			redisUtil.set("register_"+ip,System.currentTimeMillis()+"",1L);
			return false;
		}
	}

	/**
	 * 用户账号密码登陆
	 * 
	 * @param phone
	 * @param password
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "userLogin" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil userLogin(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("phone", ""), param.getOrDefault("password", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		boolean redisLimitRegister = redisLimitRegister(param.get("phone"));
		if(redisLimitRegister) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.loginAppService.userLogin(param.getString("phone"), param.getString("password"));
	}

	/**
	 * 手机号登陆
	 * 
	 * @param phone
	 * @param pwd
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "login" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil login(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("phone", ""), param.getOrDefault("deviceNumber", ""),
				param.getOrDefault("smsCode", ""), param.getOrDefault("t_phone_type", ""),
				param.getOrDefault("t_system_version", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		boolean redisLimitRegister = redisLimitRegister(param.get("phone"));
		if(redisLimitRegister) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.loginAppService.login(param.getString("phone"), param.getString("smsCode"),
				param.getString("t_phone_type"), param.getString("t_system_version"),
				null == param.get("ip") ? "" : param.getString("ip"), param.getString("deviceNumber"),
				param.containsKey("shareUserId") ? param.getInt("shareUserId") : 0);
	}

	/**
	 * 用户更新手机号
	 * 
	 * @param phone
	 * @param smsCode
	 * @param response
	 */
	@RequestMapping(value = { "updatePhone" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil updatePhone(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getString("phone"), param.getString("smsCode"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		boolean redisLimitRegister = redisLimitRegister(param.get("userId"));
		if(redisLimitRegister) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.loginAppService.updatePhone(param.getInt("userId"), param.getString("phone"),
				param.getString("smsCode"));
	}

	
	/**
	 *  验证手机短信验证码是否正确
	 * 
	 * @param phone
	 * @param smsCode
	 * @param response
	 */
	@RequestMapping(value = { "getPhoneSmsStatus" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getPhoneSmsStatus(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getString("phone"), param.getString("smsCode"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		String smsCode = param.getString("smsCode");
		String phone = param.getString("phone");
		SmsDescribe sd = (SmsDescribe) JSONObject
				.toBean(JSONObject.fromObject(redisUtil.getKeyObject("verify_" + phone)), SmsDescribe.class);
		// 判断验证码是否正确
	
		if (null == sd || !sd.getSmsCode().equals(smsCode)) {
			return new MessageUtil(0, "账号或者验证码错误!");
		}
		return new MessageUtil(1, "账号验证码通过!");
	}
	
	
	/**
	 * 修改用户性别
	 * 
	 * @param userId
	 * @param sex
	 * @param response
	 */
	@RequestMapping(value = { "upateUserSex" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil upateUserSex(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId")) || param.getInt("sex") < 0 || param.getInt("sex") > 1) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		boolean redisLimitRegister = redisLimitRegister(param.get("userId"));
		if(redisLimitRegister) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.loginAppService.upateUserSex(param.getInt("userId"), param.getInt("sex"));
	}

	/**
	 * 修改主播的在先状态
	 * 
	 * @param userId
	 * @param type     0.空闲1.忙碌2.离线
	 * @param response
	 */
	@RequestMapping(value = { "updateAnchorOnline" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil updateAnchorOnline(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId")) || param.getInt("type") < 0 || param.getInt("type") > 2) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.loginAppService.updateAnchorOnline(param.getInt("userId"), param.getInt("type"));

	}

	/**
	 * 微信登陆
	 * 
	 * @param openId
	 * @param nickName 昵称
	 * @param handImg  头像
	 * @param city     城市
	 * @param sex      性别 0.女 1.男
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "weixinLogin" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil weixinLogin(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("openId", ""), param.getOrDefault("deviceNumber", ""),
				param.getOrDefault("t_phone_type", ""), param.getOrDefault("t_system_version", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		boolean redisLimitRegister = redisLimitRegister(param.get("openId"));
		if(redisLimitRegister) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.loginAppService.weixinLogin(param.getString("openId"), param.getString("nickName"),
				param.getOrDefault("handImg", "").toString(), param.getOrDefault("city", "").toString(),
				param.getString("t_phone_type"), param.getString("t_system_version"),
				null == param.get("ip") ? "" : param.getString("ip"), param.getString("deviceNumber"),
				param.containsKey("shareUserId") ? param.getInt("shareUserId") : 0);

	}

	/**
	 * QQ登陆
	 * 
	 * @param openId
	 * @param nickName
	 * @param handImg
	 * @param city
	 * @param sex
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "qqLogin" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil qqLogin(HttpServletRequest req, String openId, String nickName, String handImg, String city,
			String t_phone_type, String t_system_version, String deviceNumber) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("openId", ""), param.getOrDefault("deviceNumber", ""),
				param.getOrDefault("t_phone_type", ""), param.getOrDefault("t_system_version", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		boolean redisLimitRegister = redisLimitRegister(param.get("openId"));
		if(redisLimitRegister) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.loginAppService.qqLogin(param.getString("openId"), param.getString("nickName"),
				param.getOrDefault("handImg", "").toString(), param.getOrDefault("city", "").toString(),
				param.getString("t_phone_type"), param.getString("t_system_version"),
				null == param.get("ip") ? "" : param.getString("ip"), param.getString("deviceNumber"),
				param.containsKey("shareUserId") ? param.getInt("shareUserId") : 0);
	}

	/**
	 * 更新登陆时间
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = { "upLoginTime" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil upLoginTime(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		this.loginAppService.upLoginTime(param.getInt("userId"));

		return new MessageUtil(1, "更新成功!");
	}

	/**
	 * 登出
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = { "logout" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil logout(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.loginAppService.logout(param.getInt("userId"));
	}

	/**
	 * 拉取新用户注册信息
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = { "getPushMsg" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getPushMsg(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.loginAppService.getPushMsg(param.getInt("userId"));
	}

	/**
	 * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
	 * 
	 * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
	 * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
	 * 
	 * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
	 * 
	 * 用户真实IP为： 192.168.1.110
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request) {

		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 获取最新版本
	 * 
	 * @param response
	 */
	@RequestMapping(value = { "getNewVersion" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getNewVersion(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.loginAppService.getNewVersion();

	}

	/**
	 * ios 审核开关
	 * 
	 * @param response
	 */
	@RequestMapping(value = { "getIosSwitch" })
	@ResponseBody
	public MessageUtil getIosSwitch() {

		return new MessageUtil(1, 0);
	}

	/**
	 * 获取ios版本更新
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = { "getIosVersion" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getIosVersion(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.loginAppService.getIosVersion();

	}

	/**
	 * 手动绑定推广人
	 * 
	 * @param userId
	 * @param idCard
	 * @return
	 */
	@RequestMapping(value = "uniteIdCard", method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil uniteIdCard(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("idCard"))
				|| param.getInt("userId") < (param.getInt("idCard") - 10000)) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.loginAppService.uniteIdCard(param.getInt("userId"), param.getInt("idCard"));
	}

	/**
	 * 修改密码
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "upPassword", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil upPassword(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("phone", ""), param.getOrDefault("password", ""),
				param.getOrDefault("smsCode", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.loginAppService.upPassword(param.getString("phone"), param.getString("password"),
				param.getString("smsCode"));

	}

	/**
	 * 非vip首次登陆获取私信数
	 * @return
	 */
	@RequestMapping(value = "privateLetterNumber", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil privateChatNumber(HttpServletRequest req){
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.loginAppService.privateChatNumber(param.getInt("userId"));
	}

	/**
	 * 用户打招呼
	 * @return
	 */
	@RequestMapping(value = "greet", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil greet(HttpServletRequest req){
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", ""),param.getOrDefault("anchorUserId", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.loginAppService.greet(param.getInt("userId"),param.getInt("anchorUserId"));
	}

}
