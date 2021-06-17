package com.yiliao.control;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import com.alibaba.fastjson.JSONObject;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.applepay.IosVerifyUtil;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.wxpay.sdk.WXPayUtil;
import com.yiliao.service.ConsumeService;
import com.yiliao.util.PrintUtil;

@Controller
@RequestMapping("pay")
public class PayCallbackControl {
	
	private static Logger logger = LoggerFactory
			.getLogger(PayCallbackControl.class);

	@Autowired
	private ConsumeService consumeService;
	
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	String FEATURE = null;
	
	/**
	 * 微信支付回调
	 * @param request
	 * @param response
	 */
	@RequestMapping("wxPayCallBack")
	@ResponseBody
	public void wxPayCallBack(HttpServletRequest request,
			HttpServletResponse response) {

		BufferedReader reader = null;

		try {
			
			FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
			dbf.setFeature(FEATURE, true);
			
			// If you can't completely disable DTDs, then at least do the following:
			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
			// JDK7+ - http://xml.org/sax/features/external-general-entities 
			FEATURE = "http://xml.org/sax/features/external-general-entities";
			dbf.setFeature(FEATURE, false);
			
			// Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
			// Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
			// JDK7+ - http://xml.org/sax/features/external-parameter-entities 
			FEATURE = "http://xml.org/sax/features/external-parameter-entities";
			dbf.setFeature(FEATURE, false);
			
			// Disable external DTDs as well
			FEATURE = "http://apache.org/xml/features/nonvalidating/load-external-dtd";
			dbf.setFeature(FEATURE, false);
			
			// and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
			dbf.setXIncludeAware(false);
			dbf.setExpandEntityReferences(false);
			
			// 读取微信发送的数据
			reader = request.getReader();
			request.setCharacterEncoding("UTF-8");
			response.setCharacterEncoding("UTF-8");
			response.setContentType("text/html;charset=UTF-8");
			response.setHeader("Access-Control-Allow-Origin", "*");

			String line = "";
			String xmlString = null;

			StringBuffer inputString = new StringBuffer();

			while ((line = reader.readLine()) != null) {
				inputString.append(line);
			}
			xmlString = inputString.toString();
			request.getReader().close();
			
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print("<xml><return_code><![CDATA[SUCCESS]]></return_code></xml>");
			out.flush();
			out.close();
			
			//字符串转为Map对象
			Map<String, String> xmlToMap = WXPayUtil.xmlToMap(xmlString);
			
			logger.info("return_code-->{},result_code-->{}",xmlToMap.get("return_code"),xmlToMap.get("result_code"));
			//支付成功!
			if("SUCCESS".equals(xmlToMap.get("return_code")) && "SUCCESS".equals(xmlToMap.get("result_code"))){
				
				this.consumeService.payNotify(xmlToMap.get("out_trade_no"), xmlToMap.get("transaction_id"));
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 支付pai回调
	 * 
	 * @param req
	 * @return
	 */
	@RequestMapping(value = { "alipayH5PayNotify" }, method = RequestMethod.POST)
	@ResponseBody
	String alipayH5PayNotify(HttpServletRequest req) {

		Map<String, String> params = convertRequestParamsToMap(req);

		if (1 == Integer.parseInt(params.get("trade_state").toString())) {
			this.consumeService.payNotify(params.get("out_trade_no").toString(), params.get("sys_trade_no").toString());
		}
		return "success";
	}
	
	/**
	 * <pre>
	 * 第一步:验证签名,签名通过后进行第二步
	 * 第二步:按一下步骤进行验证
	 * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
	 * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
	 * 3、校验通知中的seller_id（或者seller_email) 是否为out_trade_no这笔单据的对应的操作方（有的时候，一个商户可能有多个seller_id/seller_email），
	 * 4、验证app_id是否为该商户本身。上述1、2、3、4有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
	 * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
	 * 在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
	 * </pre>
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping("alipay_callback")
	@ResponseBody
	public  void  callback(HttpServletRequest request,HttpServletResponse response) {
		 
	    final Map<String, String> params = convertRequestParamsToMap(request); // 将异步通知中收到的待验证所有参数都存放到map中
		logger.info("支付宝回调，{}", params);
		if ("TRADE_SUCCESS".equals(params.get("trade_status")) || "TRADE_FINISHED".equals(params.get("trade_status"))){
			// 处理支付成功逻辑
			try {
				this.consumeService.getAlipayPublicKey(params);
			} catch (Exception e) {
				logger.error("支付宝回调业务处理报错,params:" + params, e);
			}
		} else {
			logger.error("没有处理支付宝回调业务，支付宝交易状态：{},params:{}",params.get("trade_status"), params);
		}
		// 如果签名验证正确，立即返回success，后续业务另起线程单独处理
		// 业务处理失败，可查看日志进行补偿，跟支付宝已经没多大关系。
		PrintUtil.printWriStr("success", response);
	}

	// 将request中的参数转换成Map
	@SuppressWarnings("unchecked")
	private static Map<String, String> convertRequestParamsToMap(
			HttpServletRequest request) {
		Map<String, String> retMap = new HashMap<String, String>();

		Set<Entry<String, String[]>> entrySet = request.getParameterMap().entrySet();

		for (Entry<String, String[]> entry : entrySet) {
			String name = entry.getKey();
			String[] values = entry.getValue();
			int valLen = values.length;

			if (valLen == 1) {
				retMap.put(name, values[0]);
			} else if (valLen > 1) {
				StringBuilder sb = new StringBuilder();
				for (String val : values) {
					sb.append(",").append(val);
				}
				retMap.put(name, sb.toString().substring(1));
			} else {
				retMap.put(name, "");
			}
		}

		return retMap;
	}

	/**
	 * 支付派H5支付回调
	 * @param req
	 */
	@RequestMapping(value = {"zhifupaiH5_callback"},method =RequestMethod.POST)
	@ResponseBody
	public void zhifupaiH5_callback(HttpServletRequest req, HttpServletResponse res) {
		// 将异步通知中收到的待验证所有参数都存放到map中
		try {
			Map<String, String> aprams = convertRequestParamsToMap(req);
			//支付成功!
			if(aprams.get("rt4_status").equals("SUCCESS")) {
				//订单金额 rt5_orderAmount
				//订单号 rt2_orderId
				// 实际支付金额 rt14_cashFee
				// 第三方订单号 rt11_channelOrderNum
				this.consumeService.zhifupaiH5_callback(aprams.get("rt2_orderId"),
						aprams.get("rt11_channelOrderNum"), new BigDecimal(aprams.get("rt23_paymentAmount")));

			}
			PrintUtil.printWriStr("SUCCESS", res);
		} catch (Exception e) {
			e.printStackTrace();
			PrintUtil.printWriStr("FAIL", res);
		}
	}

	/**
	 * 苹果内购校验
	 *
	 * @return MessageUtil
	 */

	@RequestMapping(value = {"iosPayCallback"}, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil iosPay(@RequestParam String orderNo, @RequestParam String transactionId, @RequestParam String payload) {

		try {
			logger.info("苹果内购校验开始，交易ID：" + transactionId + " base64校验体：" + payload);
			//线上环境验证
			String verifyResult = IosVerifyUtil.buyAppVerify(payload, 1);
			if (verifyResult == null) {
				return new MessageUtil(-600, "苹果验证失败，返回数据为空!");
			} else {
				logger.info("线上，苹果平台返回JSON:" + verifyResult);
				JSONObject appleReturn = JSONObject.parseObject(verifyResult);
				String states = appleReturn.getString("status");
				//无数据则沙箱环境验证
				if ("21007".equals(states)) {
					verifyResult = IosVerifyUtil.buyAppVerify(payload, 0);
					logger.info("沙盒环境，苹果平台返回JSON:" + verifyResult);
					appleReturn = JSONObject.parseObject(verifyResult);
					states = appleReturn.getString("status");
				}
				logger.info("苹果平台返回值：appleReturn" + appleReturn);
				// 前端所提供的收据是有效的    验证成功
				if (states.equals("0")) {
					String receipt = appleReturn.getString("receipt");
					JSONObject returnJson = JSONObject.parseObject(receipt);
					String inApp = returnJson.getString("in_app");
					List<HashMap> inApps = JSONObject.parseArray(inApp, HashMap.class);
					if (!CollectionUtils.isEmpty(inApps)) {
						ArrayList<String> transactionIds = new ArrayList<String>();
						for (HashMap app : inApps) {
							transactionIds.add((String) app.get("transaction_id"));
						}
						//交易列表包含当前交易，则认为交易成功
						if (transactionIds.contains(transactionId)) {
							//处理业务逻辑
							this.consumeService.payNotify(orderNo, String.valueOf(System.currentTimeMillis()));
							return new MessageUtil(1, "支付成功!");
						}

					}
					return new MessageUtil(-600, "未能获取获取到交易列表!");
				} else {
					return new MessageUtil(-600, "支付失败，错误码!", states);

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new MessageUtil(-600, "系统异常！");
	}


}

