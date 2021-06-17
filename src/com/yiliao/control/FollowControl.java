package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import com.yiliao.domain.entity.Follow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.FollowService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * 关注控制层
 * 
 * @author Administrator
 * 
 */
@Controller
@RequestMapping("app")
public class FollowControl {

	@Autowired
	private FollowService followService;

	/**
	 * 获取关注列表列表
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@RequestMapping(value = "getFollowList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getFollowList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.followService.getFollowList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 添加关注
	 * 
//	 * @param followUserId      关注人
//	 * @param coverFollowUserId 被关注人
//	 * @param response
	 */
	@RequestMapping(value = "saveFollow", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil saveFollow(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverFollowUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		Follow follow = new Follow();
		follow.setT_follow_id(param.getInt("userId"));
		follow.setT_cover_follow(param.getInt("coverFollowUserId"));
		return this.followService.saveFollow(follow);

	}

	/**
	 * 删除关注
	 * 
	 * @param followId
	 * @param response
	 */
	@RequestMapping(value = "delFollow", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil delFollow(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverFollow"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.followService.delFollow(param.getInt("userId"), param.getInt("coverFollow"),0);

	}

	/**
	 * 获取指定用户是否关注
	 * 
	 * @param userId
	 * @param coverFollow
	 * @param response
	 */
	@RequestMapping(value = "getSpecifyUserFollow", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getSpecifyUserFollow(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverFollow"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.followService.getSpecifyUserFollow(param.getInt("userId"), param.getInt("coverFollow"));

	}
}
