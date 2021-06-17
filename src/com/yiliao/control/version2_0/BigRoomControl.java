package com.yiliao.control.version2_0;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.version2_0.BigRoomService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;
import com.yiliao.util.SystemConfig;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("app")
public class BigRoomControl {

	@Autowired
	private BigRoomService bigRoomService;

	/**
	 * 获取大房间列表
	 * 
	 * @param userId
	 * @param page
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getBigRoomList" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getBigRoomList(HttpServletRequest req) {
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.bigRoomService.getBigRoomList(Integer.parseInt(param.getOrDefault("page", 1).toString()));
	}

	/**
	 * 开启直播
	 * 
	 * @param req
	 * @return
	 */
	@RequestMapping(value = { "openLiveTelecast" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil openLiveTelecast(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return this.bigRoomService.openLiveTelecast(param.getInt("userId"));
	}

	/**
	 * 关闭直播
	 * 
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "closeLiveTelecast" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil closeLiveTelecast(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return (MessageUtil) JSONObject.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "closeLiveTelecast.do",
						"userId=" + param.getInt("userId")),MessageUtil.class);
	}

	/**
	 * 用户加入房间
	 * 
	 * @param userId
	 * @param anchorId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "userMixBigRoom" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil userMixBigRoom(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("anchorId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		JSONObject json = HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "userMixBigRoom.do",
						"userId=" + param.getInt("userId") + "&anchorId=" + param.getInt("anchorId"));

		Map<String, Object>  map = (Map<String, Object>) json.get("m_object");

		return new MessageUtil(json.getInt("m_istatus"), map);
	}

	/**
	 * 用户退出房间
	 * 
	 * @param req
	 * @return
	 */
	@RequestMapping(value = { "userQuitBigRoom" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil userQuitBigRoom(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return (MessageUtil) JSONObject
				.toBean(HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "userQuitBigRoom.do",
						"userId=" + param.getInt("userId")), MessageUtil.class);
	}

	/**
	 * 获取贡献排行榜
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = { "getBigContributionList" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getContributionList(HttpServletRequest req) {

		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return this.bigRoomService.getContributionList(param.getInt("userId"), param.getInt("page"));
	}

	/**
	 * 获取个人主页信息
	 * 
	 * @param req
	 * @return
	 */
	@RequestMapping(value = { "getUserIndexData" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserIndexData(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return this.bigRoomService.getUserIndexData(param.getInt("userId"));
	}

	/**
	 * 获取房间中的用户列表
	 * 
	 * @param req
	 * @return
	 */
	@RequestMapping(value = { "getRoomUserList" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getRoomUserList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.bigRoomService.getRoomUserList(param.getInt("userId"), param.getInt("page"));
	}

	/**
	 * 获取主播封面
	 * 
	 * @param req
	 * @return
	 */
	@RequestMapping(value = { "getUserCoverImg" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserCoverImg(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.bigRoomService.getUserCoverImg(param.getInt("userId"));
	}

	/**
	 * 获取所有在播的大房间主播
	 * 
	 * @param req
	 * @return
	 */
	@RequestMapping(value = { "getTotalBigRoomList" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getTotalBigRoomList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return this.bigRoomService.getTotalBigRoomList();
	}

}
