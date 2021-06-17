package com.yiliao.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.yiliao.util.SystemConfig;

public class HttpClientUtil {
	
	private static String smsPassWord = SystemConfig.getValue("smsPassWord");//非联通
	
	private static String smsPassWordByUnicom = SystemConfig.getValue("smsPassWordByUnicom");//联通
	
	/**
	 * post方式
	 */
	public static String sendSms(String moblePhone,String msgContent){
		
		String responseMsg = "";
		
		//构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		
		httpClient.getParams().setContentCharset("GB2312");
		
		String url = "http://cq.ums86.com:8899/sms/Api/Send.do";
		
		//构造PostMethod的实例
		PostMethod postMethod = new PostMethod(url);
		
		postMethod.addParameter("SpCode","215736");
		postMethod.addParameter("LoginName","admin");
		postMethod.addParameter("Password",smsPassWord);
		postMethod.addParameter("MessageContent",msgContent);
		postMethod.addParameter("UserNumber",moblePhone);
		postMethod.addParameter("SerialNumber","");
		postMethod.addParameter("ScheduleTime","");
		postMethod.addParameter("ExtendAccessNum","");
		postMethod.addParameter("f","1");
		
		//执行postMethod,调用http接口
		try {
			
			httpClient.executeMethod(postMethod);
			
			responseMsg = postMethod.getResponseBodyAsString().trim();
			
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return responseMsg;
	}
	
	public static String sendSmsByUnicom(String moblePhone,String msgContent){
		
		String responseMsg = "";
		
		//构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		
		httpClient.getParams().setContentCharset("GB2312");
		
		String url = "http://cq.ums86.com:8899/sms/Api/Send.do";
		
		//构造PostMethod的实例
		PostMethod postMethod = new PostMethod(url);
		
		postMethod.addParameter("SpCode","219286");
		postMethod.addParameter("LoginName","cq_xxty");
		postMethod.addParameter("Password",smsPassWordByUnicom);
		postMethod.addParameter("MessageContent",msgContent);
		postMethod.addParameter("UserNumber",moblePhone);
		postMethod.addParameter("SerialNumber","");
		postMethod.addParameter("ScheduleTime","");
		postMethod.addParameter("ExtendAccessNum","");
		postMethod.addParameter("f","1");
		
		//执行postMethod,调用http接口
		try {
			
			httpClient.executeMethod(postMethod);
			
			responseMsg = postMethod.getResponseBodyAsString().trim();
			
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return responseMsg;
	}
	/**
     * HTTP POST请求
     * 
     * @param url
     * @param params
     * @param encoding
     * @return
     */
    public static String post(String url, Map<String, String> paramsMap, String encoding) {
    	
    	List<BasicNameValuePair> basicNameValuePairs = new ArrayList<BasicNameValuePair>();
		Iterator<Entry<String, String>> iterator = paramsMap.entrySet().iterator();
		while(iterator.hasNext()) {
			Entry<String, String> next = iterator.next();
			basicNameValuePairs.add(new BasicNameValuePair(next.getKey(), next.getValue()));
		}
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.addAll(basicNameValuePairs);
        
        if (null == encoding || encoding.trim().equals("")) {
            encoding = HTTP.DEFAULT_CONTENT_CHARSET;
        }
        
        String body = null;
        HttpEntity entity = null;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            /*
             * Post请求
             */
            HttpPost httppost = new HttpPost(url);
            /*
             * 设置参数
             */
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(params, encoding);
            httppost.setEntity(formEntity);
            /*
             * 发送请求
             */
            HttpResponse httpresponse = httpClient.execute(httppost);
            /*
             * 获取返回数据
             */
            entity = httpresponse.getEntity();
            body = EntityUtils.toString(entity,encoding);
        }catch (Exception e) {
        	
        } 
        return body;
    }
}
