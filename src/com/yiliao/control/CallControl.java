package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.CallLogService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * 通话记录
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class CallControl {

	@Autowired
	private CallLogService callLogService;

	/**
	 * 获取我的通话记录
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@RequestMapping(value = { "getCallLog" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getCallLog(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.callLogService.getCallLog(param.getInt("userId"), param.getInt("page"));
	}

}
