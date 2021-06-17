package com.yiliao.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.yiliao.service.ShareService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.PrintUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("share")
public class ShareControl {

	@Autowired
	private ShareService shareService;

	/**
	 * 获取推广链接
	 * 
	 * @return
	 */
	@RequestMapping(value = { "getSpreadUrl" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getSpreadUrl(HttpServletRequest req) {
		// 进行解密
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return shareService.getSpreadUrl(param.getInt("userId"));
	}

	/**
	 * 跳转到分享页面
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "jumpShare")
	public ModelAndView jumpShare(HttpServletRequest req,HttpServletResponse response) {
		// 解密参数
		// 验证传递的参数
		if (BaseUtil.params(req.getParameter("userId"))) {
			Map<String, Object> map = this.shareService.getDownLoadUrl();
			Map<String, Object> pacMap =this.shareService.getPacDownLoadUrl(req.getParameter("userId"));
			if(pacMap!=null) {
				map.putAll(pacMap);
			}
		    
			ModelAndView mv = new ModelAndView();
			mv.addObject("userId", req.getParameter("userId"));
			mv.addAllObjects(map);
			mv.setViewName("share");
			return mv;
		}
		return null;
	}

	
	/**
	 * 跳转到分享页面
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "jumpDounUrl")
	public ModelAndView jumpAShare(HttpServletRequest req) {
		// 解密参数
		// 验证传递的参数
		if (BaseUtil.params(req.getParameter("userId"))) {
//			Map<String, Object> map = this.shareService.getDownLoadUrl();
//			Map<String, Object> pacMap =this.shareService.getPacDownLoadUrl(req.getParameter("userId"));
//			if(pacMap!=null) {
//				map.putAll(pacMap);
//			}
			//1:A域名 2:非A域名 指定跳转---》url
			Map<String, Object> map = this.shareService.getDownLoadUrlList(1);
			ModelAndView mv = new ModelAndView();
			mv.addObject("userId", req.getParameter("userId"));
			if(map!=null) {
				mv.addObject("jumpToB", map.get("t_domain_name")+"?userId="+req.getParameter("userId"));
			}
			mv.setViewName("Ashare");
			return mv;
		}
		return null;
	}
	/**
	 * 保存设备信息和分享人
	 * 
	 * @param userId
	 * @param equipment
	 * @param system_moble
	 * @param ipAddress
	 * @param response
	 */
	@RequestMapping("addShareInfo")
	@ResponseBody
	public void addShareInfo(int userId, String equipment, String system_moble, String ip, HttpServletRequest request,
			HttpServletResponse response) {

		this.shareService.addShareInfo(userId, equipment, system_moble, ip);

		PrintUtil.printWri(new MessageUtil(1, "操作成功!"), response);
	}

	/**
	 * 获取用户真实IP地址，不使用request.getRemoteAddr();的原因是有可能用户使用了代理软件方式避免真实IP地址,
	 * 
	 * 可是，如果通过了多级反向代理的话，X-Forwarded-For的值并不止一个，而是一串IP值，究竟哪个才是真正的用户端的真实IP呢？
	 * 答案是取X-Forwarded-For中第一个非unknown的有效IP字符串。
	 * 
	 * 如：X-Forwarded-For：192.168.1.110, 192.168.1.120, 192.168.1.130, 192.168.1.100
	 * 
	 * 用户真实IP为： 192.168.1.110
	 * 
	 * @param request
	 * @return
	 */
	public static String getIpAddress(HttpServletRequest request) {

		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_CLIENT_IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}

	/**
	 * 保存分享次数
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "addShareCount")
	@ResponseBody
	public MessageUtil addShareCount(HttpServletRequest req) {

		if (!BaseUtil.params(Integer.parseInt(req.getParameter("userId")))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.shareService.addShareCount(Integer.parseInt(req.getParameter("userId")));

	}

	/**
	 * 跳转到主播招募令界面
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("jumpAnchors")
	public ModelAndView jumpAnchors(HttpServletRequest req) {
		// 解密参数
		ModelAndView mv = new ModelAndView();
		mv.addObject("userId", req.getParameter("userId"));
		mv.setViewName("anchors");
		return mv;
	}

	/**
	 * 跳转到小夫妻
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("jumpSpouse")
	public ModelAndView jumpShare1(HttpServletRequest req) {
		// 解密参数
		ModelAndView mv = new ModelAndView();
		mv.addObject("userId", req.getParameter("userId"));
		mv.setViewName("spouse");
		return mv;
	}

	/**
	 * 跳转到大学生
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("jumpStudent")
	public ModelAndView jumpStudent(HttpServletRequest req, int userId) {
		// 解密参数
		ModelAndView mv = new ModelAndView();
		mv.addObject("userId", req.getParameter("userId"));
		mv.setViewName("student");
		return mv;
	}

	/**
	 * 马云在分享
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("jumpJackMa")
	public ModelAndView jumpJackMa(HttpServletRequest req, int userId) {
		// 解密参数
		ModelAndView mv = new ModelAndView();
		mv.addObject("userId", req.getParameter("userId"));
		mv.setViewName("jackMa");
		return mv;
	}

	/**
	 * 跳转到游戏分享
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping("jumpGame")
	public ModelAndView jumpGame(HttpServletRequest req, int userId) {
		// 解密参数
		ModelAndView mv = new ModelAndView();
		mv.addObject("userId", req.getParameter("userId"));
		mv.setViewName("game");
		return mv;
	}

	@RequestMapping("getJsonpRequest")
	@ResponseBody
	public void getJsonpRequest(HttpServletRequest request, HttpServletResponse response) {
		String userId = request.getParameter("userId");
		String equipment = request.getParameter("equipment");
		String system_moble = request.getParameter("system_moble");

		this.shareService.addShareInfo(Integer.parseInt(userId), equipment, system_moble, getIpAddress(request));
	}

	/**
	 * Android 获取下载地址
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = { "getDoloadUrl" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getDoloadUrl(HttpServletRequest req) {

		Map<String, Object> downLoadUrl = this.shareService.getDownLoadUrl();

		return new MessageUtil(1, downLoadUrl);
	}

	/**
	 * IM回调消息保存
	 * 
	 * @param params
	 * @return
	 */
	@RequestMapping("getIMInfo")
	@ResponseBody
	public void getIMInfo(HttpServletRequest request) {
		try {
			BufferedReader br = request.getReader();
			String str, wholeStr = "";
			while ((str = br.readLine()) != null) {
				wholeStr += str;
			}
			JSONObject json = JSONObject.fromObject(wholeStr);
			shareService.getIMInfo((Map<String, Object>) json);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
