package com.yiliao.util;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;
import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;

public class PayUtil {
	/**
	 * 支付宝支付
	* @param orderNo 订单号
	 * @param payAmount 支付金额
	 * @param projectName 项目名称
	 * @param alipay_appid 支付宝appId
	 * @param alipay_private_key 支付宝秘钥
	 * @param alipay_public_key 支付宝公钥
	 * @return
	 * @throws Exception
	 */
	public static String alipayCreateOrder(String orderNo,BigDecimal payAmount,String projectName,String alipay_appid,String alipay_private_key,String alipay_public_key) throws Exception {
        
		//实例化客户端
		AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do",
				alipay_appid.trim(),
				alipay_private_key.trim(), "json", 
				"utf-8", 
				alipay_public_key.trim(),
				"RSA2");
		//实例化具体API对应的request类,类名称和接口名称对应,当前调用接口名称：alipay.trade.app.pay
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		//SDK已经封装掉了公共参数，这里只需要传入业务参数。以下方法为sdk的model入参方式(model和biz_content同时存在的情况下取biz_content)。
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setBody(SystemConfig.getValue("projectName")+"-"+projectName);
		model.setSubject(projectName);
		model.setOutTradeNo(orderNo);
		model.setTimeoutExpress("30m");
		model.setTotalAmount(payAmount.toString());
		model.setProductCode("QUICK_MSECURITY_PAY");
		request.setBizModel(model);
		request.setNotifyUrl(SystemConfig.getValue("alipayNotifyUrl"));
		try {
		        //这里和普通的接口调用不同，使用的是sdkExecute
		        AlipayTradeAppPayResponse response = alipayClient.sdkExecute(request);
		        
		        return response.getBody();//就是orderString 可以直接给客户端请求，无需再做处理。
		    } catch (AlipayApiException e) {
		        e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 微信支付
	 * @param body  描述类容
	 * @param orderNo 订单号
	 * @param money 支付金额
	 * @return
	 */
	public static Map<String, Object> wxPay(String body,String orderNo,Integer money,String appId,String mchId,String key) {
		
		WXPayConfigImpl config;
		try {
			config = WXPayConfigImpl.getInstance(appId,mchId,key);
			WXPay wxpay= new WXPay(config);
			
			HashMap<String, String> data = new HashMap<String, String>();
			data.put("body", SystemConfig.getValue("projectName")+"-"+body);
			data.put("out_trade_no", orderNo);
			data.put("fee_type", "CNY");
			data.put("total_fee", money.toString());
			data.put("spbill_create_ip", SystemConfig.getValue("spbill_create_ip"));
			data.put("notify_url", SystemConfig.getValue("weixinNotifyUrl"));
			data.put("trade_type", "APP");
			
			Map<String, String> r = wxpay.unifiedOrder(data);
			
			System.out.println(r);
			
			if("SUCCESS".equals(r.get("result_code")) && "OK".equals(r.get("return_msg"))){
				Map<String, String> map = new HashMap<String, String>();
				map.put("appid", config.getAppID());
				map.put("partnerid", config.getMchID());
				map.put("prepayid", r.get("prepay_id"));
				map.put("package", "Sign=WXPay");
				map.put("noncestr", WXPayUtil.generateNonceStr());
				map.put("timestamp", (System.currentTimeMillis()/1000)+"");
				map.put("sign", WXPayUtil.generateSignature(map, config.getKey()));
				return new HashMap<String, Object>() {
					private static final long serialVersionUID = 1L;
					{
						putAll(map);
					}
				};
			}else{
				return new HashMap<String, Object>();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new HashMap<String, Object>();
	}
}
