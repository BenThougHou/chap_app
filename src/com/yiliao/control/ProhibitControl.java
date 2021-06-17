package com.yiliao.control;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.ProhibitService;
import com.yiliao.util.PrintUtil;
import com.yiliao.util.SystemConfig;

import net.sf.json.JSONObject;

/**
 *  腾讯回调
 * 
 * @author Administrator
 * 
 */
@Controller
@RequestMapping("app")
public class ProhibitControl {

	Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private ProhibitService prohibitService;
	/**
	 * 
	 * 直播时鉴黄回调
	 * @param request
	 * @param response
	 */
	@RequestMapping("tencentCallback")
	@ResponseBody
	public void tencentCallback(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			
			BufferedReader br = request.getReader();
			String str, wholeStr = "";
			while ((str = br.readLine()) != null) {
				wholeStr += str;
			}
		
			logger.info("回调结果-->{}",wholeStr);
 
			JSONObject json = JSONObject.fromObject(wholeStr);
			//处理违规用户
			this.prohibitService.handleGetOutOfLine(json);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * im回调
	 * @param request
	 * @param response
	 */
	@RequestMapping("imCallBack")
	@ResponseBody
	public void imCallBack(HttpServletRequest request ,HttpServletResponse response) {
		try {
			String SdkAppid = request.getParameter("SdkAppid");
			
			logger.info("{}回调的appId",SdkAppid);
			
			if(SystemConfig.getValue("im_appid").equals(SdkAppid)) {
				
				BufferedReader br = request.getReader();
				String str, wholeStr = "";
				while ((str = br.readLine()) != null) {
					wholeStr += str;
				}

				PrintUtil.printWri("{\"ActionStatus\": \"OK\",\"ErrorInfo\": \"\",\"ErrorCode\": 0}", response);

				JSONObject fromObject = JSONObject.fromObject(wholeStr);
				JSONObject content = fromObject.getJSONArray("MsgBody").getJSONObject(0);
				String mesType = content.get("MsgType").toString();
				String text = "";
				if(mesType.equals("TIMTextElem")){
					text = content.getJSONObject("MsgContent").get("Text").toString();
				}
				if(mesType.equals("TIMImageElem")){
					JSONObject object = (JSONObject) content.getJSONObject("MsgContent").getJSONArray("ImageInfoArray").get(0);
					text = object.get("URL").toString();
				}
				if(mesType.equals("TIMSoundElem")){
					text = content.getJSONObject("MsgContent").get("Url").toString();
				}
				//保存数据
				this.prohibitService.handleImCallBack(fromObject.getInt("From_Account"),
						fromObject.getInt("To_Account"),
						text,mesType);
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
}
