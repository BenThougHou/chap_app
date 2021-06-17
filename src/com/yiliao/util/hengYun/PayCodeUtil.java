package com.yiliao.util.hengYun;


import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.fastjson.JSON;
import com.yiliao.util.HttpClientUtil;
import com.yiliao.util.Utilities.MD5;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.yiliao.util.HttpUtil;
import com.yiliao.util.Md5Util;
import com.yiliao.util.SystemConfig;

import net.sf.json.JSONObject;


public class PayCodeUtil {

	private static Logger logger = LogManager.getLogger(PayCodeUtil.class);

	public static String UID2 = "";
	//商户Id
	public static String MID2 = "";
//	public static String NOTIFY_URL = "http://47.89.12.190/pay/alipayH5PayNotify.html";
//	public static String NOTIFY_URL = "http://r203966a62.iok.la/pay/alipayH5PayNotify.html";
	
//	public static String RETURN_URL = "http://47.89.12.190/success.jsp";
//	public static String RETURN_URL = "http://r203966a62.iok.la/success.jsp";
	
	public static String BASE_URL = "https://pay.paysapi.com";

	public static String PAY_URL = "https://pay.bearsoftware.net.cn/";
	
	//密钥
	public static String TOKEN2 = "mhcm5xolze9dog4ilv1pcymt15gmjyvb";

	
	/**
	 * 支付宝h5支付
	 * 
	 * @return
	 */
	public static Map<String, Object> payAliPayH5(String url,String orderNo, BigDecimal amount, String body,String MID,String TOKEN,String pay_type) {
		
//		String url = "https://pay.iisck.cn/gateway/h5pay2";
//		String url2 = "https://pay.iisck.cn/gateway/pay";
//		switch (pay_type) {
//		case "101":
//			url=url2;
//			break;
//		}
		StringBuffer sign = new StringBuffer();
		sign.append("version=").append("1.0"); // 版本
		sign.append("&mch_id=").append(MID); // 商户号
		sign.append("&pay_type=").append(pay_type); // 类型
		sign.append("&total_amount=").append(amount.multiply(new BigDecimal(100)).intValue()); // 金额 单位(分)
		sign.append("&out_trade_no=").append(orderNo); // 订单号
		sign.append("&notify_url=").append(SystemConfig.getValue("h5NotifyUrl")); // 回调地址
		String sign_m = Md5Util.encodeByMD5ByLow(sign.toString() + "&key="+TOKEN); // sign
		sign.append("&return_url=").append(SystemConfig.getValue("h5ReturnUrl")); // 回调地址
		sign.append("&fee_type=").append("CNY"); // 币种类型
		sign.append("&sp_client_ip=").append(SystemConfig.getValue("spbill_create_ip")); // IP地址
		sign.append("&sign=").append(sign_m); // 签名
		sign.append("&device_info=0"); // 设备类型
		sign.append("&body=").append(body+System.currentTimeMillis()); // 商品描述

		System.out.println("h5支付发送数据->" + sign.toString());

		JSONObject clentJson = HttpUtil.httpHengYunClentJson(url, sign.toString());

		if (clentJson.getInt("result_code") == 0)
			return clentJson;
		return new HashMap<String, Object>();
	}
//	public static String zhifupayH5(String orderId,BigDecimal amount,Integer userId,String goodsName) {
//
//		String url = "https://www.sys.cnjrbank.com/middle-stage/quicktransaction/appInterface";
//
//		Map<String,String> hashMap = new LinkedHashMap<String, String>() {
//			private static final long serialVersionUID = 1L;
//			{
//				put("P1_bizType", "AppRandomQuickPayHtml");
//				put("P2_orderId", orderId);
//				put("P3_customerNumber", "20191113150000XX9CD2ED68E2C34A67");
//				put("P4_orderAmount", amount.setScale(2, BigDecimal.ROUND_DOWN).toString());
//				put("P5_currency", "CNY");
//				put("P6_appType", "ALIPAY");
//				put("P7_userId", userId.toString());
//				put("P8_notifyUrl", SystemConfig.getValue("alipayH5zhifupaiNotify"));
//				put("P9_successToUrl", "");
//				put("P10_falseToUrl", "");
//				put("P11_goodsName", goodsName);
//				put("P12_goodsDetail", goodsName);
//			}
//		};
//
//		String signStr = "&";
//
//		for (Map.Entry<String, String> m : hashMap.entrySet()) {
//
//			signStr = signStr + m.getValue()+"&";
//		}
//
//		System.out.println("签名前的字符串->"+signStr+"201911135700XX511E84E83DC04121");
//
//		String sign = Md5Util.encodeByMD5ByLow(signStr+"201911135700XX511E84E83DC04121");
//
//		System.out.println("签名后的sign->"+sign);
//
//		hashMap.put("sign", sign);
//
//		String clentJson = HttpClientUtil.post(url, hashMap, "UTF-8");
//
//		JSONObject json = JSONObject.fromObject(clentJson);
//
//		System.out.println("支付渠道返回数据->"+json.toString());
//
//		if(json.getString("rt2_retCode").equals("0000")) {
//
//			System.out.println("进入了获取跳转url");
//
//			return json.getString("rt10_appPayUrl");
//		}
//
//		return "";
//	}

	/**
	 *  支付派支付
	 * @param orderId 订单id
	 * @param amount 金额
	 * @param userId 用户id
	 * @param goodsName 商城名字
	 * @return
	 */
	public static String zhifupayH5(String alipay_customerNumber,String alipayH5zhifupaiNotify,String orderId,BigDecimal amount,Integer userId,String goodsName,String appType) {
		String url = "https://www.sys.cnjrbank.com/middle-stage/quicktransaction/appInterface";

		Map<String,String> hashMap = new LinkedHashMap<String, String>() {
			private static final long serialVersionUID = 1L;
			{
				put("P1_bizType", "AppRandomQuickPayHtml");
				put("P2_orderId", orderId);
				// 支付派分批的商户号【商户ID】
				put("P3_customerNumber",alipay_customerNumber);
				put("P4_orderAmount", amount.setScale(2, BigDecimal.ROUND_DOWN).toString());
				put("P5_currency", "CNY");
				put("P6_appType", appType); //WXPAY:微信 ALIPAY：支付宝
				put("P7_userId", userId.toString());
				// 通知回调结果
				put("P8_notifyUrl", alipayH5zhifupaiNotify);
				put("P9_successToUrl", "");
				put("P10_falseToUrl", "");
				put("P11_goodsName", goodsName);
				put("P12_goodsDetail", goodsName);
			}
		};

		String signStr = "&";
		for (Map.Entry<String, String> m : hashMap.entrySet()) {
			signStr = signStr + m.getValue()+"&";
		}

		// 密钥
		String sign = Md5Util.encodeByMD5ByLow(signStr+SystemConfig.getValue("alipay_secret_key"));
		hashMap.put("sign", sign);
		String clentJson = HttpClientUtil.post(url, hashMap, "UTF-8");
		JSONObject json = JSONObject.fromObject(clentJson);
		if(json.getString("rt2_retCode").equals("0000")) {
			return json.getString("rt10_appPayUrl");
		}
		return null;
	}

	/**
	 *  支付派地址转换，换成app或微信支付
	 * @param orderId 订单id
	 * @param amount 金额
	 * @param userId 用户id
	 * @param goodsName 商城名字
	//	 * @param alipayH5zhifupaiType 支付类型 app/(微信)wx/(支付宝)zfb
	 * @return
	 */
	public static Map<String, Object> zhifupayUrl(String userName,String alipay_customerNumber,String alipayH5zhifupaiNotify,String orderId, BigDecimal amount, Integer userId, String goodsName,String appType){
		String appPayUrl = zhifupayH5(alipay_customerNumber,alipayH5zhifupaiNotify,orderId,amount,userId,goodsName,appType);
		if (appPayUrl != null){
			Map<String, Object> map = new HashMap<>();
			//支付派小程序支付原始原始ID【微信支付时必须】
			map.put("userName",SystemConfig.getValue("userName"));
			//请求地址
			map.put("path",appPayUrl);
			String alipayH5zhifupaiType ="wx";
			//截取字符串中必须参数
			String[] s = appPayUrl.split("\\?");
			s = s[1].split("&");
			String customerNumber = s[0].split("=")[1];
			String templateOrderId = s[1].split("=")[1];
			if ("wx".equals(alipayH5zhifupaiType)){
				map.put("path","pages/minipayment/minipayment?customerNumber="+customerNumber+"&templateOrderId="+templateOrderId);
				return map;
			}else if ("app".equals(alipayH5zhifupaiType)){
				map.put("path","pages/minipay/minipay?customerNumber="+customerNumber+"&templateOrderId="+templateOrderId);
				return map;
			}
		}
		throw new RuntimeException("提交支付信息异常");
	}

	/**
	 * 千百付支付
	 *
	 * @param url      请求地址
	 * @param orderNo  订单号
	 * @param amount   金额（单位元）
	 * @param body     订单描述
	 * @param mchId    商户号
	 * @param key
	 * @param pay_type 支付类型 101 支付宝 102 微信
	 * @return
	 */
	public static Map < String, Object > hundredsOfPay(String url, String orderNo, BigDecimal amount, String body, String mchId, String key, String pay_type) {
		Map < String, String > map = new LinkedHashMap <>();
		map.put("service", "1101");
		map.put("mch_id", mchId);
		map.put("pay_type", pay_type);
		map.put("pay_way", "js");
		map.put("body", body);
		map.put("mch_order_id", orderNo);
		map.put("order_money", amount.toString());
		//回调地址
		map.put("notify_url", SystemConfig.getValue("h5NotifyUrl"));
		//跳转地址
		map.put("callback_url", SystemConfig.getValue("h5ReturnUrl"));

		map.put("create_ip", SystemConfig.getValue("spbill_create_ip"));
		map.put("nonce_str", RandomUtil.randomString(32));
		map.put("key", key);
		Map < String, String > params = paraFilter(map);
		StringBuilder buf = new StringBuilder((params.size() + 1) * 10);
		buildPayParams(buf, params, false);
		String preStr = buf.toString();
		String sign = MD5.MD5Encoder(preStr, "UTF-8").toLowerCase();
		map.put("sign", sign);
		String str = HttpUtil.dopost(url, map);
		Map maps = (Map) JSON.parse(str);
		String status = maps.get("status").toString();
		if (StringUtils.equals("200", status)) {
			return maps;
		} else {
			return new HashMap < String, Object >();
		}
	}

	public static Map < String, String > paraFilter(Map < String, String > sArray) {
		Map < String, String > result = new HashMap < String, String >(sArray.size());
		if (sArray == null || sArray.size() <= 0) {
			return result;
		}
		for (String key : sArray.keySet()) {
			String value = sArray.get(key);
			if (value == null || value.equals("") || key.equalsIgnoreCase("sign")) {
				continue;
			}
			result.put(key, value);
		}
		return result;
	}


	public static void buildPayParams(StringBuilder sb, Map < String, String > payParams, boolean encoding) {
		List< String > keys = new ArrayList < String >(payParams.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			sb.append(key).append("=");
			if (encoding) {
				sb.append(urlEncode(payParams.get(key)));
			} else {
				sb.append(payParams.get(key));
			}
			sb.append("&");
		}
		sb.setLength(sb.length() - 1);
	}

	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, "UTF-8");
		} catch (Throwable e) {
			return str;
		}
	}

}
