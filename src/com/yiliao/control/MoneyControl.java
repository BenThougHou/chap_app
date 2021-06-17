package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.MoneyService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * 
 * @author Administrator
 * 
 *         查询用户钱包
 */
@Controller
@RequestMapping("app")
public class MoneyControl {

	@Autowired
	private MoneyService moneyService;

	/**
	 * 查询用户钱包
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getUserMoney", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserMoney(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.getUserMoney(param.getInt("userId"));

	}

	/**
	 * 查询用户的余额
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getQueryUserBalance", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getQueryUserBalance(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.getQueryUserBalance(param.getInt("userId"));

	}

	/**
	 * 获取提现折扣
	 * t_end_type -2:微信 -1:支付宝
	 * @param response
	 */
	@RequestMapping(value = "getPutforwardDiscount", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getPutforwardDiscount(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId")) || param.getInt("t_end_type") < -7
				|| param.getInt("t_end_type") > -1) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.getPutforwardDiscount(param.getInt("t_end_type"));

	}

	/**
	 * 获取充值折扣
	 * t_end_type -1.支付宝  -2.微信
	 * @param response
	 */
	@RequestMapping(value = "getRechargeDiscount", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getRechargeDiscount(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId")) && !BaseUtil.params(param.getInt("t_end_type"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.getRechargeDiscount(param.getInt("t_end_type"));

	}

	/**
	 * 获取可提现金币
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getUsableGold", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUsableGold(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.getUsableGold(param.getInt("userId"));

	}

	/**
	 * 用户更新提现资料
	 * 
	 * @param userId
	 * @param t_real_name
	 * @param t_account_number
	 * @param t_type
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "modifyPutForwardData", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil modifyPutForwardData(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getString("t_real_name"),
				param.getString("t_account_number"),param.getOrDefault("qrCodeUrl", "")) && (param.getInt("t_type") < 0 || param.getInt("t_type") > 1)) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.modifyPutForwardData(param.getInt("userId"), param.getString("t_real_name"),
				param.getOrDefault("t_nick_name","").toString(), param.getString("t_account_number"), param.getInt("t_type"),
				param.getOrDefault("t_head_img","").toString(),param.getString("qrCodeUrl"));

	}

	/**
	 * 申请提现
	 * 
	 * @param dataId
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "confirmPutforward", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil confirmPutforward(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("dataId"), param.getInt("putForwardId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.confirmPutforward(param.getInt("dataId"), param.getInt("userId"),
				param.getInt("putForwardId"));

	}

	/**
	 * 获取用户金币明细
	 * 
	 * @param userId
	 * @param year
	 * @param month
	 * @param queryType
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getUserGoldDetails", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserGoldDetails(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId",0), param.getOrDefault("year",0), param.getOrDefault("month",0), param.getOrDefault("page",0))
				|| param.getInt("queryType") < -1 || param.getInt("queryType") > 1) {
			// 返回数据

			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.getUserGoldDetails(param.getInt("userId"), param.getInt("year"), param.getInt("month"),
				param.getInt("queryType"), param.getInt("page"));

	}

	/**
	 * 获取指定月份的的收入与支付
	 * 
	 * @param userId
	 * @param year
	 * @param monty
	 * @param response
	 */
	@RequestMapping(value = "getProfitAndPayTotal", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getProfitAndPayTotal(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("year"), param.getInt("month"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.moneyService.getProfitAndPayTotal(param.getInt("userId"), param.getInt("year"),
				param.getInt("month"));
	}
	
	/**
	 * 获取支付配置列表
	 * @param req
	 * @return
	 */
	@RequestMapping(value= {"getPayDeployList"},method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getPayDeployList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return moneyService.getPayDeployList();
	}

}
