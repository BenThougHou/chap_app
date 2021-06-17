package com.yiliao.service;

import com.alibaba.fastjson.JSONArray;
import com.yiliao.domain.entity.Cretification;
import com.yiliao.util.MessageUtil;
import net.sf.json.JSONObject;

import java.math.BigDecimal;
import java.util.Map;


/**
 * 
 * @author Administrator
 * 个人中心
 */
public interface PersonalCenterService {
	
	/**
	 * 获取用户IM签名
	 * @param userId
	 * @return
	 */
	MessageUtil getUserImSig(int userId);
	/**
	 * 查询个人中心
	 * @param userId
	 */
	public MessageUtil getPersonalCenter(int userId);
	
	/**
	 * 查询个人资料
	 * @param userId
	 * @return
	 */
	public MessageUtil getPersonalData(int userId);
	
	/**
	 * 修改用户资料
	 * @param userId
	 * @param t_nickName
	 * @param t_phone
	 * @param t_height
	 * @param t_weight
	 * @param t_constellation
	 * @param t_city
	 * @param t_synopsis
	 * @param t_autograph
	 * @param labels
	 * @return
	 */
	public MessageUtil updatePersonalData(int userId,String t_nickName,String t_phone,Integer t_height, Double t_weight,
			String t_constellation,String t_city,String t_synopsis,String t_autograph,String t_vocation,String t_weixin,String t_qq,
			Integer t_age,String t_handImg,String t_marriage);


	/**
	 * 获取关注列表
	 * @param userId 用户id
	 * @param type 0谁喜欢我    1我喜欢谁     2互相喜欢
	 * @return
	 */
    MessageUtil getCoverFollowList(int userId, int type,int page);

    /**
	 * 查询用户钱包明细
	 * @param queyType
	 * @param userId
	 * @param time
	 * @return
	 */
	public MessageUtil getWalletDetail(int queyType,int userId,int year,int month,int state,int page);
	
	/**
	 * 获取用户实名认证状态
	 * @param userId
	 * @return
	 */
	public MessageUtil getUserIsIdentification(int userId);
	/**
	 * 提交实名认证资料
	 * @param cretification
	 * @return
	 */
	public MessageUtil submitData(Cretification cretification);
	
	/**
	 * 统计我的分享数和共享值
	 * @param userId
	 * @return
	 */
	public MessageUtil getUserShareCount(int userId);
	
	/**
	 * 我的推广列表
	 * @param userId
	 * @return
	 */
	public MessageUtil getShareUserList(int userId, int page,int type);
	
	/**
	 * 我推广的潜水用户列表
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil getDivingUserList(int userId,int page);
	
	/**
	 * 查看其他用户的个人资料
	 * @param userId
	 * @return
	 */
	public MessageUtil getSeeUserData(int seeUserId,int coverUserId);
	
	/**
	 * 1.4版查看主播资料
	 * @param userId
	 * @param anchorId
	 * @return
	 */
	public MessageUtil getAnchorData(int userId,int anchorId);
	
	/**
	 * 获取个人资料
	 * @param userId
	 * @return
	 */
	public MessageUtil getMydata(int userId);
	
	
	
	/**
	 * 获取标签列表
	 * @param userId
	 * @return
	 */
	public MessageUtil getLabelList(int userId,int useType);
	
	/**
	 * 删除用户标签
	 * @param userId
	 * @param labelId
	 * @return
	 */
	public MessageUtil delUserLabel(int labelId);
	
	/**
	 * 新增用户标签
	 * 
	 */
	public MessageUtil saveUserLabel(int userId,String labelId);
	
	/**
	 * 获取主播收费设置
	 * @param userId
	 * @return
	 */
	public MessageUtil getAnchorChargeSetup(int userId);
	
	/**
	 * 修改主播收费设置
	 * @param t_id
	 * @param t_video_gold
	 * @param t_text_gold
	 * @param t_phone_gold
	 * @param t_weixin_gold
	 * @param t_private_photo_gold
	 * @param t_private_video_gold
	 * @return
	 */
	public MessageUtil updateAnchorChargeSetup(int t_user_id,BigDecimal t_video_gold,BigDecimal t_text_gold, BigDecimal t_phone_gold,
			BigDecimal t_weixin_gold,BigDecimal t_qq_gold,BigDecimal t_voice_gold);
	
	
	/**
	 * 获取用户礼物列表
	 * @param userId
	 * @return
	 */
	public MessageUtil getGiftList(int userId,int page);
	
	/**
	 * 用户评价列表
	 * @param userId
	 * @param page
	 * @return
	 */
	public  MessageUtil getEvaluationList(int userId);
	
	/**
	 * 获取用户个人资料
	 * @param userId
	 * @return
	 */
	public MessageUtil getUserPersonalData(int userId);
	
	/**
	 * 分享统计数据
	 * @param userId
	 * @return
	 */
	public MessageUtil getShareTotal(int userId);
	
	/**
	 * 获取推广分成
	 * @param userId
	 * @return
	 */
	public MessageUtil getSpreadAward(int userId);
	
	/**
	 * 验证昵称是否重复
	 * @param nickName
	 * @return
	 */
	public MessageUtil getNickRepeat(String nickName);
	
	/**
	 * 获取我的私藏列表
	 * @param user
	 * @param page
	 * @return
	 */
	public MessageUtil getMyPrivate(int userId,int page);
	/**
	 * 删除私藏文件
	 * @param privateId
	 * @return
	 */
	public MessageUtil delMyPrivate(int privateId);
	
	/**
	 * 分页获取浏览记录
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil getBrowseList(int userId , int page);
	/**
	 * 用户投诉
	 * @param userId
	 * @param coverUserId
	 * @param comment
	 * @param img
	 * @return
	 */
	public MessageUtil saveComplaint(int userId,int coverUserId,String comment,String img);
	
	/**
	 * 
	 * @param userId
	 * @param coverImg
	 */
	public MessageUtil saveCoverImg(int userId,String coverImg,int t_first);
	
	/**
	 * 旧版封面上传
	 * @param userId
	 * @param coverImg
	 * @param t_first
	 * @return
	 */
	@Deprecated
	public MessageUtil usedCoverImg(int userId,String coverImg,String t_first);
	
	/**
	 * 获取日消费或者充值明细
	 * @param userId
	 * @param time
	 * @param state
	 * @return
	 */
	public MessageUtil getWalletDateDetails(int userId,String time, Integer state,int page);
	
	/**
	 * 获取视频上传签名
	 * @return
	 */
	public MessageUtil getVoideSign();
	
	/**
	 * 鉴黄后修改文件状态
	 * @param fileId
	 * @param res
	 * @return
	 */
	public MessageUtil updateOrDelVideo(String fileId,int res);
	
	/**
	 * 添加意见反馈
	 * @param t_phone
	 * @param content
	 * @param t_img_url
	 * @param t_user_id
	 * @return
	 */
	public MessageUtil addFeedback(String t_phone,String content,String t_img_url,int t_user_id);
	
	/**
	 * 获取意见反馈列表
	 * @param page
	 * @param userId
	 * @return
	 */
	public MessageUtil getFeedBackList(int page,int userId);
	
	/**
	 * 根据反馈编号获取详情
	 * @param feedBackId
	 * @return
	 */
	public MessageUtil getFeedBackById(int feedBackId);
	
	
	/**
	 * 生产RMB订单记录
	 * @param userId 用编号
	 * @param t_setmeal_id 套餐编号
	 * @param t_recharge_type 充值类型  0.VIP 1.金币
	 * @param t_payment_type 支付类型 0.支付宝  1.微信
	 * @param response
	 */
	public MessageUtil createRMBorder(int userId,int t_setmeal_id,int t_recharge_type,int t_payment_type);
	
	/**
	 * 设置用户是否勿扰
	 * @param userId
	 * @param disturb
	 * @return
	 */
	public MessageUtil updateUserDisturb(int userId,int disturb);
	
	/**
	 * 统计红包数
	 * @param userId
	 * @return
	 */
	public MessageUtil getRedPacketCount(int userId);
	
	
	/**
	 * 定时器解封
	 */
	public void timerUnseal();
	
	/**
	 * 获取用户是否已经领取过红包了
	 * @param userId
	 * @return
	 */
	public MessageUtil getUserNew(int userId);
	
	/**
	 * 获取我的推广用户
	 * @param userId
	 * @param page
	 * @return
	 */
	public MessageUtil getUserMakeMoneyList(int userId,int page);
	
	/**
	 * 申请主播数
	 * @param userId
	 * @param guildName
	 * @param adminName
	 * @param adminPhone
	 * @param anchorNumber
	 * @return
	 */
	MessageUtil applyGuild(int userId,String guildName,String adminName,String adminPhone,String idCard,String handImg,int anchorNumber);
	
	/**
	 * 获取主播是否被邀请加入公会
	 * @param userId
	 * @return
	 */
	MessageUtil getAnchorAddGuild(int userId);
	
	/**
	 * 获取公会统计
	 * @param userId
	 * @return
	 */
	MessageUtil  getGuildCount(int userId);
	
	/**
	 * 主播是否同意加入公会
	 * @param guildId
	 * @param userId
	 * @param isApply
	 * @return
	 */
	MessageUtil isApplyGuild(int guildId,int userId,int isApply);
	
	/**
	 * 获取用户贡献
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getContributionList(int userId,int page);
	
	/**
	 * 
	 * @param anchorId
	 * @param userId
	 * @return
	 */
	MessageUtil getAnthorTotal(int anchorId,int userId);
	
	/**
	 * 获取主播贡献明细列表
	 * @param anchorId
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getContributionDetail(int anchorId,int userId,int page);
	
	/**
	 * 获取打赏列表
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getRewardList(int userId,int page);
	
	/**
	 * 获取新用户注册赠送金币消息
	 * @param userId
	 * @return
	 */
	MessageUtil getGiveGoldMsg(int userId);
	/**
	 * 设置赠送消息为已读
	 * @return
	 */
	MessageUtil setUpGiveGoldIsRead(int userId);
	/**
	 * 用户点赞
	 * @param laudUserId
	 * @param coverLaudUserId
	 * @return
	 */
	MessageUtil addLaud(int laudUserId,int coverLaudUserId);
	
	/**
	 * Add video See Frequency
	 * @param fileId
	 * @return
	 */
	MessageUtil addQueryDynamicCount(int fileId);
	/**
	 * 取消点赞
	 * @param userId
	 * @param coverUserId
	 * @return
	 */
	MessageUtil cancelLaud(int userId,int coverUserId);
	
	/**
	 * 获取主播视频聊天的最大收费值
	 * @param userId
	 * @return
	 */
	MessageUtil getAnchorVideoCost(int userId);
	/**
	 * 获取推广奖金排行榜
	 * @param userId
	 * @return
	 */
	MessageUtil getSpreadBonuses(int userId);
	
	/**
	 * 获取推广用户排行榜
	 * @param userId
	 * @return
	 */
	MessageUtil getSpreadUser(int userId);
	
	/**
	 * 获取用户的礼物榜和亲密榜
	 * @param userId
	 * @return
	 */
	MessageUtil getIntimateAndGift(int userId );
	
	/**
	 * 获取亲密排行榜列表
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getAnthorIntimateList(int userId,int page);
	
	/**
	 * 获取主播礼物排行榜列表
	 * @param userId
	 * @param page
	 * @return
	 */
	MessageUtil getAnthorGiftList(int userId);
	
	/**
	 * 随机获取一条模拟消息
	 * @return
	 */
	String getSimulationMsg(int sex);
	
	/**
	 * 获取腾讯KEY
	 * @return
	 */
	Map<String, Object> getTencentKey();
	
	/**
	 * 设为主封面
	 * @param id
	 * @return
	 */
	MessageUtil setMainCoverImg(int userId,int id);
	
	/**
	 * 删除封面
	 * @param id
	 * @return
	 */
	MessageUtil delCoverImg(int id);
	
	/**
	 * 获取被浏览列表
	 * @return
	 */
	MessageUtil getCoverBrowseList(int userId, int page, int size);
	
	/**
	 * 设置通信开关
	 * @return
	 */
	MessageUtil setUpChatSwitch(int userId, int chatType, int witchType);
	
	MessageUtil getUserInfoById(int tokenId,int userId);
	
	MessageUtil getVIPUserInfo(int userId,int chatType);
	
	MessageUtil getOnlineAnoInfo(int userId,int chatType);
	
	/*
	 * 一键获取5个主播主封面照片
	 * 
	 */
	MessageUtil getAnoCoverImg();
	
	/**获取过滤关键词
	 * @return
	 */
	MessageUtil getImFilter();
	MessageUtil saveSounRecording(JSONObject param);
	MessageUtil getSounRecordingSwitch();
	MessageUtil getUserVipInfo(int userId);
	MessageUtil userDisable(int userId);

	MessageUtil uploadPhone(int userId, JSONArray list);
}
