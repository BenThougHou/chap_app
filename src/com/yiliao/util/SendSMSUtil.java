package com.yiliao.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.github.qcloudsms.SmsMultiSender;
import com.github.qcloudsms.SmsMultiSenderResult;
import com.github.qcloudsms.SmsMultiSenderResult.Detail;
import com.github.qcloudsms.httpclient.HTTPException;

public class SendSMSUtil {


	public static void main(String[] args) {

		System.out.println(SendSMSUtil.sendSms("13983287114", "1234"));

	}

	/**
	 * 腾讯云短信
	 * 
	 * @param phone
	 * @param code
	 * @return
	 */
	public static boolean sendQQSMS(String appId, String appKey, int templateId, String smsSign, String phone,
			String code) {
		// 需要发送短信的手机号码
		String[] phoneNumbers = { phone };
		// 指定模板ID单发短信
		try {
			String[] params = { code };
			SmsMultiSender msender = new SmsMultiSender(Integer.parseInt(appId), appKey);
			SmsMultiSenderResult result = msender.sendWithParam("86", phoneNumbers, templateId, params, smsSign, null,
					null); // 签名参数未提供或者为空时，会使用默认签名发送短信
			System.out.print(result);
			Detail detail = result.details.get(0);
			if (0 == detail.result) {
				System.out.println("发送成功!");
				return true;
			}
		} catch (HTTPException e) {
			// HTTP响应码错误
			e.printStackTrace();
		} catch (JSONException e) {
			// json解析错误
			e.printStackTrace();
		} catch (IOException e) {
			// 网络IO错误
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 阿里云 短信
	 * 
	 * @param phone
	 * @param code
	 * @return
	 */
	public static boolean sendAliPaySMS(String appId, String appKey, String templateId, String smsSign, String phone,
			String code) {

		try {

			// 设置超时时间-可自行调整
			System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
			System.setProperty("sun.net.client.defaultReadTimeout", "10000");
			// 初始化ascClient需要的几个参数
			final String product = "Dysmsapi";// 短信API产品名称（短信产品名固定，无需修改）
			final String domain = "dysmsapi.aliyuncs.com";// 短信API产品域名（接口地址固定，无需修改）
			// 初始化ascClient,暂时不支持多region（请勿修改）
			IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", appId, appKey);
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
			IAcsClient acsClient = new DefaultAcsClient(profile);
			// 组装请求对象
			SendSmsRequest request = new SendSmsRequest();
			// 使用post提交
			request.setMethod(MethodType.POST);
			// 必填:待发送手机号。支持以逗号分隔的形式进行批量调用，批量上限为1000个手机号码,批量调用相对于单条调用及时性稍有延迟,验证码类型的短信推荐使用单条调用的方式；发送国际/港澳台消息时，接收号码格式为00+国际区号+号码，如“0085200000000”
			request.setPhoneNumbers(phone);
			// 必填:短信签名-可在短信控制台中找到
			request.setSignName(smsSign);
			// 必填:短信模板-可在短信控制台中找到
			request.setTemplateCode(templateId);
			// 可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
			// 友情提示:如果JSON中需要带换行符,请参照标准的JSON协议对换行符的要求,比如短信内容中包含\r\n的情况在JSON中需要表示成\\r\\n,否则会导致JSON在服务端解析失败
			request.setTemplateParam("{\"code\":\"" + code + "\"}");
			// 可选-上行短信扩展码(扩展码字段控制在7位或以下，无特殊需求用户请忽略此字段)
			// request.setSmsUpExtendCode("90997");
			// 可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
			request.setOutId("yourOutId");
			// 请求失败这里会抛ClientException异常
			SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);

			System.out.println(sendSmsResponse.getCode());

			if (sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {
				// 请求成功
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * 网易云短信
	 * 
	 * @param phone
	 * @param code
	 * @return
	 */
	public static boolean sendNetEaseSMS(String appId, String appKey, int templateId, String smsSign, String phone,
			String code) {

		try {

			// 发送验证码的请求路径URL
			final String SERVER_URL = "https://api.netease.im/sms/sendcode.action";
			// 随机数
			final String NONCE = "123456";
			// 验证码长度，范围4～10，默认为4
			final String CODELEN = "6";

			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(SERVER_URL);
			String curTime = String.valueOf((new Date()).getTime() / 1000L);
			/*
			 * 参考计算CheckSum的java代码，在上述文档的参数列表中，有CheckSum的计算文档示例
			 */
			String checkSum = CheckSumBuilder.getCheckSum(appKey, NONCE, curTime);

			// 设置请求的header
			httpPost.addHeader("AppKey", appId);
			httpPost.addHeader("Nonce", NONCE);
			httpPost.addHeader("CurTime", curTime);
			httpPost.addHeader("CheckSum", checkSum);
			httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

			// 设置请求的的参数，requestBody参数
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			/*
			 * 1.如果是模板短信，请注意参数mobile是有s的，详细参数配置请参考“发送模板短信文档” 2.参数格式是jsonArray的格式，例如
			 * "['13888888888','13666666666']" 3.params是根据你模板里面有几个参数，那里面的参数也是jsonArray格式
			 */
			nvps.add(new BasicNameValuePair("templateid", templateId + ""));
			nvps.add(new BasicNameValuePair("mobile", phone));
			nvps.add(new BasicNameValuePair("codeLen", CODELEN));
			nvps.add(new BasicNameValuePair("params", code));

			httpPost.setEntity(new UrlEncodedFormEntity(nvps, "utf-8"));

			// 执行请求
			HttpResponse response = httpClient.execute(httpPost);
			/*
			 * 1.打印执行结果，打印结果一般会200、315、403、404、413、414、500 2.具体的code有问题的可以参考官网的Code状态表
			 */
			String retCode = EntityUtils.toString(response.getEntity(), "utf-8");

			if ("200".equals(retCode)) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static final String charset = "utf-8";

	// 请登录zz.253.com 获取创蓝API账号(非登录账号,示例:N1234567)
//	public static String username = "b06s03";

	// 请登录zz.253.com 获取创蓝API密码(非登录密码)
//	public static String password = "12345678";

	/**
	 * 发送短信
	 * 
	 * @return
	 */
	public static boolean sendSms(String phone, String smsCode) {

		try {
			System.out.println("短信请求手机号  " + phone);
			System.out.println("短信请求验证码  " + smsCode);
			System.out.println("短信请求账号密码  " + SystemConfig.getValue("chat_sms_username") + "  " + SystemConfig.getValue("chat_sms_password"));
			String host = "https://u.smsyun.cc/sms-partner/access/" + SystemConfig.getValue("chat_sms_username") + "/sendsms"; // 提交地址ַ
			String md_password = Md5Util.encodeByMD5ByLow(SystemConfig.getValue("chat_sms_password")); // 密码进行MD5加密 32位小写
			String content = "【"+SystemConfig.getValue("sms_name")+"】您的登陆验证码为" + smsCode + "，请在5分钟内登陆。如果不是您本人操作，请忽略本信息。";// 发送内容

			String data = "{\"smstype\":\"4\",\"clientid\":\"" + SystemConfig.getValue("chat_sms_username") + "\",\"password\":\"" + md_password
					+ "\",\"mobile\":\"" + phone + "\",\"content\":\"" + content
					+ "\",\"sendtime\":\"2019-03-19 17:29:19\",\"extend\":\"00\",\"uid\":\"00\"}";

			// 发送 POST 请求
			String sr = HttpUtil.httpClentString(host, data);
			System.out.println("短信返回 " + sr);
			Map object = JSON.toObject(sr, Map.class);
			List<Map> result = (List) object.get("data");
			if (!result.isEmpty()) {
				if (result.get(0).get("code").toString().equals("0")) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
