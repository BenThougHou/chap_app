package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.SystemService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("app")
public class SystemControl {

	@Autowired
	private SystemService systemService;

	/**
	 * 获取私密相册收费标准
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getPrivatePhotoMoney", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getPrivatePhotoMoney(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId",0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}

		return this.systemService.getPrivatePhotoMoney();

	}

	/**
	 * 获取私密视频收费项
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getPrivateVideoMoney", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getPrivateVideoMoney(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId",0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}

		return this.systemService.getPrivateVideoMoney();
	}

	/**
	 * 获取主播收费设置
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnthorChargeList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnthorChargeList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId",0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}

		return this.systemService.getAnthorChargeList();
	}

	/**
	 * 获取认证微信号
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getIdentificationWeiXin", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getIdentificationWeiXin(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId",0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}

		return this.systemService.getIdentificationWeiXin();

	}

	/**
	 * 获取客服QQ
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getServiceQQ", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getServiceQQ(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId",0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}

		return this.systemService.getServiceQQ(param.getInt("userId"));
	}

	/**
	 * 获取帮助列表
	 * 
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getHelpContre" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getHelpContre(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId",0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.systemService.getHelpContre(param.getInt("userId"));
	}

	/**
	 * 加载预览图
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = { "onloadGlanceOver" })
	@ResponseBody
	public void onloadGlanceOver(HttpServletRequest req, HttpServletResponse response) {
		// 解密参数
		if (BaseUtil.params(req.getParameter("userId"))) {
			this.systemService.onloadGlanceOver(Integer.parseInt(req.getParameter("userId").toString()), response);
		}
	}
}
