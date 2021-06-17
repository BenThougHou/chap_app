package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.MessageService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * 消息
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class MessageControl {

	@Autowired
	private MessageService messageService;

	/**
	 * 获取未读消息数
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getUnreadMessage", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUnreadMessage(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.messageService.getUnreadMessage(param.getInt("userId"));

	}

	/**
	 * 获取消息列表
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getMessageList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getMessageList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId",0), param.getOrDefault("page",0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.messageService.getMessageList(param.getInt("userId"), param.getInt("page"));
	}

	/**
	 * 设置为已读
	 * 
	 * @param messageId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "setupRead", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil setupRead(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId",0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.messageService.setupRead(param.getInt("userId"));

	}

	/**
	 * 跟新消息编号删除消息
	 * 
	 * @param messageId 消息编号
	 * @param response
	 */
	@RequestMapping(value = "delMessage", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil delMessage(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("messageId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.messageService.delMessage(param.getInt("messageId"));

	}
}
