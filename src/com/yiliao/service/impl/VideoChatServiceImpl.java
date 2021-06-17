package com.yiliao.service.impl;

import java.text.NumberFormat;
import java.util.List;
import java.util.Map;

import com.yiliao.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yiliao.domain.Room;
import com.yiliao.service.VideoChatService;

import net.sf.json.JSONObject;

/**
 * 视频聊天
 * 
 * @author Administrator
 */
@Service("videoChatService")
public class VideoChatServiceImpl extends ICommServiceImpl implements VideoChatService {

	@Autowired
	private RedisUtil redisUtil;
	/**
	 * 获取签名
	 */
	@Override
	public MessageUtil getImUserSig(int userId) {
		MessageUtil mu = null;
		try {
			String userSig = WebRTCSigApi.getInstance().genUserSig(userId + "", 7200);

			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(userSig);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("IM获取用户签名异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

	@Override
	public MessageUtil getVideoChatAutograph(int userId, int anthorId) {
		MessageUtil mu = null;
		try {
			//初衷：加入缓存限制用户并发呼叫，实际对现有的自动呼叫逻辑有影响
//			String user_ano = redisUtil.get("USER_IS_BUSY_"+anthorId);
//			String user_use = redisUtil.get("USER_IS_BUSY_"+userId);
//			if(user_ano!=null||user_use!=null) {
//				return new MessageUtil(-2, "用户正忙,请稍后再试.");
//			}
			
//			redisUtil.setTime("USER_IS_BUSY_"+anthorId, "1", 1500l);
//			redisUtil.setTime("USER_IS_BUSY_"+userId, "1", 1500l);
			
			// 给用户分配房间号
			int roomId = this.getFinalDao().getIEntitySQLDAO().saveData("insert into t_room_time_log "
					+ "(t_user_id,t_anchor_id,t_create_time) values (?,?,now())",userId,anthorId);
//			Room rm = new Room(roomId);
//			rm.setCreateTime(System.currentTimeMillis());
			// 删除可用当前可用房间

//			RoomTimer.useRooms.put(rm.getRoomId(), rm);

			JSONObject json = new JSONObject();
			json.put("roomId", roomId);

			// 获取用户金币数
//			Map<String, Object> userBalance = this.getMap(
//					"SELECT SUM(t_recharge_money+t_profit_money+t_share_money) AS totalBalance FROM t_balance WHERE t_user_id = ? ",
//					userId);
//
//			// 获取主播联系需要的金币
//			// 获取被链接人每分钟需要消耗多少金币
//			String videoGoldSql = "SELECT t_video_gold FROM t_anchor_setup WHERE t_user_id = ?";
//
//			List<Map<String, Object>> anchorList = this.getQuerySqlList(videoGoldSql, anthorId);
//
//			if (anchorList.isEmpty()) {
//				logger.info("当前主播编号-->{}", anthorId);
//				return new MessageUtil(-1, "--参数异常--");
//			}
//
//			Map<String, Object> videoGold = anchorList.get(0);
//
//			// 用户金额
//			BigDecimal userBal = new BigDecimal(userBalance.get("totalBalance").toString());
//			// 主播聊天金额
//			BigDecimal anthBal = new BigDecimal(videoGold.get("t_video_gold").toString());
//
//			//获取用户余额
//			Map<String, Object> map = getMap("SELECT t_fail_gold  FROM t_system_setup");
//			
//			logger.info("系统设置的金币->{}",map.toString());
//			logger.info("用户的金币数->{}",userBal);
//			logger.info("比对结果->{}",new BigDecimal(map.get("t_fail_gold").toString()).compareTo(userBal) <= 0);
//			
//			// 判断用户的金币是否满足聊天计时
//			if (null == userBal || userBal.compareTo(anthBal) == -1 || 
//					new BigDecimal(map.get("t_fail_gold").toString()).compareTo(userBal) >= 0) {
//				json.put("onlineState", -1);
//			} else {
//				if (userBal.compareTo(BigDecimal.valueOf(0)) > 0) {
//
//					if (userBal.compareTo(anthBal) == 0) {
//						json.put("onlineState", 1);
//					} else if (anthBal.multiply(BigDecimal.valueOf(2)).setScale(0, BigDecimal.ROUND_DOWN)
//							.compareTo(userBal) >= 0 && userBal.compareTo(anthBal) > 0) {
//						json.put("onlineState", 1);
//					}
//				}
//			}
			
			json.put("onlineState", 0);
			//主播呼叫主播 判断对方身份
			String sql="select t_role from t_user where t_id=? ";
			Map<String, Object> map = this.getMap(sql, anthorId);
			json.put("coverRole", map.get("t_role"));
			
			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(json);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("用户获取签名异常!", e);
			mu = new MessageUtil(0, "程序异常!");

		}
		return mu;
	}

	/*
	 * 获取privateMapKey(non-Javadoc)
	 * 
	 * @see com.yiliao.service.VideoChatService#getVideoChatPriavteMapKey(int, int)
	 */
	@Override
	public MessageUtil getVideoChatPriavteMapKey(int userId, int roomId) {
		MessageUtil mu = null;
		try {

			String privateMapKey = WebRTCSigApi.getInstance().genPrivateMapKey(userId + "", roomId, 300);

			mu = new MessageUtil();

			mu.setM_istatus(1);
			mu.setM_object(privateMapKey);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("用户获取privateMapKey异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}
	/**
	 * 计算百分比
	 * @param total
	 * @param number
	 * @return
	 */
	public String calculationPercent(int total, int number) {

		NumberFormat numberFormat = NumberFormat.getInstance();

		// 设置精确到小数点后2位
		numberFormat.setMaximumFractionDigits(2);

		return numberFormat.format((float) number / (float) total * 100);
	}

	@Override
	public Boolean checkVipSwitch(int type,int userId) {
		try {
			//1 拨打视频 2:接听视频 返回 false:VIP打开 并判断是否为VIP  true 取反
			Map<String, Object> map = this.getMap("select t_dial_video,t_answer_video,t_dial_anchor_video from t_system_setup ");
			String sql="select t_is_vip,t_is_svip from t_user where t_id=?";
			if(type==1) {
				if(map.get("t_dial_video").toString().equals("1")) {
					Map<String, Object> userMap = this.getMap(sql,userId);
					if(userMap.get("t_is_vip").toString().equals("1")&&userMap.get("t_is_svip").toString().equals("1")) {
						return true;
					}
				}
			} else if(type==2){
				if(map.get("t_answer_video").toString().equals("1")) {
					Map<String, Object> userMap = this.getMap(sql,userId);
					if(userMap.get("t_is_vip").toString().equals("1")&&userMap.get("t_is_svip").toString().equals("1")) {
						return true;
					}
				}
			}else if(type==3){
				if(map.get("t_dial_anchor_video").toString().equals("1")) {
					Map<String, Object> userMap = this.getMap(sql,userId);
					if(userMap.get("t_is_vip").toString().equals("1")&&userMap.get("t_is_svip").toString().equals("1")) {
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 判断主播是否还有免费次数
	 * @param userId 用户id
	 * @param coverLinkUserId 主播id
	 * @return true 允许拨打视频   false 已无次数,不能拨打视频
	 */
	@Override
	public MessageUtil svipSwitch(int userId, int coverLinkUserId){
		String sql="select t_is_svip from t_user where t_id=?";
		Map<String, Object> userMap = this.getMap(sql,userId);
		if(userMap.get("t_is_svip").toString().equals("0")){
			// 查询用户信息,判断用户是否是svip
			if(!redisUtil.exists("free_svip_number_"+coverLinkUserId)){
				
				//不存在
				Map<String, Object> map = this.getMap("select t_free_svip from t_system_setup ");
				redisUtil.setTime("free_svip_number_"+coverLinkUserId,map.get("t_free_svip").toString(), DateUtils.millis(1));
				return new MessageUtil(1,"允许拨打视频");
				
			}else if (Integer.parseInt(redisUtil.get("free_svip_number_"+coverLinkUserId)) > 0 ){
				return new MessageUtil(1,"允许拨打视频");
			}
			// 没有拨打次数了
			return new MessageUtil(2,"女神今日免费聊天次数已被其他SVIP占用，如继续拨打，会产生费用。");
		}
		return new MessageUtil(1,"允许拨打视频");
	}

	
	@Override
	public Boolean checkChatSwitch(int type,int userId) {
		try {
			// // 1:视频  2:语音 3:文字聊天  返回 false:VIP打开 并判断是否为VIP  true 取反
			Map<String, Object> map = this.getMap("select t_is_not_disturb,t_voice_switch,t_text_switch from t_user where t_id=? ",userId);
			
			if(type==1) {
				if(map.get("t_is_not_disturb").toString().equals("0")) {
						return true;
				}
			} else if(type==2){
				if(map.get("t_voice_switch").toString().equals("0")) {
						return true;
				}
			} else if(type==3){
				if(map.get("t_text_switch").toString().equals("0")) {
					return true;
			}
		}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public Boolean checkBlackUserInfo(int userId, int coverUserId) {
		try {
			String sqlString="select t_id from t_black_user where t_user_id = ? and t_cover_user_id=?  ";
			List<Map<String,Object>> blackUserMap = this.getQuerySqlList(sqlString, coverUserId,userId);	
			if(blackUserMap!=null&&!blackUserMap.isEmpty()) {
					return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}  
		return false;
	}
	
	
	@Override
	public MessageUtil addVideoScreenshotInfo(int userId,int videoUserId, int videoAnchorUserId, int roomId,String url) {
		
		try {
			//挂断
			HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "breakLink.do",
					"userId=" + userId +"&breakType=" +3 + "&roomId=" + roomId);
			
			//防止未挂断就推送封号了
			Thread.sleep(3000);
			this.executeSQL("insert into t_video_screenshot_log"
					+ " (t_user_id,t_anchor_id,t_room_id,t_screenshot_img_url,t_create_time) "
					+ "values"
					+ " (?,?,?,?,now()) ",videoUserId,videoAnchorUserId,roomId, url);
			//封号
			HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "handleIllegalityUser.do",
					"userId=" + userId+"&imgUrl=" + url);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return new MessageUtil(1, "添加成功");
	}
	
	@Override
	public MessageUtil getVideoScreenshotStatus() {
		MessageUtil mu = null;
		try {
			Map<String, Object> map = this.getMap("select t_screenshot_user_switch, t_screenshot_anchor_switch, ifnull(t_screenshot_time_list,'') t_screenshot_time_list,t_screenshot_video_switch from t_system_setup");
			
			String[] split = map.get("t_screenshot_time_list").toString().split(",");
			map.put("t_screenshot_time_list", split);
			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(map);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("IM获取用户签名异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}	
}
