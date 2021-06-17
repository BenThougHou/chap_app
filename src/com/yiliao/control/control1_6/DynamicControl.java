package com.yiliao.control.control1_6;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.service1_6.DynamicService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;
import com.yiliao.util.SystemConfig;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * 动态控制层
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class DynamicControl {

	@Autowired
	private DynamicService dynamicService;

	/**
	 * 获取动态列表
	 * 
	 * @param userId
	 * @param reqType
	 * @param page
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getUserDynamicList" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getDynamicList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		
		return this.dynamicService.getDynamicList(param.getInt("userId"),
					Integer.parseInt(param.getOrDefault("reqType", 0).toString()), param.getInt("page"));
		
	}

	/***
	 * 添加动态
	 * 
	 * @param userId
	 * @param title
	 * @param content
	 * @param address
	 * @param isVisible
	 * @param files
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "releaseDynamic" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil releaseDynamic(HttpServletRequest req) throws Exception {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if ((!BaseUtil.params(param.getOrDefault("userId", 0)))
				|| ((!StringUtils.isNotBlank(param.getOrDefault("content", "").toString()))
						&& (!StringUtils.isNotBlank(param.getOrDefault("files", "").toString())))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.releaseDynamic(param.getInt("userId"), param.getOrDefault("title", "").toString(),
				param.getString("content"), param.getString("address"), param.getInt("isVisible"),
				StringUtils.isNotBlank(param.getString("files")) ? JSONArray.fromObject(param.get("files"))
						: new JSONArray());
	}

	/**
	 * 评论动态
	 * 
	 * @param userId
	 * @param coverUserId
	 * @param comment
	 * @param dynamicId
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "discussDynamic" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil discussDynamic(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("comment", ""),
				param.getOrDefault("dynamicId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.discussDynamic(param.getInt("userId"), param.getInt("coverUserId"),
				param.getString("comment"), param.getInt("dynamicId"));
	}

	/**
	 * 动态点赞
	 * 
	 * @param userId
	 * @param dynamicId
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "giveTheThumbsUp" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil giveTheThumbsUp(HttpServletRequest req)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("dynamicId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.giveTheThumbsUp(param.getInt("userId"), param.getInt("dynamicId"));
	}

	/**
	 * 获取动态明细
	 * 
	 * @param userId
	 * @param dynamicId
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getDynamicDetails" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getDynamicDetails(HttpServletRequest req)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密

		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("dynamicId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.getDynamicDetails(param.getInt("userId"), param.getInt("dynamicId"));
	}

	/**
	 * 动态查看付费文件
	 * 
	 * @param userId
	 * @param fileId
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "dynamicPay" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil dynamicPay(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密

		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("fileId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.dynamicPay(param.getInt("userId"), param.getInt("fileId"));
	}

	/**
	 * 删除动态
	 * 
	 * @param userId
	 * @param dynamicId
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "delDynamic" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil delDynamic(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("dynamicId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return this.dynamicService.delDynamic(param.getInt("userId"), param.getInt("dynamicId"));
	}

	/**
	 * 获取评论列表
	 * @param userId
	 * @param dynamicId
	 * @param page
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getCommentList" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getCommentList(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("dynamicId", 0),
				param.getOrDefault("page", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.getCommentList(param.getInt("userId"), param.getInt("dynamicId"),
				param.getInt("page"));
	}

	/**
	 * 删除评论
	 * 
	 * @param userId
	 * @param commentId
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "delComment" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil delComment(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("commentId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.delComment(param.getInt("userId"), param.getInt("commentId"));
	}

	/**
	 * 发送通知
	 * 
	 * @param userId
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@RequestMapping(value = { "sendSocketNotice" }, method = { RequestMethod.POST })
	@ResponseBody
	public void sendSocketNotice(int userId) {

		HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "sendSocketNotice.do","userId=" + userId);
	}

	/**
	 * 获取点赞列表
	 * 
	 * @param userId
	 * @param dynamicId
	 * @param page
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getPraiseList" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getPraiseList(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 进行解密

		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("dynamicId", 0),
				param.getOrDefault("page", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return this.dynamicService.getPraiseList(param.getInt("userId"), param.getInt("dynamicId"),
				param.getInt("page"));
	}

	/**
	 * 获取用户是否有新的动态通知
	 * 
	 * @param userId
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getUserDynamicNotice" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserDynamicNotice(HttpServletRequest req)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 进行解密

		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.getUserDynamicNotice(param.getInt("userId"));
	}

	/**
	 * 获取最新评论消息
	 * 
	 * @param userId
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getUserNewComment" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getUserNewComment(HttpServletRequest req)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密

		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.getUserNewComment(param.getInt("userId"));
	}

	/**
	 * 获取自己的动态
	 * 
	 * @param userId
	 * @param page
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getOwnDynamicList" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getOwnDynamicList(HttpServletRequest req)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return this.dynamicService.getOwnDynamicList(param.getInt("userId"), param.getInt("page"));
	}

	/**
	 * 获取个人动态列表
	 * 
	 * @param userId
	 * @param coverUserId
	 * @param page
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "getPrivateDynamicList" }, method = { RequestMethod.POST, RequestMethod.GET })
	@ResponseBody
	public MessageUtil getPrivateDynamicList(HttpServletRequest req)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		// 进行解密

		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("coverUserId", 0),
				param.getOrDefault("page", 0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return this.dynamicService.getPrivateDynamicList(param.getInt("userId"), param.getInt("coverUserId"),
				param.getInt("reqType"), param.getInt("page"));
	}

}
