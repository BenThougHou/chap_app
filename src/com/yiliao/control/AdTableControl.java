package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.AdTableService;
import com.yiliao.util.MessageUtil;


/**
 * 广告
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class AdTableControl {

	@Autowired
	private AdTableService adTableService;

	/**
	 * 获取广告列表
	 * 
	 * @param userId
	 * @param page
	 */
	@RequestMapping(value = { "getAdTable" })
	@ResponseBody
	public MessageUtil getAdTable(int type,int page,int size,int userId,HttpServletRequest req) {

		return this.adTableService.getAdTable(type,page,size,userId);
	}

}
