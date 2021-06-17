package com.yiliao.control;

import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.yiliao.service.HomePageService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;

import net.sf.json.JSONObject;

/**
 * 主页控制层
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class HomePageControl {

	@Autowired
	private HomePageService homePageService;

	
	/**主播手动置顶功能
	 * @param req
	 * @return
	 * @throws ExecutionException
	 */
	@RequestMapping(value = "setOperatingTopping", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil setOperatingTopping(HttpServletRequest req) throws ExecutionException {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.homePageService.operatingTopping(param.getInt("userId"));

	}

	/**
	 * 男粉丝置顶功能
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "setUserTopping", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil setUserTopping(HttpServletRequest req){
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.homePageService.userTopping(param.getInt("userId"));
	}
	
	/**
	 * app主页
	 * 
	 * @param userId    请求人编号
	 * @param page      页码
	 * @param queryType 类型 0.新人 1.推荐 2.活跃 3.女神
	 * @param condition 查询条件(昵称 ID号)
	 * @param response
	 * @throws ExecutionException
	 */
	@RequestMapping(value = "getHomePageList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getHomePageList(HttpServletRequest req) throws ExecutionException {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page")) || param.getInt("queryType") < -1
				|| param.getInt("queryType") > 4) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getHomePageList(param.getInt("userId"), param.getInt("page"),
				param.getInt("queryType"));

	}
	/**
	 * app主页---选聊
	 * 
	 * @param userId    请求人编号
	 * @param anotherId      删除的用户Id
	 * @throws ExecutionException
	 */
	@RequestMapping(value = "getSelectCharAnother", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getSelectCharAnother(HttpServletRequest req) throws ExecutionException {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"),param.getInt("anotherId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.homePageService.getSelectCharAnother(param.getInt("userId"),param.getInt("anotherId"));

	}
	
	/**
	 * 获取推荐列表
	 * 
	 * @param userId    请求人编号
	 * @param page      页码
	 * @param queryType 类型 -1:全部 0.女 1.男
	 * @param condition 查询条件(昵称 ID号)
	 * @param response
	 */
	@RequestMapping(value = { "getHomeNominateList" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil getHomeNominateList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.homePageService.getHomeNominateList(param.getInt("userId"), param.getInt("page"));
	}

	/**
	 * 获取主播播放页
	 * 
	 * @param consumeUserId      查看人
	 * @param coverConsumeUserId 被查看人
	 * @param albumId            数据编号
	 * @param queryType          查询类型
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnchorPlayPage", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnchorPlayPage(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getAnchorPlayPage(param.getInt("userId"), param.getInt("coverConsumeUserId"),
				StringUtils.isNotBlank(param.getOrDefault("albumId", "").toString()) ? param.getInt("albumId") : 0,
				param.getInt("queryType"));

	}

	/**
	 * 获取banner列表
	 * 
	 * @param response
	 * @throws ExecutionException
	 */
	@RequestMapping(value = "getBannerList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getBannerList(HttpServletRequest req) throws ExecutionException {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return  homePageService.getBannerList();

	}

	/**
	 * ios 获取 banner 相关数据
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getIosBannerList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getIosBannerList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.homePageService.getIosBannerList();

	}

	/**
	 * 获取短视频列表
	 * 
	 * @param page
	 * @param response
	 */
	@RequestMapping(value = "getVideoList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getVideoList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.homePageService.getVideoList(param.getInt("page"), param.getInt("userId"),
				  null == param.get("queryType") ? -1 : param.getInt("queryType"));
	}

	/**
	 * 获取同城列表
	 * 
	 * @param page
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getCityWideList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getCityWideList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getCityWideList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 获取搜索列表
	 * 
	 * @param page
	 * @param condition
	 * @param response
	 */
	@RequestMapping(value = "getSearchList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getSearchList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"), param.getString("condition"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getSearchList(param.getInt("userId"), param.getInt("page"),
				param.getString("condition"));

	}

	/**
	 * 主播加载粉丝[男粉]
	 * 
	 * @param userId     主播编号
	 * @param page       页码
	 * @param search     搜索条件 昵称or id号
	 * @param searchType 搜索类型 -1:全部 0:女 1:男
	 * @param response
	 * @throws ExecutionException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getOnLineUserList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getOnLineUserList(HttpServletRequest req) throws ExecutionException {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page")) || param.getInt("searchType") < -1
				|| param.getInt("searchType") > 1) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getOnLineUserList(param.getInt("userId"), param.getInt("page"),
				param.getOrDefault("search", "").toString(), param.getInt("searchType"));
	}

	/**
	 * 跳转到文明公约
	 * 
	 * @return
	 */
	@RequestMapping(value = "jumpCivilization", method = RequestMethod.POST)
	public ModelAndView jumpCivilization() {
		return new ModelAndView("civilization");
	}

	/**
	 * 跳转到分享有礼
	 * 
	 * @return
	 */
	@RequestMapping(value = "jumpShareCourtesy", method = RequestMethod.POST)
	public ModelAndView jumpShareCourtesy() {
		return new ModelAndView("shareCourtesy");
	}

	/**
	 * 魅力排行榜
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getGlamourList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getGlamourList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("queryType"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getGlamourList(param.getInt("userId"), param.getInt("queryType"));
	}

	/**
	 * 消费排行榜
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getConsumeList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getConsumeList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("queryType"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getConsumeList(param.getInt("userId"), param.getInt("queryType"));
	}

	/**
	 * 豪礼排行榜
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getCourtesyList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getCourtesyList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("queryType"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.homePageService.getCourtesyList(param.getInt("userId"), param.getInt("queryType"));
	}

	/**
	 * 获取主播收益明细
	 * 
	 * @param userId
	 * @param response
	 */
	@RequestMapping(value = "getAnchorProfitDetail", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnchorProfitDetail(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("queryType"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getAnchorProfitDetail(param.getInt("userId"), param.getInt("queryType"));

	}

	/**
	 * 获取风格设置
	 * 
	 * @param response
	 */
	/**
	 * @return
	 */
	@RequestMapping(value = "getStyleSetUp", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getStyleSetUp(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.homePageService.getStyleSetUp();

	}

	/**
	 * get free anchor list
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@RequestMapping(value = "getTryCompereList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getTryCompereList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getTryCompereList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * get new anchor list
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@RequestMapping(value = "getNewCompereList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getNewCompereList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (!BaseUtil.params(param.getInt("userId"), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.homePageService.getNewCompereList(param.getInt("userId"), param.getInt("page"));

	}

}
