package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.BlackUserService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 *	黑名单
 */
@Controller
@RequestMapping("app")
public class BlackUserController {
	
	@Autowired
	private BlackUserService blackUserService;

	/**
	 * 	拉入黑名单
	 * @param key
	 * @return
	 */
	@RequestMapping(value = { "addBlackUser" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil addBlackUser(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getString("userId"),param.getString("cover_userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		String  t_user_id = param.getString("userId");
		String t_cover_user_id = param.getString("cover_userId");
		return blackUserService.addBlackUser(t_user_id,t_cover_user_id);
	}
	
	/**
	 * 	黑名单列表
	 * @param key
	 * @return
	 */
	@RequestMapping(value = { "getBlackUserList" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getBlackUserList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getString("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		String userId = param.getString("userId");
		Integer page = Integer.parseInt(param.getOrDefault("page","1").toString());
		Integer size = Integer.parseInt(param.getOrDefault("size","10").toString());
		
		return blackUserService.getBlackUserList(userId,page,size);
	}
	
	/**
	 * 	删除黑名单
	 * @param key
	 * @return
	 */
	@RequestMapping(value = { "delBlackUser" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil delBlackUser(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getString("userId"),param.getString("t_id"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		String userId = param.getString("userId");
		String t_id = param.getString("t_id");
		return blackUserService.delBlackUser(userId,t_id);
	}
	
}
