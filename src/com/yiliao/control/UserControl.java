 package com.yiliao.control;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.yiliao.domain.entity.Cretification;
import com.yiliao.service.PersonalCenterService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 用户个人中心
 * 
 * @author Administrator
 * 
 */
@Controller
@RequestMapping("app")
public class UserControl {

	@Autowired
	private PersonalCenterService personalCenterService;

	/**
	 * 获取IM过滤词
	 * @param req
	 * @return
	 */
	@RequestMapping(value= {"getImFilter"},method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getImFilter(HttpServletRequest req) {
		
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return personalCenterService.getImFilter();
	}
	
	/**
	 * 获取用户IM签名
	 * @param req
	 * @return
	 */
	@RequestMapping(value= {"getUserImSig"},method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserImSig(HttpServletRequest req) {
		
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return personalCenterService.getUserImSig(param.getInt("userId"));
	}
	
	/**
	 * 加载个人资料[查询自己的详情]
	 * 
	 * @param req
	 * @return
	 * @throws ExecutionException
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "index", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil userPersonal(HttpServletRequest req) throws ExecutionException {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return personalCenterService.getPersonalCenter(param.getInt("userId"));

	}

	/**
	 * 获取个人资料
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getPersonalData", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getPersonalData(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getPersonalData(param.getInt("userId"));

	}

	/**
	 * 修改个人资料
	 * 
	 * @param userId          用户编号
	 * @param t_nickName      用户昵称
	 * @param t_phone         用户电话
	 * @param t_height        身高
	 * @param t_weight        体重
	 * @param t_constellation 星座
	 * @param t_city          城市
	 * @param t_synopsis      简介
	 * @param t_autograph     签名
	 * @param labels          标签
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "updatePersonalData", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil updatePersonalData(HttpServletRequest req) {

		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		MessageUtil mu = null;
		// 调用标签存储方法
		if (null != param.get("lables") && !param.getString("lables").isEmpty()) {
			mu = this.personalCenterService.saveUserLabel(param.getInt("userId"), param.getString("lables"));
			if (mu.getM_istatus() == 0) {
				return mu;
			}
		}

		// 调用存储封面图片的方法
		if (null != param.get("coverImg") && !param.getString("coverImg").isEmpty()) {
			mu = this.personalCenterService.usedCoverImg(param.getInt("userId"), param.getString("coverImg"),
					param.getString("t_first"));
			if (mu.getM_istatus() == 0) {
				return mu;
			}
		}

		return this.personalCenterService.updatePersonalData(param.getInt("userId"),
				(String) param.getOrDefault("t_nickName", null), (String) param.getOrDefault("t_phone", null),
				Integer.parseInt(param.getOrDefault("t_height", 0).toString()),
				Double.parseDouble(param.getOrDefault("t_weight", 0).toString()),
				(String) param.getOrDefault("t_constellation", null), (String) param.getOrDefault("t_city", null),
				(String) param.getOrDefault("t_synopsis", null), (String) param.getOrDefault("t_autograph", null),
				(String) param.getOrDefault("t_vocation", null), (String) param.getOrDefault("t_weixin", null),
				(String) param.getOrDefault("t_qq", null),
				Integer.parseInt(param.getOrDefault("t_age", 0).toString()),
				(String) param.getOrDefault("t_handImg", null),(String) param.getOrDefault("t_marriage",null));

	}
	
	
	/**
	 * 用户上传封面
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = {"replaceCoverImg"},method = {RequestMethod.POST})
	@ResponseBody
	public MessageUtil replaceCoverImg(HttpServletRequest req) {
		
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0),param.getOrDefault("coverImg","")) 
				|| -1 == Integer.valueOf(param.getOrDefault("t_first", -1).toString())) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return this.personalCenterService.saveCoverImg(param.getInt("userId"),param.getString("coverImg"),
				param.getInt("t_first"));

	}
	
	/**
	 * 设为主封面
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = {"setMainCoverImg"},method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil setMainCoverImg(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0),param.getOrDefault("coverImgId","")) || 0 == param.getInt("coverImgId")) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return personalCenterService.setMainCoverImg(param.getInt("userId"),param.getInt("coverImgId"));
	}
	
	/**
	 * 删除封面
	 * @param req
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = {"delCoverImg"} ,method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil delCoverImg(HttpServletRequest req) {
		
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0),param.getOrDefault("coverImgId",""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return personalCenterService.delCoverImg(param.getInt("coverImgId"));
	}
	
	
	

	/**
	 * 获取标签列表
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getLabelList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getLabelList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getLabelList(param.getInt("tokenId"),Integer.parseInt(param.getOrDefault("useType", "1").toString()));

	}


	
	
	/**
	 * 删除用户标签
	 * 
	 * @param userId
	 * @param labelId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "delUserLabel", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil delUserLabel(HttpServletRequest req) {

		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("labelId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.delUserLabel(param.getInt("labelId"));

	}

	/**
	 * 新增用户标签
	 * 
	 * @param userId
	 * @param labelId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "saveUserLabel", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil saveUserLabel(HttpServletRequest req) {

		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("labelId", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.saveUserLabel(param.getInt("userId"), param.getString("labelId"));

	}

	/**
	 * 查询明细
	 * 
	 * @param queyType 1.收入明细 2.支出明细 3.充值明细 4.提现明细
	 * @param userId
	 * @param time
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getWalletDetail", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getWalletDetail(HttpServletRequest req) {

		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("queyType", 0),
				param.getOrDefault("page", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return personalCenterService.getWalletDetail(param.getInt("queyType"), param.getInt("userId"),
				Integer.parseInt(param.getOrDefault("year", "0").toString()),
				Integer.parseInt(param.getOrDefault("month", "0").toString()),
				Integer.parseInt(param.getOrDefault("state", "0").toString()) , param.getInt("page"));

	}

//	/**
//	 * 获取钱包日明细
//	 * 
//	 * @param userId
//	 * @param time
//	 * @param state    1.收入明细 2.消费明细 3.充值明细
//	 * @param response
//	 */
//	@SuppressWarnings("unchecked")
//	@RequestMapping(value = "getWalletDateDetails", method = RequestMethod.POST)
//	@ResponseBody
//	public MessageUtil getWalletDateDetails(HttpServletRequest req, int userId, String time, Integer state, int page,
//			HttpServletResponse response) {
//		JSONObject param = RSACoderUtil.privateDecrypt(req);
//		// 验证传递的参数
//		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("state", 0),
//				param.getOrDefault("page", 0), param.getOrDefault("time", ""))) {
//			// 返回数据
//			return new MessageUtil(-500, "服务器拒绝执行请求!");
//		}
//
//		return this.personalCenterService.getWalletDateDetails(param.getInt("userId"), time, state, page);
//
//	}

	/**
	 * 获取用户是否实名认证
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getUserIsIdentification", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserIsIdentification(HttpServletRequest req) {

		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getUserIsIdentification(param.getInt("userId"));

	}

	/**
	 * 提交实名认证资料
	 * 
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "submitIdentificationData", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil submitData(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}


		Cretification cretification = new Cretification();
		cretification.setT_user_id(param.getInt("userId"));
		cretification.setT_user_photo(param.getOrDefault("t_user_photo","").toString());
		cretification.setT_user_video(param.getOrDefault("t_user_video","").toString());
		cretification.setT_nam(param.getOrDefault("t_nam", "").toString());
		cretification.setT_id_card(param.getOrDefault("t_id_card","").toString());
		cretification.setT_card_back(param.getOrDefault("t_card_back","").toString());
		cretification.setT_card_face(param.getOrDefault("t_card_face","").toString());
		if (StrUtil.hasEmpty(param.getString("t_type"))){
			cretification.setT_type(0);
		}else{
			cretification.setT_type(Integer.parseInt(param.getString("t_type")));
		}
		return this.personalCenterService.submitData(cretification);

	}

	/**
	 * 统计我的推荐数和贡献值
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getUserShareCount", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserShareCount(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getUserShareCount(param.getInt("userId"));

	}

	/**
	 * 我的推广用户
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getShareUserList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getShareUserList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0),
				param.getOrDefault("type", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getShareUserList(param.getInt("userId"), param.getInt("page"),
				param.getInt("type"));
	}

	/**
	 * 获取已经贡献了金币的推广用户
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getUserMakeMoneyList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserMakeMoneyList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getUserMakeMoneyList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 我的潜水用户
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getDivingUserList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getDivingUserList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getDivingUserList(param.getInt("userId"), param.getInt("page"));
	}

	/**
	 * 查看主播资料[查看对方详情]
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	@RequestMapping(value = "getUserData", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserData(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("coverUserId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getSeeUserData(param.getInt("userId"), param.getInt("coverUserId"));

	}

	/**
	 * 1.4版 获取主播资料
	 * 
	 * @param userId
	 * @param anchorId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnchorData", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnchorData(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("anchorId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getSeeUserData(param.getInt("userId"), param.getInt("anchorId"));

	}

	/**
	 * 1.4 版获取个人资料
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getMydata", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getMydata(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getMydata(param.getInt("userId"));

	}

	/**
	 * 获取主播收费设置
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnchorChargeSetup", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnchorChargeSetup(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("anchorId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getAnchorChargeSetup(param.getInt("anchorId"));

	}

	/**
	 * 修改主播收费设置
	 * 
	 * @param t_id
	 * @param t_video_gold
	 * @param t_text_gold
	 * @param t_phone_gold
	 * @param t_weixin_gold
	 * @param t_private_photo_gold
	 * @param t_private_video_gold
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "updateAnchorChargeSetup", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil updateAnchorChargeSetup(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.updateAnchorChargeSetup(
				param.getInt("userId"),
				new BigDecimal(param.getOrDefault("t_video_gold", "0").toString()),
				new BigDecimal(param.getOrDefault("t_text_gold", "0").toString()),
				new BigDecimal(param.getOrDefault("t_phone_gold", "0").toString()),
				new BigDecimal(param.getOrDefault("t_weixin_gold", 0).toString()),
				new BigDecimal(param.getOrDefault("t_qq_gold", 0).toString()),
				new BigDecimal(param.getOrDefault("t_voice_gold", "0").toString()));

	}

	/**
	 * 获取用户已获得的礼物列表
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	@RequestMapping(value = "getGiveGiftList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getGiveGiftList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getGiftList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 获取 礼物榜和亲密榜
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getIntimateAndGift", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getIntimateAndGift(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getIntimateAndGift(param.getInt("userId"));

	}

	/**
	 * 用户评价标签列表
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getEvaluationList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getEvaluationList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getEvaluationList(param.getInt("userId"));

	}

	/**
	 * 获取用户个人资料
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getUserPersonalData", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserPersonalData(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getUserPersonalData(param.getInt("userId"));

	}

	/**
	 * 分享统计
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getShareTotal", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getShareTotal(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getShareTotal(param.getInt("userId"));

	}

	/**
	 * 获取推荐规则数据
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getSpreadAward", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getSpreadAward(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getSpreadAward(param.getInt("userId"));

	}

	/**
	 * 获取昵称是否重复
	 * 
	 * @param nickName
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getNickRepeat", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getNickRepeat(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("nickName", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getNickRepeat(param.getString("nickName"));

	}

	/**
	 * 获取我的私藏
	 * 
	 * @param user
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getMyPrivate", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getMyPrivate(HttpServletRequest req, int userId, int page, HttpServletResponse response) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getMyPrivate(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 删除我的私藏
	 * 
	 * @param photoId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "delMyPrivate", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil delMyPrivate(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("privateId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.delMyPrivate(param.getInt("privateId"));

	}

	/**
	 * 获取浏览记录
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getBrowseList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getBrowseList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getBrowseList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 用户投诉
	 * 
	 * @param userId      投诉人
	 * @param coverUserId 被投诉人
	 * @param comment     投诉内容
	 * @param img         投诉图片(多图以,隔开)
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "saveComplaint", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil saveComplaint(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("coverUserId", 0),
				param.getOrDefault("comment", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.saveComplaint(param.getInt("userId"), param.getInt("coverUserId"),
				param.getString("comment"), param.getOrDefault("img", "").toString());

	}

	/**
	 * 获取视频上传的签名
	 * 
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getVoideSign", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getVoideSign(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.personalCenterService.getVoideSign();

	}

	/**
	 * 添加意见反馈
	 * 
	 * @param t_phone   联系方式
	 * @param content   反馈内容
	 * @param t_img_url 反馈图片
	 * @param t_user_id 反馈人
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "addFeedback", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil addFeedback(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("t_phone", ""), param.getOrDefault("content", ""),
				param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.addFeedback(param.getString("t_phone"), param.getString("content"),
				param.getOrDefault("t_img_url", "").toString(), param.getInt("userId"));

	}

	/**
	 * 获取意见反馈列表
	 * 
	 * @param page
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getFeedBackList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getFeedBackList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("page", 0), param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getFeedBackList(param.getInt("page"), param.getInt("userId"));

	}

	/**
	 * 获取意见反馈详情
	 * 
	 * @param feedBackId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getFeedBackById", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getFeedBackById(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("feedBackId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getFeedBackById(param.getInt("feedBackId"));

	}

	/**
	 * 修改用户是否勿扰状态
	 * 
	 * @param userId
	 * @param disturb  0. 1.
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "updateUserDisturb", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil updateUserDisturb(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0)) || null == param.get("disturb")
				|| param.getInt("disturb") < 0 || param.getInt("disturb") > 1) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.updateUserDisturb(param.getInt("userId"), param.getInt("disturb"));

	}

	/**
	 * 统计当前人收到的未领取的红包数
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getRedPacketCount", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getRedPacketCount(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getRedPacketCount(param.getInt("userId"));

	}

	/**
	 * 获取用户是否是新手
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getUserNew", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserNew(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getUserNew(param.getInt("userId"));

	}

	/**
	 * 申请公会
	 * 
	 * @param userId       申请人编号
	 * @param guildName    公会名称
	 * @param adminName    管理员姓名
	 * @param adminPhone   管理员联系电话
	 * @param anchorNumber 申请主播数
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "applyGuild", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil applyGuild(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("guildName", ""),
				param.getOrDefault("adminName", ""), param.getOrDefault("adminPhone", ""),
				param.getOrDefault("idCard", ""), param.getOrDefault("handImg", ""))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.applyGuild(param.getInt("userId"), param.getString("guildName"),
				param.getString("adminName"), param.getString("adminPhone"), param.getString("idCard"),
				param.getString("handImg"),  Integer.parseInt(param.getOrDefault("anchorNumber", "0").toString()));

	}

	/**
	 * 拉取该主播是否被邀请加入公会
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnchorAddGuild", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnchorAddGuild(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getAnchorAddGuild(param.getInt("userId"));

	}

	/**
	 * 获取公会统计
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getGuildCount", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getGuildCount(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getGuildCount(param.getInt("userId"));

	}

	/**
	 * 主播是否同意加入公会
	 * 
	 * @param guildId  公会编号
	 * @param userId   主播编号
	 * @param isApply  是否同意加入公会 0.否 1.是
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "isApplyGuild", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil isApplyGuild(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("guildId", 0))
				|| null == param.get("isApply") || param.getInt("isApply") < 0 || param.getInt("isApply") > 1) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.isApplyGuild(param.getInt("guildId"), param.getInt("userId"),
				param.getInt("isApply"));

	}

	/**
	 * 获取公会贡献列表
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getContributionList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getContributionList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getContributionList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 获取主播统计(主播贡献明细)
	 * 
	 * @param anchorId
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnthorTotal", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnthorTotal(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("anchorId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.getAnthorTotal(param.getInt("anchorId"), param.getInt("userId"));

	}

	/**
	 * 公会主播贡献明细列表
	 * 
	 * @param anchorId
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getContributionDetail", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getContributionDetail(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("anchorId", 0),
				param.getOrDefault("page", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.getContributionDetail(param.getInt("anchorId"), param.getInt("userId"),
				param.getInt("page"));

	}

	/**
	 * 获取打赏列表
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getRewardList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getRewardList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.getRewardList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 获取用户未读的注册赠送金币消息
	 * 
	 * @param userId
	 * @param response
	 * @param version  1.4
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getGiveGoldMsg", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getGiveGoldMsg(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.getGiveGoldMsg(param.getInt("userId"));

	}

	/**
	 * 设置注册赠送消息为已读
	 * 
	 * @param userId
	 * @param response
	 * @param version  1.4
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "setUpGiveGoldIsRead", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil setUpGiveGoldIsRead(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.setUpGiveGoldIsRead(param.getInt("userId"));

	}

	/**
	 * 用户点赞
	 * 
	 * @param laudUserId
	 * @param coverLaudUserId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "addLaud", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil addLaud(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("coverLaudUserId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.addLaud(param.getInt("userId"), param.getInt("coverLaudUserId"));

	}

	/**
	 * 新增照片或者视频查看次数
	 * 
	 * @param fileId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "addQueryDynamicCount", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil addQueryDynamicCount(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("fileId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.addQueryDynamicCount(param.getInt("fileId"));

	}

	/**
	 * 取消点赞
	 * 
	 * @param userId
	 * @param coverUserId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "cancelLaud", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil cancelLaud(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("coverUserId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.cancelLaud(param.getInt("userId"), param.getInt("coverUserId"));

	}

	/**
	 * 获取当前主播设置视频收费聊天的最大值
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnchorVideoCost", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnchorVideoCost(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.getAnchorVideoCost(param.getInt("userId"));

	}

	/**
	 * 获取推广奖励排行榜
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getSpreadBonuses", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getSpreadBonuses(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.getSpreadBonuses(param.getInt("userId"));

	}

	/**
	 * 获取推广人数排行榜
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getSpreadUser", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getSpreadUser(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.getSpreadUser(param.getInt("userId"));

	}

	/**
	 * 获取主播亲密列表
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnthorIntimateList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnthorIntimateList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}
		return this.personalCenterService.getAnthorIntimateList(param.getInt("userId"), param.getInt("page"));

	}

	/**
	 * 获取礼物排行榜列表
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnthorGiftList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnthorGiftList(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			return new MessageUtil(-500, "服务器拒绝执行请求.");
		}

		return this.personalCenterService.getAnthorGiftList(param.getInt("userId"));

	}
	
	/**
	 * 	获取被浏览记录(谁看过我)
	 * 
	 * @param userId
	 * @param page
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getCoverBrowseList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getCoverBrowseList(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getOrDefault("page", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.getCoverBrowseList(param.getInt("userId"), param.getInt("page"), param.getInt("size"));

	}
	
	/**
	 * 	设置聊天勿扰开关
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "setUpChatSwitch", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil setUpChatSwitch(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0),param.getOrDefault("chatType", 1))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.personalCenterService.setUpChatSwitch(param.getInt("userId"), param.getInt("chatType"), param.getInt("switchType"));

	}
	
	/**
	 * 	获取用户信息(昵称 头像 )
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getUserInfoById", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getUserInfoById(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return this.personalCenterService.getUserInfoById(param.getInt("tokenId"),param.getInt("userId"));

	}
	
	/**
	 * 	主播一键匹配: 获取一个VIP用户
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getVIPUserInfo", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getVIPUserInfo(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return  this.personalCenterService.getVIPUserInfo(param.getInt("userId"),param.getInt("chatType"));
	}
	
	/**
	 * 	用户一键匹配: 获取一个在线主播
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getOnlineAnoInfo", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getOnlineAnoInfo(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		//当前为用户  查找主播    
		return  this.personalCenterService.getOnlineAnoInfo(param.getInt("userId"),param.getInt("chatType"));
	}
	
	/**
	 * 	用户一键获取主播的封面图 5张
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getAnoCoverImg", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getAnoCoverImg(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		//当前为用户  查找主播    
		return  this.personalCenterService.getAnoCoverImg();
	}
	
	/**
	 * 	储存录音
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "saveSounRecording", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil saveSounRecording(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return  this.personalCenterService.saveSounRecording(param);
	}
	
	/**
	 * 	储存录音开关
	 * 
	 * @param userId
	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "getSounRecordingSwitch", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getSounRecordingSwitch(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		return  this.personalCenterService.getSounRecordingSwitch();
	}

	/**
	 *  获取关注列表
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "getCoverFollowList", method = RequestMethod.POST)
	@ResponseBody
	public  MessageUtil getCoverFollowList(HttpServletRequest req){
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0), param.getInt("page"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return personalCenterService.getCoverFollowList(param.getInt("userId"),param.getInt("type"), param.getInt("page"));
	}
	
	/**
	 *  获取VIP状态
	 * @param req
	 * @return
	 */
	@RequestMapping(value = "getUserVipInfo", method = RequestMethod.POST)
	@ResponseBody
	public  MessageUtil getUserVipInfo(HttpServletRequest req){
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		return personalCenterService.getUserVipInfo(param.getInt("userId"));
	}
	
	/**
	 * 永久封号处理
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "userDisable", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil disable(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		//当前为用户  查找主播
		return  this.personalCenterService.userDisable(param.getInt("tokenId"));
	}

	@RequestMapping(value = "uploadPhone", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil uploadPhone(HttpServletRequest req) {
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId", 0))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		String contacts = param.getString("list");
		JSONArray list = JSON.parseArray(contacts);
		return  this.personalCenterService.uploadPhone(param.getInt("userId"),list);
	}
}
