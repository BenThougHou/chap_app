package com.yiliao.control;


import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qiniu.common.QiniuException;
import com.yiliao.service.VideoChatService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;
import com.yiliao.util.RedisUtil;
import com.yiliao.util.SystemConfig;
import com.yiliao.util.qiniu.ResourcesCensor;

import net.sf.json.JSONObject;

/**
 * 视频聊天控制层
 * 
 * @author Administrator
 * 
 */
@Controller
@RequestMapping("app")
public class VideoChatControl {

	@Autowired
	private VideoChatService videoChatService;
	@Autowired
	private RedisUtil redis; 
	/**
	 * 获取速配房间号
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getSpeedDatingRoom" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getSpeedDatingRoom(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return (MessageUtil) JSONObject
				.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "getSpeedDatingRoom.do","userId=" + param.getInt("userId")),
						MessageUtil.class);
	}

	/**
	 * 获取IM签名
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getImUserSig")
	@ResponseBody
	public MessageUtil getImUserSig(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.videoChatService.getImUserSig(param.getInt("userId"));

	}

	/**
	 * 根据用户编号获取签名 步骤1
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getVideoChatAutograph")
	@ResponseBody
	public MessageUtil getVideoChatUserSig(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("anthorId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.videoChatService.getVideoChatAutograph(param.getInt("userId"), param.getInt("anthorId"));

	}

	/**
	 * 获取PriavteMapKey
	 * 
	 * @param userId   用户编号
	 * @param roomId   房间号
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getVideoChatPriavteMapKey")
	@ResponseBody
	public MessageUtil getVideoChatPriavteMapKey(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("roomId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.videoChatService.getVideoChatPriavteMapKey(param.getInt("userId"), param.getInt("roomId"));

	}

	/**
	 * 用户对主播发起视频聊天 步骤2
	 * 
//	 * @param launchUserId 发起人
//	 * @param coverLinkUserId 被连接人
//	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("launchVideoChat")
	@ResponseBody
	public MessageUtil launchVideoChat(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("coverLinkUserId", 0),
				param.getOrDefault("chatType", 1)) || param.getInt("userId") == param.getInt("coverLinkUserId")) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		// 1:拨打  2:接听
		Boolean flag=videoChatService.checkVipSwitch(1,param.getInt("userId"));
		if(flag) {
//			return new MessageUtil(-7, "非常抱歉,VIP享有拨打视频权限!");
			return new MessageUtil(-7, "VIP用户才能和附近美女视频互动哦~");
		}

		int chatType =(int) param.getOrDefault("chatType",1);
//		int chatType =1;
//		正常流程呼叫  1:视频  2:语音 
//		自动呼叫 主播呼用户
//		chatType：3 视频 4：语音
//		自动呼叫 用户呼主播
//		chatType：5 视频 6：语音
		int switchType=1;
		if(chatType==1||chatType==3||chatType==5) {
			switchType=1;
		}
		if(chatType==2||chatType==4||chatType==6) {
			switchType=2;
		}
//		switchType 1:视频  2:语音 3:文字聊天 4:黑名单  返回 false:VIP打开 并判断是否为VIP
		if(switchType==1) {
			flag=videoChatService.checkChatSwitch(switchType,param.getInt("coverLinkUserId"));
			if(flag) {
				return new MessageUtil(-5, "Sorry,对方设置了视频勿扰!");
			}
		}else if(switchType==2) {
			flag=videoChatService.checkChatSwitch(switchType,param.getInt("coverLinkUserId"));
			if(flag) {
				return new MessageUtil(-5, "Sorry,对方设置了语音勿扰!");
			}
		}
		//黑名单判断 true 被拉入黑名单
		flag=videoChatService.checkBlackUserInfo(param.getInt("userId"),param.getInt("coverLinkUserId"));
		if(flag) {
			return new MessageUtil(-5, "Sorry,对方已将您拉入黑名单!");
		}

//		flag =  videoChatService.vipSwitch(param.getInt("userId"),param.getInt("coverLinkUserId"));
//		if (!flag){
//			return new MessageUtil(-6, "女神今日免费聊天次数已被其他SVIP占用，如继续拨打，会产生费用!");
//		}

		return (MessageUtil) JSONObject
				.toBean(HttpUtil
						.httpClentJson(SystemConfig.getValue("chat_mina_url") + "launchVideoChat.do",
								"userId=" + param.getInt("userId") + "&coverLinkUserId="
										+ param.getInt("coverLinkUserId")
										+ "&roomId=" + param.getInt("roomId")
										+ "&chatType=" + chatType),
						MessageUtil.class);

	}

	/**
	 * svip免费通话次数校验
	 * @param req
	 * @return
	 */
	@RequestMapping("svipCheck")
	@ResponseBody
	public MessageUtil svipCheck(HttpServletRequest req){
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("coverLinkUserId", 0)
				, param.getInt("userId") == param.getInt("coverLinkUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

//		return  videoChatService.vipSwitch(param.getInt("userId"),param.getInt("coverLinkUserId"));
		return  null;
	}

	/**
	 * 开始计时 步骤3
	 * 
	 * @param launchUserId
	 * @param coverLinkUserId
	 * @param roomId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("videoCharBeginTiming")
	@ResponseBody
	public MessageUtil videoCharBeginTiming(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("anthorId", 0), param.getOrDefault("userId", 0),param.getOrDefault("chatType", 1),
				param.getOrDefault("roomId", 0)) || param.getInt("anthorId") == param.getInt("userId")) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		int chatType=(int) param.getOrDefault("chatType",1);
		//主播拨打用户 限制VIP接听
		String anthorTo = redis.get("VIP_USER_"+param.getInt("anthorId")+"_"+param.getInt("userId"));
		Boolean flag=videoChatService.checkVipSwitch(2,param.getInt("userId"));
		if(anthorTo!=null&&anthorTo.equals("1")&&flag) {
			return new MessageUtil(-7, "音视频功能只有VIP可使用!");
		}
		return (MessageUtil) JSONObject  
				.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "videoCharBeginTiming.do",
						"userId=" + param.getInt("userId") + "&anthorId=" + param.getInt("anthorId") + "&roomId="
								+ param.getInt("roomId")+ "&chatType="
										+ chatType),
						MessageUtil.class);

	}

	/**
	 * 断开链接 步骤4
	 * 
	 * @param userId
	 * @param roomId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("breakLink")
	@ResponseBody
	public MessageUtil breakLink(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("roomId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		Object breakType = param.getOrDefault("breakType", 1);
		return (MessageUtil) JSONObject
				.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "breakLink.do",
						"userId=" + param.getInt("userId") +"&breakType=" +breakType + "&roomId=" + param.getInt("roomId")), MessageUtil.class);

	}

	/**
	 * 用户挂端链接
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("userHangupLink")
	@ResponseBody
	public MessageUtil userHangupLink(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return (MessageUtil) JSONObject
				.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "userHangupLink.do",
						"userId=" + param.getInt("userId")), MessageUtil.class);
	}

	/**
	 * 主播对用户发起聊天 步骤2
	 * 
	 * @param anchorUserId
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("anchorLaunchVideoChat")
	@ResponseBody
	public MessageUtil anchorLaunchVideoChat(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("anchorUserId", 0), param.getOrDefault("userId", 0),param.getOrDefault("chatType",1),
				param.getOrDefault("roomId", 0)) || param.getInt("anchorUserId") == param.getInt("userId")) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		// 1:拨打  2:接听
		Boolean flag=false;
		
		int chatType =(int) param.getOrDefault("chatType",1);
		int switchType=1;
		if(chatType==1||chatType==3||chatType==5) {
			switchType=1;
		}
		if(chatType==2||chatType==4||chatType==6) {
			switchType=2;
		}
		
		
		// 1:拨打  2:接听  3:主播拨打
		flag=videoChatService.checkVipSwitch(3,param.getInt("anchorUserId"));
		if(flag) {
			return new MessageUtil(-7, "VIP主播才能和用户视频互动哦~");
		}
		
		
		
		
//		int chatType=1;
		// 1:视频  2:语音 3:文字聊天
		if(switchType==1) {
			flag=videoChatService.checkChatSwitch(switchType,param.getInt("userId"));
			if(flag) {
				return new MessageUtil(-5, "Sorry,对方设置了视频勿扰!");
			}
		}else if(switchType==2) {
			flag=videoChatService.checkChatSwitch(switchType,param.getInt("userId"));
			if(flag) {
				return new MessageUtil(-5, "Sorry,对方设置了语音勿扰!");
			}
		}
		//黑名单判断 true 被拉入黑名单
		flag=videoChatService.checkBlackUserInfo(param.getInt("anchorUserId"),param.getInt("userId"));
		if(flag) {
			return new MessageUtil(-5, "Sorry,对方已将您拉入黑名单!");
		}
		
		return (MessageUtil) JSONObject
				.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "anchorLaunchVideoChat.do",
						"userId=" + param.getInt("userId") + "&anchorId=" + param.getInt("anchorUserId") + "&roomId="
								+ param.getInt("roomId")
								+ "&chatType="
										+ chatType),
						MessageUtil.class);
	}

	/***
	 * 获取当前用户是否被呼叫【弹框作用】 步骤3
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getUuserCoverCall")
	@ResponseBody
	public MessageUtil getUuserCoverCall(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		JSONObject httpClentJson = HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "getUserCoverCall.do",
				"userId=" + param.getInt("userId"));
		MessageUtil messageUtil = new MessageUtil(1,"成功");
		messageUtil.setM_object(httpClentJson);
		return messageUtil;
	}

	/**
	 * 获取房间状态
	 * 
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getRoomState" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getRoomState(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("roomId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return (MessageUtil) JSONObject
				.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "getUuserCoverCall.do",
						"roomId=" + param.getInt("roomId")), MessageUtil.class);
	}
	
	
	/**
	 * 获取视频截图参数
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getVideoScreenshotStatus")
	@ResponseBody
	public MessageUtil getVideoScreenshotStatus (HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.videoChatService.getVideoScreenshotStatus();
	}
	
	
	/**
	 * 新增视频截图参数
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("addVideoScreenshotInfo")
	@ResponseBody
	public MessageUtil addVideoScreenshotInfo(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.videoChatService.addVideoScreenshotInfo(
				param.getInt("userId"),
				param.getInt("videoUserId"),
				param.getInt("videoAnchorUserId"),
				param.getInt("roomId"),
				param.getString("videoImgUrl"));
	}
	
	/**
	 * 获取视频截图结果
	 * 
//	 * @param userId
//	 * @param response
	 * @throws QiniuException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getQiNiuKey")
	@ResponseBody
	public MessageUtil getQiNiuKey(HttpServletRequest req) throws QiniuException {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		String imgData = param.getString("imgData");
		
		String qiNiuKey = new ResourcesCensor().getQiNiuKey(imgData);
		MessageUtil messageUtil = new MessageUtil(1, "获取成功");
		messageUtil.setM_object(qiNiuKey);
		return messageUtil;
	}
	
	/**
	 * 获取房间状态 视频链接之后轮询是否在计费
	 * @param req
	 * @return
	 */
	@RequestMapping(value= {"getVideoStatus"},method = RequestMethod.POST )
	@ResponseBody
	public MessageUtil getVideoStatus(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId",0),param.getOrDefault("userId",0),param.getOrDefault("roomId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return (MessageUtil) JSONObject
				.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "getVideoStatus.do",
						"videoUserId=" + param.getInt("videoUserId")
						 + "&videoCoverUserId=" + param.getInt("videoCoverUserId") 
						 + "&userId=" + param.getInt("userId") 
						 + "&roomId=" + param.getInt("roomId") ), MessageUtil.class);
	}

	/**
	 * 获取房间状态 视频链接之后轮询是否在计费
	 * @param req
	 * @return
	 */
	@RequestMapping(value= {"svipSwitch"},method = RequestMethod.POST )
	@ResponseBody
	public MessageUtil svipSwitch(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("coverLinkUserId", 0),
				param.getInt("userId") == param.getInt("coverLinkUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.videoChatService.svipSwitch(param.getInt("userId"),param.getInt("coverLinkUserId"));
	}

}
