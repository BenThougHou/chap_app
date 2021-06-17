package com.yiliao.util;

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class HttpUtil {

	private static CloseableHttpClient httpclient;
	static{
		try {
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null,
					new TrustStrategy() {
						@Override
						public boolean isTrusted(X509Certificate[] chain,
												 String authType) throws CertificateException {
							return true;
						}
					}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		} catch (Exception e) {
		}
	}
	/**
	 * @方法名 httpClent
	 * @说明 (传递参数的方法)
	 * @param 参数
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	@SuppressWarnings("static-access")
	public static JSONObject httpHengYunClentJson(String httpUrl, String content) {
		try {
			// 创建连接
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.setRequestProperty("contentType", "utf-8");
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.connect();
			PrintWriter out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "utf-8"));
			out.println(content);
			out.close();

			// 读取响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String lines;
			StringBuffer sb = new StringBuffer("");
			while ((lines = reader.readLine()) != null) {
				lines = new String(lines.getBytes(), "utf-8");
				sb.append(lines);
			}
			reader.close();
			// 断开连接
			connection.disconnect();

			return JSONObject.fromObject(sb.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}


	public static String dopost(String url, Map <String,String> map)
	{
		String responseContent = "";
		HttpPost httppost = new HttpPost(url);
		List < NameValuePair > formparams = new ArrayList <NameValuePair>();
		for (Map.Entry<String, String> m : map.entrySet())
		{
			formparams.add(new BasicNameValuePair(m.getKey(), m.getValue()));
		}
		UrlEncodedFormEntity uefEntity;
		try
		{
			uefEntity = new UrlEncodedFormEntity(formparams, "UTF-8");
			httppost.setEntity(uefEntity);
			//CloseableHttpResponse response = httpclient.execute(httppost);
			CloseableHttpResponse response = createSSLClientDefault().execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null)
			{
				responseContent = EntityUtils.toString(entity, "UTF-8");

			}
			response.close();
		}
		catch (ClientProtocolException e)
		{
			responseContent = e.getMessage();
			e.printStackTrace();
		}
		catch (UnsupportedEncodingException e)
		{
			responseContent = e.getMessage();
			e.printStackTrace();
		}
		catch (IOException e)
		{
			responseContent = e.getMessage();
			e.printStackTrace();
		}
		finally
		{
			try
			{
				httpclient.close();
			}
			catch (IOException e)
			{
				responseContent = e.getMessage();
			}
		}
		return responseContent;
	}
	public static CloseableHttpClient createSSLClientDefault()
	{
		try
		{
			SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
				//信任所有
				@Override
				public boolean isTrusted(X509Certificate[] chain,
										 String authType) throws CertificateException {
					return true;
				}
			}).build();
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);
			return HttpClients.custom().setSSLSocketFactory(sslsf).build();
		}
		catch (KeyManagementException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		catch (KeyStoreException e)
		{
			e.printStackTrace();
		}
		return  HttpClients.createDefault();
	}
	/**
	 * @方法名 httpConnection
	 * @说明 (使用url获取网络数据)
	 * @param 参数
	 * @param URL
	 * @param 参数
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	public static JSONObject httpConnection(String URL) {
		URL url = null;
		HttpURLConnection connection = null;
		// 生成验证码
		try {
			url = new URL(URL);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			// connection.setRequestProperty("Content-type", "text/html");
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.setUseCaches(false);
			connection.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			reader.close();
			return new JSONObject().fromObject(buffer.toString());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public void text(String content) throws IOException {

		FileWriter fileWriter = new FileWriter("D:\\111\\123456.txt");
		fileWriter.write(content);
		fileWriter.flush();
		fileWriter.close();
	}

	/**
	 * @方法名 getSend
	 * @说明 (短信通道post方法)
	 * @param 参数
	 * @param strUrl
	 * @param 参数
	 * @param param
	 * @param 参数
	 * @return 设定文件
	 * @return String 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	public String postSend(String strUrl, String param) {

		URL url = null;
		HttpURLConnection connection = null;

		try {
			url = new URL(strUrl);
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.connect();

			// POST����ʱʹ��
			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(param);
			out.flush();
			out.close();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			StringBuffer buffer = new StringBuffer();
			String line = "";
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			reader.close();
			return buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	/**
	 * @方法名 httpClent
	 * @说明 (传递参数的方法)
	 * @param 参数
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	@SuppressWarnings("static-access")
	public static JSONObject httpClentJson(String httpUrl, String content) {
		try {
			// 创建连接
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.setRequestProperty("contentType", "utf-8");
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.connect();
			PrintWriter out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "utf-8"));
			out.println(content);
			out.close();

			// 读取响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String lines;
			StringBuffer sb = new StringBuffer("");
			while ((lines = reader.readLine()) != null) {
				lines = new String(lines.getBytes(), "utf-8");
				sb.append(lines);
			}
			reader.close();
			// 断开连接
			connection.disconnect();

			return JSONObject.fromObject(sb.toString());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) {

		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append("appid=").append("3138072828");
		sbBuffer.append("&data=").append("222222");
		sbBuffer.append("&money=").append("0.01");
		sbBuffer.append("&type=").append(1);
		sbBuffer.append("&uip=").append("125.80.130.101");
		sbBuffer.append("&appkey=").append("1cad61550fa7642d0b69122c4be05b6e");

		String token = Md5Util.encodeByMD5ByLow(sbBuffer.toString());
		
		sbBuffer.append("&token=").append(token);

		System.out.println(sbBuffer.toString());
		
		
		JSONObject httpClentJson = httpClentJson("http://yunpay.waa.cn/", sbBuffer.toString());

		System.out.println(httpClentJson);

	}

	/**
	 * @方法名 httpClent
	 * @说明 (传递参数的方法)
	 * @param 参数
	 * @return 设定文件
	 * @return JSONObject 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	@SuppressWarnings("static-access")
	public static String httpClentString(String httpUrl, String content) {
		try {
			// 创建连接
			URL url = new URL(httpUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setRequestMethod("POST");
			connection.setUseCaches(false);
			connection.setRequestProperty("Accept-Charset", "utf-8");
			connection.setRequestProperty("contentType", "utf-8");
			connection.setInstanceFollowRedirects(true);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.connect();
			PrintWriter out = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "utf-8"));
			out.println(content);
			out.close();

			// 读取响应
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String lines;
			StringBuffer sb = new StringBuffer("");
			while ((lines = reader.readLine()) != null) {
				lines = new String(lines.getBytes(), "utf-8");
				sb.append(lines);
			}
			reader.close();
			// 断开连接
			connection.disconnect();

			return sb.toString();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 转为16进制方法
	 * 
	 * @param str
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public String paraTo16(String str) throws UnsupportedEncodingException {
		String hs = "";

		byte[] byStr = str.getBytes("UTF-8");
		for (int i = 0; i < byStr.length; i++) {
			String temp = "";
			temp = (Integer.toHexString(byStr[i] & 0xFF));
			if (temp.length() == 1)
				temp = "%0" + temp;
			else
				temp = "%" + temp;
			hs = hs + temp;
		}
		return hs.toUpperCase();

	}

}
