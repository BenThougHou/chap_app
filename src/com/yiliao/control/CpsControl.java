package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.CpsService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * CPS相关接口
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class CpsControl {

	@Autowired
	private CpsService cpsService;

	/**
	 * 获取提现方式列表
	 * 
	 * @param response
	 */
	@RequestMapping(value = "getTakeOutMode", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getTakeOutMode() {

		JSONObject json = new JSONObject();
		json.put("0", "支付宝");
		json.put("1", "中国工商银行");
		json.put("2", "中国农业银行");
		json.put("3", "中国银行");
		json.put("4", "中国建设银行");

		return new MessageUtil(1, json);

	}

	/**
	 * 申请成为CPS联盟主
	 * 
	 * @param cpsName       CPS名称
	 * @param cpsUrl        CPS网址
	 * @param active        活跃用户
	 * @param proportions   分成比例
	 * @param realName      真名
	 * @param takeOutId     提现方式ID
	 * @param accountNumber 提现账号
	 * @param phone         联系电话
	 * @param response
	 */
	@RequestMapping(value = "addCpsMs", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil addCpsMs(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getString("cpsName"), param.getString("cpsUrl"),
				param.getInt("active"), param.getInt("proportions"), param.getString("realName"),
				param.getString("accountNumber"), param.getString("phone")) || param.getInt("takeOutId") < 0) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.cpsService.addCpsMs(param.getInt("userId"), param.getString("cpsName"), param.getString("cpsUrl"),
				param.getInt("active"), param.getInt("proportions"), param.getString("realName"),
				param.getInt("takeOutId"), param.getString("accountNumber"), param.getString("phone"));
	}

	/**
	 * 获取cps贡献列表
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@RequestMapping(value = "getMyContributionList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getContributionList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.cpsService.getContributionList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 获取cps统计数据
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getTotalDateil", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getTotalDateil(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.cpsService.getTotalDateil(param.getInt("userId"));
	}
}
