package com.yiliao.control.control1_7;

import java.io.BufferedReader;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.service1_7.SpeedDatingService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * 速配相关
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class SpeedDatingControl {

	@Autowired
	private SpeedDatingService speedDatingService;

	/**
	 * 开启速配
	 * 
	 * @param userId
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = { "openSpeedDating" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil openSpeedDating(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		if (!BaseUtil.params(param.getOrDefault("userId",0), param.getOrDefault("roomId",0))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		
		return speedDatingService.openSpeedDating(param.getInt("userId"), param.getInt("roomId"));
	}

	/**
	 * 断流回调结束速配
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = { "endSpeedDating" }, method = { RequestMethod.POST })
	@ResponseBody
	public void endSpeedDating(HttpServletRequest request,String title) throws Exception {
		
		if(StringUtils.isBlank(title)) {
			BufferedReader br;
			br = request.getReader();
			String str, wholeStr = "";
			while ((str = br.readLine()) != null) {
				wholeStr += str;
			}
			if(StringUtils.isBlank(wholeStr)) {
				return ;
			}
			String res = URLDecoder.decode(wholeStr, "UTF-8");
			title = res.split("=")[1];
		}
		
		int userId = Integer.parseInt(title.split("/")[0]);
		
		speedDatingService.endSpeedDating(userId,0);
	}
	/**
	 * app调用结束速配
	 * @param request
	 * @param response
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@RequestMapping(value = { "appEndSpeedDating" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil appEndSpeedDating(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		if (!BaseUtil.params(param.getInt("userId"))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return speedDatingService.endSpeedDating(param.getInt("userId"),1);
	}
	

	/**
	 * 个人获取主播输配播放列表
	 * 
	 * @param userId
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@RequestMapping(value = { "getSpeedDatingAnchor" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getSpeedDatingAnchor(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		if (!BaseUtil.params(param.getInt("userId"))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}

		return speedDatingService.getSpeedDatingAnchor(param.getInt("userId"));
	}

	/**
	 * 获取主播计时统计
	 * 
	 * @param userId
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@RequestMapping(value = { "getUserSpeedTime" })
	@ResponseBody
	public MessageUtil getUserSpeedTime(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		if (!BaseUtil.params(param.getInt("userId"))) {
			return new MessageUtil(Integer.valueOf(-500), "服务器拒绝执行请求!");
		}
		return speedDatingService.getUserSpeedTime(param.getInt("userId"));
	}
	
	
	/**
	 * 获取
	 * 
	 * @return
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@RequestMapping(value = { "getSpeedDatingList" })
	@ResponseBody
	public MessageUtil getSpeedDatingList(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {
		
		return speedDatingService.getSpeedDatingList();
	}
}
