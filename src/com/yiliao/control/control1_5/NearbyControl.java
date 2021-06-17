package com.yiliao.control.control1_5;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.NearbyService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * 附近相关控制层
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class NearbyControl {
	
	@Autowired
	private NearbyService nearbyService;

	
	/**
	 * 用户上传坐标法
	 * @param userId 用户编号
	 * @param lat 纬度
	 * @param lng 经度
	 * @param response
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = {"uploadCoordinate"},method = {RequestMethod.POST})
	@ResponseBody
	public MessageUtil uploadCoordinate(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException{
		 
		//进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		//验必传的参数
		if(!BaseUtil.params(param.getOrDefault("userId", 0),param.getOrDefault("lat",0.0),param.getOrDefault("lng",0.0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.nearbyService.uploadCoordinate(param.getInt("userId"),param.getDouble("lat"),param.getDouble("lng"));
		
	}

	@RequestMapping(value = {"uploadLocation"},method = {RequestMethod.POST})
	@ResponseBody
	public MessageUtil uploadLocation(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException{

		//进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		//验必传的参数
		if(!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.nearbyService.uploadLocation(param.getInt("userId"),param.getString("city"));

	}
	
	
	
	/**
	 * 获取附近的主播
	 * @param lat 纬度
	 * @param lng 经度
	 * @param response
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = {"getNearbyList"},method = {RequestMethod.POST})
	@ResponseBody
	public MessageUtil getNearbyList(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException{
		 
		//进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		//验必传的参数
		if(!BaseUtil.params(param.getOrDefault("userId",0),param.getOrDefault("lat",0.0),param.getOrDefault("lng",0.0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return  this.nearbyService.getNearbyList(param.getDouble("lat"), param.getDouble("lng"),param.getInt("userId"));
	}
	
	/**
	 *  加载附近用户列表
	 * @param userId
	 * @param page
	 * @param response
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = {"getAnthorDistanceList"},method = {RequestMethod.POST})
	@ResponseBody
	public MessageUtil getAnthorDistanceList(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {
		 
		//进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		//验必传的参数
		if(!BaseUtil.params(param.getOrDefault("userId",0),param.getOrDefault("lat",0.0),param.getOrDefault("lng",0.0),param.getOrDefault("page",0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.nearbyService.getAnthorDistanceList(param.getInt("userId"), param.getInt("page"),param.getDouble("lat"),param.getDouble("lng"));
		
	}
	
	
	/**
	 * 获取用户信息
	 * @param userId
	 * @param coverSeeUserId
	 * @param lat
	 * @param lng
	 * @param response
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("getUserDeta")
	@ResponseBody
	public MessageUtil getUserDeta(HttpServletRequest req) throws NoSuchAlgorithmException, InvalidKeySpecException {
		 
		//进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		//验必传的参数
		if(!BaseUtil.params(param.getOrDefault("userId",0),param.getOrDefault("lat",0.0),param.getOrDefault("lng",0.0),param.getOrDefault("coverSeeUserId",0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.nearbyService.getUserDeta(param.getInt("userId"), param.getInt("coverSeeUserId"), param.getDouble("lat"), param.getDouble("lng"));
		
	}
	
}
