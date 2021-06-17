package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.CommentService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * 评论相关借口
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class CommentControl {

	@Autowired
	private CommentService commentService;

	/**
	 * 对主播进行评价
	 * 
	 * @param commUserId      评论人
	 * @param coverCommUserId 被评论人
	 * @param commScore       评论分值
	 * @param lables          评论标签
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "saveComment" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil saveComment(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverCommUserId"), param.getInt("commScore"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.commentService.saveComment(param.getInt("userId"), param.getInt("coverCommUserId"),
				param.getInt("commScore"), param.getOrDefault("comment","").toString(), param.getString("lables"));

	}

}
