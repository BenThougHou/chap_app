package com.yiliao.control;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yiliao.service.ConsumeService;
import com.yiliao.service.VideoChatService;
import com.yiliao.util.BaseUtil;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.RSACoderUtil;
import com.yiliao.util.RedisUtil;

import net.sf.json.JSONObject;

/**
 * 用户消费控制层
 * 
 * @author Administrator
 *
 */
@Controller
@RequestMapping("app")
public class ConsumeControl {

	@Autowired
	private VideoChatService videoChatService;
	@Autowired
	private ConsumeService consumeService;
	
	@Autowired
	RedisUtil redisUtil;

	/**
	 * 用户查看私密照片
	 * 
//	 * @param consumeUserId
//	 * @param coverConsumeUserId
//	 * @param response
	 */
	@RequestMapping(value = { "seeImgConsume" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil seeImgConsume(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"), param.getInt("photoId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.consumeService.seeImgConsume(param.getInt("tokenId"), param.getInt("coverConsumeUserId"),
				param.getInt("photoId"));

	}

	/**
	 * 查看私密视频
	 * 
//	 * @param consumeUserId
//	 * @param coverConsumeUserId
//	 * @param response
	 */
	@RequestMapping(value = { "seeVideoConsume" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil seeVideoConsume(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"), param.getInt("videoId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.consumeService.seeVideoConsume(param.getInt("tokenId"), param.getInt("coverConsumeUserId"),
				param.getInt("videoId"));

	}

	/**
	 * 查看手机号
	 * 
//	 * @param consumeUserId
//	 * @param coverConsumeUserId
//	 * @param response
	 */
	@RequestMapping(value = { "seePhoneConsume" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil seePhoneConsume(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		String string = redisUtil.get("seePhoneConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"));

		if (StringUtils.isNotBlank(string)) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		redisUtil.set("seePhoneConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"),
				"seePhoneConsume_" + param.getInt("userId"));
		redisUtil.expireSecond("seePhoneConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"), 1000l);
		
		return this.consumeService.seePhoneConsume(param.getInt("tokenId"), param.getInt("coverConsumeUserId"));
	}

	/**
	 * 查看微信
	 * 
//	 * @param consumeUserId
//	 * @param coverConsumeUserId
//	 * @param response
	 */
	@RequestMapping(value = { "seeWeiXinConsume" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil seeWeiXinConsume(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		String string = redisUtil.get("seeWeiXinConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"));

		if (StringUtils.isNotBlank(string)) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		redisUtil.set("seeWeiXinConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"),
				"seeWeiXinConsume_" + param.getInt("userId"));
		redisUtil.expireSecond("seeWeiXinConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"), 1000l);
		
		return this.consumeService.seeWeiXinConsume(param.getInt("tokenId"), param.getInt("coverConsumeUserId"));

	}

	/**
	 * 查看QQ
	 * 
//	 * @param consumeUserId
//	 * @param coverConsumeUserId
//	 * @param response
	 */
	@RequestMapping(value = { "seeQQConsume" }, method = { RequestMethod.POST })
	@ResponseBody
	public MessageUtil seeQQConsume(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		
		String string = redisUtil.get("seeQQConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"));

		if (StringUtils.isNotBlank(string)) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		redisUtil.set("seeQQConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"),
				"seeQQConsume_" + param.getInt("userId"));
		redisUtil.expireSecond("seeQQConsume_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"), 1000l);
		
		return this.consumeService.seeQQConsume(param.getInt("tokenId"), param.getInt("coverConsumeUserId"));

	}

	/**
	 * 非VIP发送文本消息 (non-Javadoc)
//	 * @param consumeUserId 消费者id
//	 * @param coverConsumeUserId 被消费者id
//	 * @param chatType 聊天类型 1:文字 2:图片 3:语音
	 */
	@RequestMapping(value = { "sendTextConsume" }, method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil sendTextConsume(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		// 1:视频  2:语音 3:文字聊天
		boolean flag=videoChatService.checkChatSwitch(3,param.getInt("coverConsumeUserId"));
		if(flag) {
			return new MessageUtil(-5, "Sorry,对方设置私信了勿扰!");
		}
		
		
		//黑名单判断 true 被拉入黑名单
		flag=videoChatService.checkBlackUserInfo(param.getInt("userId"),param.getInt("coverConsumeUserId"));
		if(flag) {
			return new MessageUtil(-5, "Sorry,对方已将您拉入黑名单!");
		}
		return this.consumeService.sendTextConsume(param.getInt("tokenId"), param.getInt("coverConsumeUserId"));
	}

	/**
	 * VIP 查看私密图片或者视频
	 * 
//	 * @param userId
//	 * @param sourceId
	 */
	@RequestMapping(value = "vipSeeData", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil vipSeeData(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("sourceId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.consumeService.vipSeeData(param.getInt("tokenId"), param.getInt("sourceId"));

	}

	/**
	 * 消费者给被消费者发红包
	 * 
//	 * @param consumeUserId
//	 * @param coverConsumeUserId
//	 * @param gold
	 */
	@RequestMapping(value = "sendRedEnvelope", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil sendRedEnvelope(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		if (param.getInt("gold") < 99 || !BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		
		String string = redisUtil.get("userSendRedEnvelope_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"));

		if (StringUtils.isNotBlank(string)) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		redisUtil.set("userSendRedEnvelope_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"),
				"userSendRedEnvelope_" + param.getInt("userId"));
		redisUtil.expireSecond("userSendRedEnvelope_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"), 1000l);
		
		return this.consumeService.sendRedEnvelope(param.getInt("tokenId"), param.getInt("coverConsumeUserId"),
				param.getInt("gold"));

	}

	/**
	 * 领取红包
	 * 
//	 * @param userId
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "receiveRedPacket", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil receiveRedPacket(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.consumeService.receiveRedPacket(Integer.parseInt(param.getOrDefault("t_id","0").toString()), param.getInt("userId"));

	}

	/**
	 * 获取礼物列表
	 * 
//	 * @param response
	 */
	@RequestMapping(value = "getGiftList", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getGiftList(HttpServletRequest req) {

		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.consumeService.getGiftList();

	}

	/**
	 * 获取守护礼物
//	 * @param response
	 */
	@RequestMapping(value = "getGuard", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil getGuard(HttpServletRequest req) {
		try {
			// 解密参数
			JSONObject param = RSACoderUtil.privateDecrypt(req);
			// 验证传递的参数
			if (!BaseUtil.params(param.getInt("userId"))) {
				// 返回数据
				return new MessageUtil(-500, "服务器拒绝执行请求!");
			}
			return this.consumeService.getGuard();
		} catch (Exception e) {
			e.printStackTrace();
			return new MessageUtil(0, "程序异常!");
		}
	}

	/**
	 * 赠送礼物
	 * 
//	 * @param consumeUserId      赠送者
//	 * @param coverConsumeUserId 被赠送者
//	 * @param giftId             礼物编号
//	 * @param giftNum            礼物数量
//	 * @param response
	 */
	@RequestMapping(value = "userGiveGift", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil userGiveGift(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getInt("userId"), param.getInt("coverConsumeUserId"), param.getInt("giftId"),
				param.getInt("giftNum"))) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		String string = redisUtil.get("userGiveGift_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"));

		if (StringUtils.isNotBlank(string)) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}
		redisUtil.set("userGiveGift_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"),
				"userGiveGift_" + param.getInt("userId"));
		redisUtil.expireSecond("userGiveGift_" + param.getInt("userId") + "_" + param.getInt("coverConsumeUserId"), 1000l);
		
		return this.consumeService.userGiveGift(param.getInt("tokenId"), param.getInt("coverConsumeUserId"),
				param.getInt("giftId"), param.getInt("giftNum"));

	}

	/**
	 * vip充值
	 * 
//	 * @param userId    用户编号
//	 * @param setMealId 套餐编号
//	 * @param payType   支付类型 -1.支付宝 -2.微信 -3 恒云支付宝H5 -4恒云支付宝扫码
//	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "vipStoreValue", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil vipStoreValue(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);

		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId",0), param.getOrDefault("setMealId",0),param.getOrDefault("payDeployId", 0)) || param.getInt("payType") < -7
				|| param.getInt("payType") > -1) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.consumeService.vipStoreValue(param.getInt("userId"), param.getInt("setMealId"),
				param.getInt("payType"),param.getInt("payDeployId"));

	}

	/**
	 * 金币充值
	 * 
//	 * @param userId    用户编号
//	 * @param setMealId 套餐编号
//	 * @param payType   支付类型 -1.支付宝 -2.微信 -3 恒云支付宝H5 -4恒云支付宝扫码
//	 * @param response
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping(value = "goldStoreValue", method = RequestMethod.POST)
	@ResponseBody
	public MessageUtil goldStoreValue(HttpServletRequest req) {
		// 解密参数
		JSONObject param = RSACoderUtil.privateDecrypt(req);
		// 验证传递的参数
		if (!BaseUtil.params(param.getOrDefault("userId",0), param.getOrDefault("setMealId",0),param.getOrDefault("payDeployId", 0)) && param.getInt("payType") < -5
				|| param.getInt("payType") > -1) {
			// 返回数据
			return new MessageUtil(-500, "服务器拒绝执行请求!");
		}

		return this.consumeService.goldStoreValue(param.getInt("userId"), param.getInt("setMealId"),
				param.getInt("payType"),param.getInt("payDeployId"));

	}

}
