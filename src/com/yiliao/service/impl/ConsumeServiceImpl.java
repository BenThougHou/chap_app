package com.yiliao.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.yiliao.domain.Balance;
import com.yiliao.domain.MessageEntity;
import com.yiliao.domain.WalletDetail;
import com.yiliao.evnet.PushMesgEvnet;
import com.yiliao.service.ConsumeService;
import com.yiliao.service.GoldComputeService;
import com.yiliao.util.ChannelSettlementThread;
import com.yiliao.util.DateUtils;
import com.yiliao.util.HttpUtil;
import com.yiliao.util.JSON;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.Mid;
import com.yiliao.util.PayCallbackThread;
import com.yiliao.util.PayUtil;
import com.yiliao.util.PoolThread;
import com.yiliao.util.RedisUtil;
import com.yiliao.util.SpringConfig;
import com.yiliao.util.SystemConfig;
import com.yiliao.util.cache.CacheRemove;
import com.yiliao.util.hengYun.PayCodeUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import redis.clients.jedis.Jedis;

/**
 * 消费服务实现累
 *
 * @author Administrator
 */
@Service("consumeService")
public class ConsumeServiceImpl extends ICommServiceImpl implements ConsumeService {


    private GoldComputeService goldComputeService = (GoldComputeService) SpringConfig.getInstance()
            .getBean("goldComputeService");

    @Autowired
    RedisUtil redisUtil;

    /*
     * 用户查看照片 (non-Javadoc)
     *
     * @see com.yiliao.service.app.ConsumeService#SeeImgConsume(int, int)
     */
    @Override
    public MessageUtil seeImgConsume(int consumeUserId, int coverConsumeUserId, int photoId) {
        MessageUtil mu = null;
        try {

            // 更新查看次数
            String uSql = " UPDATE t_album SET t_see_count = t_see_count+1 WHERE t_id = ?  ";
            this.executeSQL(uSql, photoId);

            // 获取改照片是否已经消费过了
            if (queryOrderExits(consumeUserId, coverConsumeUserId, photoId,
                    WalletDetail.CHANGE_CATEGORY_PRIVATE_PHOTO)) {

                // 查看该资料是否还需需要进行存储
                savePrivate(consumeUserId, photoId);

                mu = new MessageUtil();
                mu.setM_istatus(2);
                return mu;
            }

            /* 判断用户是否是VIP VIP用户无需付费 */
            if (!getUserIsVip(consumeUserId)) {

                String sql = "SELECT t_money FROM t_album WHERE t_id = ? AND t_file_type = 0 ";
                Map<String, Object> price = this.getMap(sql, photoId);

                int orderId = this.saveOrder(consumeUserId, coverConsumeUserId, photoId,
                        WalletDetail.CHANGE_CATEGORY_PRIVATE_PHOTO, new BigDecimal(price.get("t_money").toString()));
                // 扣除消费者需要消费的金币
                if (goldComputeService.userConsume(consumeUserId, WalletDetail.CHANGE_CATEGORY_PRIVATE_PHOTO,
                        new BigDecimal(price.get("t_money").toString()), orderId)) {
                    // 分配用户的消费的金币
                    goldComputeService.distribution(new BigDecimal(price.get("t_money").toString()), consumeUserId,
                            coverConsumeUserId, WalletDetail.CHANGE_CATEGORY_PRIVATE_PHOTO, orderId);
                    // 查看该资料是否还需需要进行存储
                    savePrivate(consumeUserId, photoId);

                    mu = new MessageUtil();
                    mu.setM_istatus(1);
                    mu.setM_strMessage("消费成功!");
                } else {
                    this.executeSQL("DELETE FROM t_order WHERE t_id = ? ", orderId);
                    mu = new MessageUtil();
                    mu.setM_istatus(-1);
                    mu.setM_strMessage("余额不足!请充值.");
                }
            } else {
                mu = new MessageUtil();
                mu.setM_istatus(2);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看图片消费异常!", e);
            mu = new MessageUtil(0, "程序异常");
        }
        return mu;
    }

    /*
     * 查看私密视频 (non-Javadoc)
     *
     * @see com.yiliao.service.app.ConsumeService#seeVideoConsume(int, int)
     */
    @Override
    public MessageUtil seeVideoConsume(int consumeUserId, int coverConsumeUserId, int videoId) {
        MessageUtil mu = null;
        try {

            // 更新查看次数
            String uSql = " UPDATE t_album SET t_see_count = t_see_count+1 WHERE t_id = ?  ";
            this.executeSQL(uSql, videoId);

            // 获取该视频是否已经消费过了
            if (queryOrderExits(consumeUserId, coverConsumeUserId, videoId,
                    WalletDetail.CHANGE_CATEGORY_PRIVATE_VIDEO)) {
                // 查看该资料是否还需需要进行存储
                savePrivate(consumeUserId, videoId);
                mu = new MessageUtil();
                mu.setM_istatus(2);
                return mu;
            }
            /* 如果是VIP 直接返回数据 */
            if (getUserIsVip(consumeUserId)) {
                mu = new MessageUtil();
                mu.setM_istatus(2);
                return mu;
            }

            String sql = "SELECT t_money FROM t_album WHERE t_id = ? AND t_file_type = 1";
            Map<String, Object> price = this.getMap(sql, videoId);

            // 保存消费记录 切返回订单编号
            int orderId = this.saveOrder(consumeUserId, coverConsumeUserId, videoId,
                    WalletDetail.CHANGE_CATEGORY_PRIVATE_VIDEO, new BigDecimal(price.get("t_money").toString()));

            // 扣除消费者需要消费的金币
            if (goldComputeService.userConsume(consumeUserId, WalletDetail.CHANGE_CATEGORY_PRIVATE_VIDEO,
                    new BigDecimal(price.get("t_money").toString()), orderId)) {

                // 分配用户的消费的金币
                goldComputeService.distribution(new BigDecimal(price.get("t_money").toString()), consumeUserId,
                        coverConsumeUserId, WalletDetail.CHANGE_CATEGORY_PRIVATE_VIDEO, orderId);

                // 查看该资料是否还需需要进行存储
                savePrivate(consumeUserId, videoId);

                new PoolThread(PoolThread.data_query_pay_video, videoId).start();

                mu = new MessageUtil();
                mu.setM_istatus(1);
                mu.setM_strMessage("消费成功!");
            } else {
                this.executeSQL("DELETE FROM t_order WHERE t_id = ? ", orderId);
                mu = new MessageUtil();
                mu.setM_istatus(-1);
                mu.setM_strMessage("余额不足!请充值.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看私密视频异常!", e);
            mu = new MessageUtil(0, "程序异常!");
        }
        return mu;
    }

    /*
     * 用户查看手机号 (non-Javadoc)
     *
     * @see com.yiliao.service.app.ConsumeService#seePhoneConsume(int, int)
     */
    @Override
    public MessageUtil seePhoneConsume(int consumeUserId, int coverConsumeUserId) {
        MessageUtil mu = null;
        try {

            if (consumeUserId == coverConsumeUserId) {
                Map<String, Object> map = this.getMap("SELECT t_phone FROM t_user WHERE t_id = ?",
                        coverConsumeUserId);

                mu = new MessageUtil();
                mu.setM_istatus(1);
                mu.setM_strMessage("消费成功!");
                mu.setM_object(map.get("t_weixin"));
                return mu;
            }
            // 优先获取主播用户的查看手机号收费设置
            String sql = " SELECT t_phone_gold FROM t_anchor_setup WHERE t_user_id = ? ";
            Map<String, Object> price = this.getMap(sql, coverConsumeUserId);
            // if compere phone query gold 0 return compere not public
            if (0 == new BigDecimal(price.get("t_phone_gold").toString()).intValue()) {
                mu = new MessageUtil();
                mu.setM_istatus(-2);
                return mu;
            }
            // 获取该用户是否已经消费过了查看主播手机号
            if (queryOrderExits(consumeUserId, coverConsumeUserId, 0, WalletDetail.CHANGE_CATEGORY_PHONE)) {
                mu = new MessageUtil();
                mu.setM_istatus(2);
                return mu;
            }
            // 保存消费记录
            int orderId = this.saveOrder(consumeUserId, coverConsumeUserId, 0, WalletDetail.CHANGE_CATEGORY_PHONE,
                    new BigDecimal(price.get("t_phone_gold").toString()));
            // 扣除消费者需要消费的金币
            if (goldComputeService.userConsume(consumeUserId, WalletDetail.CHANGE_CATEGORY_PHONE,
                    new BigDecimal(price.get("t_phone_gold").toString()), orderId)) {

                // 分配用户的消费的金币
                goldComputeService.distribution(new BigDecimal(price.get("t_phone_gold").toString()), consumeUserId,
                        coverConsumeUserId, WalletDetail.CHANGE_CATEGORY_PHONE, orderId);

                // 获取用户的手机号
                Map<String, Object> map = this.getMap("SELECT t_phone FROM t_user WHERE t_id = ?", coverConsumeUserId);

                mu = new MessageUtil();
                mu.setM_istatus(1);
                mu.setM_strMessage("消费成功!");
                mu.setM_object(map.get("t_phone"));
            } else {
                this.executeSQL("DELETE FROM t_order WHERE t_id = ? ", orderId);
                mu = new MessageUtil();
                mu.setM_istatus(-1);
                mu.setM_strMessage("余额不足!请充值.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看用户手机号异常!", e);
            mu = new MessageUtil(0, "程序异常!");
        }
        return mu;
    }

    /*
     * 查看微信号 (non-Javadoc)
     *
     * @see com.yiliao.service.app.ConsumeService#seeWeiXinConsume(int, int)
     */
    @Override
    public MessageUtil seeWeiXinConsume(int consumeUserId, int coverConsumeUserId) {
        MessageUtil mu = null;
        try {

            if (consumeUserId == coverConsumeUserId) {
                Map<String, Object> map = this.getMap("SELECT t_weixin FROM t_user WHERE t_id = ?",
                        coverConsumeUserId);

                mu = new MessageUtil();
                mu.setM_istatus(1);
                mu.setM_strMessage("消费成功!");
                mu.setM_object(map.get("t_weixin"));
                return mu;
            }

            // 获取该用户是否消费过了查看主播微信号
            if (queryOrderExits(consumeUserId, coverConsumeUserId, 0, WalletDetail.CHANGE_CATEGORY_WEIXIN)) {
                mu = new MessageUtil();
                mu.setM_istatus(2);
                return mu;
            }


            String sql = " SELECT t_weixin_gold FROM t_anchor_setup WHERE t_user_id = ? ";

            List<Map<String, Object>> sqlList = getQuerySqlList(sql, coverConsumeUserId);

            if (!sqlList.isEmpty()) {
                // 保存消费记录
                int orderId = this.saveOrder(consumeUserId, coverConsumeUserId, 0, WalletDetail.CHANGE_CATEGORY_WEIXIN,
                        new BigDecimal(sqlList.get(0).get("t_weixin_gold").toString()));

                // 扣除消费者需要消费的金币
                if (goldComputeService.userConsume(consumeUserId, WalletDetail.CHANGE_CATEGORY_WEIXIN,
                        new BigDecimal(sqlList.get(0).get("t_weixin_gold").toString()), orderId)) {

                    // 分配用户的消费的金币
                    goldComputeService.distribution(new BigDecimal(sqlList.get(0).get("t_weixin_gold").toString()),
                            consumeUserId, coverConsumeUserId, WalletDetail.CHANGE_CATEGORY_WEIXIN, orderId);

                    // 获取用户的手机号
                    Map<String, Object> map = this.getMap("SELECT t_weixin FROM t_user WHERE t_id = ?",
                            coverConsumeUserId);

                    mu = new MessageUtil();
                    mu.setM_istatus(1);
                    mu.setM_strMessage("消费成功!");
                    mu.setM_object(map.get("t_weixin"));
                } else {
                    this.executeSQL("DELETE FROM t_order WHERE t_id = ? ", orderId);
                    mu = new MessageUtil();
                    mu.setM_istatus(-1);
                    mu.setM_strMessage("余额不足!请充值.");
                }
            } else {
                mu = new MessageUtil();
                mu.setM_istatus(-1);
                mu.setM_strMessage("主播资料未完善");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看用户微信异常!,查看人【{}】,被查看人【{}】", consumeUserId, coverConsumeUserId, e);
            mu = new MessageUtil(0, "程序异常!");
        }
        return mu;
    }

    /**
     * 非VIP发送文本消息 (non-Javadoc)
     *
     * @param consumeUserId      消费者id
     * @param coverConsumeUserId 被消费者id
     * @param chatType           聊天类型 1:文字 2:图片 3:语音
     */
    @Override
    public synchronized MessageUtil sendTextConsume(int consumeUserId, int coverConsumeUserId) {
        try {
//            if (chatType == 2) {
//                // 只有svip会员才能发送图片
//                String sql = "select t_is_vip from t_user where and t_is_vip = 2  t_id = ? ";
//                Map<String, Object> userMap = this.getMap(sql, consumeUserId);
//                if (userMap != null) {
//                    return this.sendConsume(consumeUserId, coverConsumeUserId);
//                }
//                return new MessageUtil(-5, "发送图片需要升级到SVIP!");
//            } else {
//                return this.sendConsume(consumeUserId, coverConsumeUserId);
//            }
            return this.sendConsume(consumeUserId, coverConsumeUserId);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发送私密聊天信息异常;发送人[{}],接收人[{}]", consumeUserId, coverConsumeUserId, e);
            return new MessageUtil(0, "程序异常!");
        }
    }

    /**
     * 非VIP发送文本消息 (non-Javadoc)
     *
     * @param consumeUserId      消费者id
     * @param coverConsumeUserId 被消费者id
     */
    public synchronized MessageUtil sendConsume(int consumeUserId, int coverConsumeUserId) throws Exception {
        MessageUtil mu = null;
        // 获取当前用户角色
        List<Map<String, Object>> user = this.getQuerySqlList(
                "SELECT * FROM t_user WHERE   t_id = ? AND (t_role = 1 or t_is_vip = 0 or t_is_svip = 0 )", consumeUserId);
//			!getUserIsVip(consumeUserId) &&
        if ((null == user || user.isEmpty())) {
            // 聊天对数据进行自减
            Integer number = this.getPrivateNumber(consumeUserId);
            if (number < 0) {
                redisUtil.set("private_chat_number_" + consumeUserId, String.valueOf(number - 1));
                // 获取对方收费设置
                String sql = " SELECT t_text_gold FROM t_anchor_setup WHERE t_user_id = ? ";
                Map<String, Object> price = this.getMap(sql, coverConsumeUserId);
                // 保存消费记录 并返回消费记录编号
                int orderId = this.saveOrder(consumeUserId, coverConsumeUserId, 0, WalletDetail.CHANGE_CATEGORY_TEXT,
                        new BigDecimal(price.get("t_text_gold").toString()));
                // 扣除消费者需要消费的金币
                if (goldComputeService.userConsume(consumeUserId, WalletDetail.CHANGE_CATEGORY_TEXT,
                        new BigDecimal(price.get("t_text_gold").toString()), orderId)) {

                    //获取系统设置  判断文本聊天  是否需要给用户分配金币
                    //					getMap("", objects)
                    // 分配用户的消费的金币
                    goldComputeService.distribution(new BigDecimal(price.get("t_text_gold").toString()), consumeUserId,
                            coverConsumeUserId, WalletDetail.CHANGE_CATEGORY_TEXT, orderId);

                    mu = new MessageUtil();
                    mu.setM_istatus(1);
                    mu.setM_strMessage("消费成功!");
                } else {
                    this.executeSQL("DELETE FROM t_order WHERE t_id = ? ", orderId);
                    mu = new MessageUtil();
                    mu.setM_istatus(-1);
                    mu.setM_strMessage("余额不足!请充值.");
                }
            } else {
                redisUtil.set("private_chat_number_" + consumeUserId, String.valueOf(number - 1), DateUtils.millis(1));
                // 这里将number再减1是为了给前端一个及时提示
                number -= 1;
                if (number == 0) {
                    redisUtil.set("private_chat_number_" + consumeUserId, String.valueOf(number - 1), DateUtils.millis(1));
                    mu = new MessageUtil(3, "当日私信条数已使用完!");
                } else {
                    mu = new MessageUtil(2, "无需支付!");
                }
            }
        } else {
            return new MessageUtil(2, "无需支付!");
        }
        return mu;
    }

    /**
     * 查询本次订单是否已经存在支付过的类型
     *
     * @param consume
     * @param cover_consume
     * @param consume_score
     * @param consume_type
     * @return fals没有记录 true已有记录
     */
    private boolean queryOrderExits(int consume, int cover_consume, int consume_score, int consume_type) {
        String sql = "SELECT * FROM t_order WHERE t_consume = ?  AND t_cover_consume = ? AND t_consume_type = ? ";

        if (consume_type == WalletDetail.CHANGE_CATEGORY_PRIVATE_PHOTO
                || consume_type == WalletDetail.CHANGE_CATEGORY_PRIVATE_VIDEO) {
            sql = sql + " AND t_consume_score =  " + consume_score;
        }

        List<Map<String, Object>> dataMap = this.getQuerySqlList(sql, consume, cover_consume, consume_type);

        return null == dataMap ? false : dataMap.isEmpty() ? false : true;
    }

    /**
     * 存储订单记录
     *
     * @param consume       消费者
     * @param cover_consume 被消费者
     * @param consume_score 消费资源数据编号
     * @param consume_type  消费类型
     * @param amount        消费金额
     */
    private int saveOrder(int consume, int cover_consume, int consume_score, int consume_type, BigDecimal amount) {

        String sql = "INSERT INTO t_order (t_consume, t_cover_consume, t_consume_type, t_consume_score, t_amount, t_create_time) VALUES (?, ?, ?, ?, ?, ?)";

        return this.getFinalDao().getIEntitySQLDAO().saveData(sql, consume, cover_consume, consume_type, consume_score,
                amount, DateUtils.format(new Date(), DateUtils.FullDatePattern));
    }

    /**
     * 存储用户的私藏
     *
     * @param consume   消费者
     * @param dynamicId 动态编号
     */
    private void savePrivate(int consume, int dynamicId) {
        // 查询需要保存的数据是否已经存在了
        String sql = "SELECT * FROM t_private_collection WHERE t_user_id = ? AND t_album_id = ?";
        List<Map<String, Object>> dataLsit = this.getQuerySqlList(sql, consume, dynamicId);
        // 判断该数据是否已经存在
        if (null == dataLsit || !dataLsit.isEmpty()) {
            // 保存数据
            sql = " INSERT INTO t_private_collection (t_user_id,t_album_id) VALUES ( ?, ?)";
            this.executeSQL(sql, consume, dynamicId);
        }
    }

    /*
     * VIP 查看私密照片或者视频 (non-Javadoc)
     *
     * @see com.yiliao.service.app.ConsumeService#vipSeeData(int, int)
     */
    @Override
    public MessageUtil vipSeeData(int vipUserId, int sourceId) {
        MessageUtil mu = null;
        try {
            savePrivate(vipUserId, sourceId);

            mu = new MessageUtil(1, "查看成功!");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("vip查看私密视频或者照片", e);
            mu = new MessageUtil(0, "程序异常!");
        }
        return mu;
    }

    /*
     * 消费者给被消费者发红包 (non-Javadoc)
     *
     * @see com.yiliao.service.app.ConsumeService#sendRedEnvelope(int, int, int)
     */
    @SuppressWarnings("null")
    @Override
    public MessageUtil sendRedEnvelope(int consumeUserId, int coverConsumeUserId, int gold) {
        MessageUtil mu = null;
        try {

            JSONObject restJson = HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "getUserIsVideo.do",
                    "userId=" + consumeUserId);

            Map<String, Integer> map = null;

            if (null != restJson.get("m_object") && !"null".equals(restJson.getString("m_object"))) {
                map = (Map<String, Integer>) restJson.get("m_object");
            }

            int consumeGold = 0;

            // 如果用户正在进行视频聊天中
            if (null != map && !map.isEmpty()) {
                // 计算出用户已经消费了多少金币
                int timing = map.get("timing") / 60 + 2;
                consumeGold = map.get("deplete") * timing;

                // 判断用户发送的红包金币加上已经视频聊天消费了的金币 是否足够
                if (map.get("gold") < (consumeGold + gold)) {
                    mu = new MessageUtil(-1, "余额不足!,请充值.");
                    return mu;
                } else {
                    // 更新视频聊天中的总金币数
//					map.put("gold", map.get("gold") - gold);
                    restJson = HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "userBuyGold.do",
                            "userId=" + consumeUserId + "&gold=" + gold);
                    if (-1 == restJson.getInt("m_istatus")) {
                        return new MessageUtil(0, "扣除聊天金币异常!");
                    }

                }
            }
            // 保存消费记录
            int orderId = this.saveOrder(consumeUserId, coverConsumeUserId, 0, WalletDetail.CHANGE_CATEGORY_RED_PACKET,
                    new BigDecimal(gold));
            // 扣除消费者需要消费的金币
            if (goldComputeService.userConsume(consumeUserId, WalletDetail.CHANGE_CATEGORY_RED_PACKET,
                    new BigDecimal(gold), orderId)) {

                // 分配用户的消费的金币
                goldComputeService.distribution(new BigDecimal(gold), consumeUserId, coverConsumeUserId,
                        WalletDetail.CHANGE_CATEGORY_RED_PACKET, orderId);

                mu = new MessageUtil();
                mu.setM_istatus(1);
                mu.setM_strMessage("发送红包成功!");

                Map<String, Object> consumeUser = goldComputeService.queryUserData(consumeUserId);

                String message = "收到来自" + consumeUser.get("t_nickName") + "的红包";

                // socket推送
                HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "sendRedPackegNotice.do",
                        "userId=" + coverConsumeUserId);

                // 异步通知
                this.applicationContext
                        .publishEvent(new PushMesgEvnet(new MessageEntity(coverConsumeUserId, message, 0, new Date())));

            } else {
                this.executeSQL("DELETE FROM t_order WHERE t_id = ? ", orderId);
                mu = new MessageUtil();
                mu.setM_istatus(-1);
                mu.setM_strMessage("余额不足!请充值.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("发送红包异常!", e);
            mu = new MessageUtil(0, "程序异常!");
        }

        return mu;
    }

    /*
     * 礼物 (non-Javadoc)
     *
     * @see com.yiliao.service.app.ConsumeService#getGiftList()
     */
    @Override
    public MessageUtil getGiftList() {
        MessageUtil mu = null;

        try {

            String sql = "SELECT t_gift_id,t_gift_name,t_gift_gif_url,t_gift_still_url,t_gift_gold FROM t_gift WHERE t_is_enable = 0 ORDER BY t_gift_gold ASC ";

            List<Map<String, Object>> data = this.getQuerySqlList(sql);

            mu = new MessageUtil();
            mu.setM_istatus(1);
            mu.setM_object(data);

        } catch (Exception e) {
            e.printStackTrace();
            logger.debug("获取礼物列表异常!", e);
            mu = new MessageUtil(0, "程序异常!");
        }

        return mu;
    }

    /**
     * 获取守护礼物
     *
     * @return
     * @throws RuntimeException
     */
    @Override
    public MessageUtil getGuard() throws RuntimeException {
        String sql = "SELECT t_gift_id,t_gift_name,t_gift_gif_url,t_gift_still_url,t_gift_gold FROM t_gift WHERE t_is_enable = 0 and t_is_guard = 1 ";
        List<Map<String, Object>> data = this.getQuerySqlList(sql);
        if (data.size() > 0) {
            return new MessageUtil(1, data.get(0));
        }
        return new MessageUtil(0, "获取守护礼物异常");
    }

    /*
     * 用户赠送礼物 (non-Javadoc)
     *
     * @see com.yiliao.service.app.ConsumeService#userGiveGift(int, int, int, int)
     */
    @Override
    public MessageUtil userGiveGift(int consumeUserId, int coverConsumeUserId, int giftId, int giftNum) {
        MessageUtil mu = null;

        try {
            // 根据编号查询出礼物数据
            Map<String, Object> giftMap = this.getMap("SELECT * FROM t_gift WHERE  t_gift_id = ?", giftId);

            // 得到用户消费的金币数
            BigDecimal totalGold = new BigDecimal(giftMap.get("t_gift_gold").toString())
                    .multiply(new BigDecimal(giftNum)).setScale(2, BigDecimal.ROUND_DOWN);

            JSONObject restJson = HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "getUserIsVideo.do",
                    "userId=" + consumeUserId);

            Map<String, Integer> map = null;

            Object object = restJson.get("m_object");

            if (null != object && !object.toString().equals("null")) {
                map = (Map<String, Integer>) restJson.get("m_object");
            }

            int consumeGold = 0;

            // 如果用户正在进行视频聊天中
            if (null != map && !map.isEmpty()) {
                // 计算出用户已经消费了多少金币
                int timing = map.get("timing") / 60 + 2;
                consumeGold = map.get("deplete") * timing;

                // 判断用户消费金币加上已经视频聊天消费了的金币 是否足够
                if (map.get("gold") < (consumeGold + totalGold.intValue())) {
                    mu = new MessageUtil(-1, "余额不足!,请充值.");
                    return mu;
                } else {
                    // 更新视频聊天中的总金币数
//					map.put("gold", map.get("gold") - totalGold.intValue());
                    restJson = HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "userBuyGold.do",
                            "userId=" + consumeUserId + "&gold=" + totalGold.intValue());
                    if (-1 == restJson.getInt("m_istatus")) {
                        return new MessageUtil(0, "扣除聊天金币异常!");
                    }
                }
            }

            // 保存消费记录
            int orderId = this.saveOrder(consumeUserId, coverConsumeUserId, giftId, WalletDetail.CHANGE_CATEGOR_GIFT,
                    totalGold);

            // 扣除消费者需要消费的金币
            if (goldComputeService.userConsume(consumeUserId, WalletDetail.CHANGE_CATEGOR_GIFT, totalGold, orderId)) {

                // 分配用户的消费的金币
                goldComputeService.distribution(totalGold, consumeUserId, coverConsumeUserId,
                        WalletDetail.CHANGE_CATEGOR_GIFT, orderId);

                new PoolThread(PoolThread.data_query_gift, giftId).start();

                mu = new MessageUtil();
                mu.setM_istatus(1);
                mu.setM_strMessage("赠送成功!");
                if (new BigDecimal(giftMap.get("t_gift_gold").toString()).compareTo(new BigDecimal("0")) >= 0) {
                    //赠送礼物
                    Map<String, Object> userMap1 = this.getMap("select t_nickName,t_handImg from t_user where t_id=?", consumeUserId);
                    Map<String, Object> userMap2 = this.getMap("select t_nickName t_cover_nickName,t_handImg t_cover_handImg from t_user where t_id=?", coverConsumeUserId);
                    HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "sendSocketMsg.do",
                            "t_nickName=" + userMap1.get("t_nickName")
                                    + "&t_handImg=" + userMap1.get("t_handImg")
                                    + "&t_cover_nickName=" + userMap2.get("t_cover_nickName")
                                    + "&t_cover_handImg=" + userMap2.get("t_cover_handImg")
                                    + "&sendType=" + "3"
                                    + "&mid=" + Mid.allGiftSendSocket
                                    + "&recharge=" + ""
                                    + "&t_gift_still_url=" + giftMap.get("t_gift_still_url"));
                }

            } else {
                this.executeSQL("DELETE FROM t_order WHERE t_id = ? ", orderId);
                mu = new MessageUtil();
                mu.setM_istatus(-1);
                mu.setM_strMessage("余额不足!请充值.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("赠送礼物异常!", e);
            mu = new MessageUtil(0, "程序异常!");
        }

        return mu;
    }

    /**
     * 获取用户是否是VIP用户
     *
     * @param userId
     * @return
     */
    private boolean getUserIsVip(int userId) {
        try {

            String vSql = "SELECT t_is_vip FROM t_user WHERE t_id = ?";

            Map<String, Object> vipMap = this.getMap(vSql, userId);

            if ("0".equals(vipMap.get("t_is_vip").toString())) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取用户是否是VIP异常", e);
        }

        return false;
    }

    /**
     * 用户拆红包
     */
    @Override
    public synchronized MessageUtil receiveRedPacket(Integer t_id, int userId) {
        MessageUtil mu = null;
        try {

            List<Map<String, Object>> findBySQLTOMap = null;
            // 拆开指定红包
            if (null != t_id && 0 != t_id) {
                String sql = "SELECT t_redpacket_id,t_hair_userId,t_receive_userId,t_redpacket_content,t_redpacket_gold,t_redpacket_draw,t_redpacket_type,DATE_FORMAT(t_create_time,'%Y-%m-%d %T') AS t_create_time FROM t_redpacket_log WHERE t_redpacket_id = ? AND t_redpacket_draw = 0";
                findBySQLTOMap = this.getQuerySqlList(sql, t_id);
            } else { // 红包列表
                String sql = "SELECT t_redpacket_id,t_hair_userId,t_receive_userId,t_redpacket_content,t_redpacket_gold,t_redpacket_draw,t_redpacket_type,DATE_FORMAT(t_create_time,'%Y-%m-%d %T') AS t_create_time FROM t_redpacket_log WHERE t_receive_userId = ? AND t_redpacket_draw = 0 ;";
                findBySQLTOMap = this.getQuerySqlList(sql, userId);
            }

            if (null == findBySQLTOMap || findBySQLTOMap.isEmpty()) {
                mu = new MessageUtil(-1, "暂无红包!");
            } else {

                BigDecimal totalGold = new BigDecimal(0);

                for (Map<String, Object> m : findBySQLTOMap) {

                    // 统计用户变动前的金额
                    String banSql = "SELECT * FROM t_balance WHERE t_user_id = ? ";

                    Map<String, Object> balance = this.getMap(banSql, m.get("t_receive_userId"));

                    BigDecimal originalGold = null;

                    int chageType = WalletDetail.CHANGE_CATEGORY_RED_PACKET;
                    int goldType = Balance.GOLD_TYPE_PROFIT;
                    /**
                     * 0.赠送红包 1.贡献红包 2.主播认证红包 3.后台赠送提现红包 4:后台赠送充值红包
                     */
                    if ("0".equals(m.get("t_redpacket_type").toString())
                            || "2".equals(m.get("t_redpacket_type").toString())
                            || "3".equals(m.get("t_redpacket_type").toString())) {
                        originalGold = new BigDecimal(balance.get("t_profit_money").toString());
                    } else if("1".equals(m.get("t_redpacket_type").toString())) {
                        chageType = WalletDetail.CHANGE_CATEGOR_RECOMMENDATION;
                        goldType = Balance.GOLD_TYPE_SHARE;
                        originalGold = new BigDecimal(balance.get("t_share_money").toString());
                    } else if("4".equals(m.get("t_redpacket_type").toString())) {
                        goldType = Balance.GOLD_TYPE_RECHARGE;
                        originalGold = new BigDecimal(balance.get("t_recharge_money").toString());
                    }
                    // 入账
                    goldComputeService.saveChangeRecord(userId, originalGold,
                            new BigDecimal(m.get("t_redpacket_gold").toString()), WalletDetail.CHANGE_TYPE_INCOME,
                            chageType, goldType, (Integer) m.get("t_redpacket_id"));
                    // 修改当前记录
                    this.executeSQL("UPDATE t_redpacket_log SET t_redpacket_draw = 1 WHERE t_redpacket_id = ? ",
                            m.get("t_redpacket_id"));

                    totalGold = totalGold.add(new BigDecimal(m.get("t_redpacket_gold").toString()));
                }

                Map<String, Object> re_m = new HashMap<String, Object>();
                re_m.put("totalGold", totalGold.add(new BigDecimal(0)));
                re_m.put("redpachageCount", findBySQLTOMap.size());

                return new MessageUtil(1, re_m);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("拆红包异常!", e);
            mu = new MessageUtil(0, "程序异常!");
        }
        return mu;
    }

    /*
     * 充值VIP(non-Javadoc)
     *
     * @see com.yiliao.service.ConsumeService#vipStoreValue(int, int)
     */
    @Override
    public MessageUtil vipStoreValue(int userId, int setMealId, int payType, int payDeployId) {
        MessageUtil mu = null;
        try {
            // 根据套餐编号得到用户需要支付的金额

            String setMealSql = "SELECT  t_money,t_pid FROM t_vip_setmeal WHERE t_id = ? AND t_is_enable = 0 ";

            Map<String, Object> setMealMap = this.getMap(setMealSql, setMealId);

            String orderNo = "";

            Map<String, Object> map = null;

            String aliPay = null;

            // 从redis中取得支付key
            Map<String, Object> payDeploy = null;

            payDeploy = redisUtil.hmget("pay_deploy_" + payDeployId);

            if (null == payDeploy || payDeploy.isEmpty()) {
                return new MessageUtil(-2, "支付信息未配置!");
            }

            if (payType == -1) {
                orderNo = orderNo + "zf_" + userId + "_" + System.currentTimeMillis();
                aliPay = PayUtil.alipayCreateOrder(orderNo, new BigDecimal(setMealMap.get("t_money").toString()),
                        "VIP充值", payDeploy.get("appId").toString(), payDeploy.get("privateKey").toString(), payDeploy.get("publicKey").toString());
            } else if (payType == -2) {
                orderNo = orderNo + "wx_" + userId + "_" + System.currentTimeMillis();
                // 计算需要支付的金额
                int money = new BigDecimal(setMealMap.get("t_money").toString()).multiply(new BigDecimal(100))
                        .intValue();

                map = PayUtil.wxPay("VIP", orderNo, money, payDeploy.get("appId").toString(), payDeploy.get("mchid").toString(),
                        payDeploy.get("mchidKey").toString());
            } else if (payType == -3) {
                orderNo = "code_" + userId + "_" + System.currentTimeMillis();
                String t_h5_pay_url = redisUtil.get("t_h5_pay_url");
                map = PayCodeUtil.hundredsOfPay(t_h5_pay_url, orderNo,
                        new BigDecimal(setMealMap.get("t_money").toString()), "VIP",
                        payDeploy.get("appId").toString(), payDeploy.get("privateKey").toString(),
                        "101");
            } else if (payType == -4) {
                orderNo = "code_" + userId + "_" + System.currentTimeMillis();
                String t_h5_pay_url = redisUtil.get("t_h5_pay_url");
                map = PayCodeUtil.payAliPayH5(t_h5_pay_url, orderNo,
                        new BigDecimal(setMealMap.get("t_money").toString()), "VIP",
                        payDeploy.get("appId").toString(), payDeploy.get("privateKey").toString(),
                        "101");
            } else if (payType == -5) {
                orderNo = "code_" + userId + "_" + System.currentTimeMillis();
                map = PayCodeUtil.zhifupayUrl(payDeploy.get("appId").toString(), payDeploy.get("mchid").toString(),
                        payDeploy.get("mchidKey").toString(),
                		orderNo, new BigDecimal(setMealMap.get("t_money").toString()).setScale(2, BigDecimal.ROUND_DOWN)
                        , userId, "VIP","WXPAY");
                logger.info("支付接口key参数查询=" + JSONObject.fromObject(map).toString());
            } else if (payType == -6) {
                orderNo = "code_" + userId + "_" + System.currentTimeMillis();
                map = new HashMap<>();
                map.put("userName", SystemConfig.getValue("userName"));
                map.put("path", PayCodeUtil.zhifupayH5(
                		payDeploy.get("privateKey").toString(), payDeploy.get("publicKey").toString(),
                		orderNo, new BigDecimal(setMealMap.get("t_money").toString()).setScale(2, BigDecimal.ROUND_DOWN)
                        , userId, "VIP","ALIPAY"));
                logger.info("支付接口key参数查询=" + JSONObject.fromObject(map).toString());
            } else if (payType == -7){
                orderNo = orderNo + "apple_" + userId + "_" + System.currentTimeMillis();
                map = new HashMap<>();
                map.put("orderNo",orderNo);
                map.put("transactionId",setMealMap.get("t_pid").toString());
            }

            if (StringUtils.isNotBlank(aliPay) || !map.isEmpty()) {

                // 生产订单记录
                String orderSql = "INSERT INTO t_recharge ( t_user_id, t_recharge_money, t_order_no, t_recharge_type, t_payment_type, t_setmeal_id, t_order_state,t_pay_id , t_create_time) VALUES (?,?,?,?,?,?,?,?,?);";

                this.executeSQL(orderSql, userId, setMealMap.get("t_money").toString(), orderNo, 0, payType, setMealId,
                        0, payDeployId, DateUtils.format(new Date(), DateUtils.FullDatePattern));

                mu = new MessageUtil(1, "订单创建成功!");
                mu.setM_object(payType == -1 ? aliPay : map);
            } else {
                mu = new MessageUtil(-1, "创建订单失败!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户{}进行VIP充值异常", userId, e);
            mu = new MessageUtil(0, "程序异常!");
        }
        return mu;
    }

    /**
     * 用户充值金币
     */
    @Override
    public MessageUtil goldStoreValue(int userId, int setMealId, int payType, int payDeployId) {
        MessageUtil mu = null;
        try {
            String smlSql = "SELECT t_pid, t_money FROM t_set_meal WHERE t_id = ? AND t_is_enable = 0 ";

            Map<String, Object> smlMap = this.getMap(smlSql, setMealId);

            String orderNo = "";

            Map<String, Object> map = null;

            String alipay = null;

            // 从redis中取得支付key
            Map<String, Object> payDeploy = redisUtil.hmget("pay_deploy_" + payDeployId);
            if (payDeploy.isEmpty()) {
                return new MessageUtil(-2, "支付信息未配置!");
            }
            if (payType == -1) {
                orderNo = orderNo + "zf_" + userId + "_" + System.currentTimeMillis();

                alipay = PayUtil.alipayCreateOrder(orderNo, new BigDecimal(smlMap.get("t_money").toString()), "金币充值",
                        payDeploy.get("appId").toString(), payDeploy.get("privateKey").toString(), payDeploy.get("publicKey").toString());
            } else if (payType == -2) {
                orderNo = orderNo + "wx_" + userId + "_" + System.currentTimeMillis();

                // 计算需要支付的金额
                int money = new BigDecimal(smlMap.get("t_money").toString()).multiply(new BigDecimal(100)).intValue();

                map = PayUtil.wxPay("金币充值", orderNo, money, payDeploy.get("appId").toString(), payDeploy.get("mchid").toString(),
                        payDeploy.get("mchidKey").toString());
            } else if (payType == -3) {
                orderNo = "code_" + userId + "_" + System.currentTimeMillis();
                String t_h5_pay_url = redisUtil.get("t_h5_pay_url");
                map = PayCodeUtil.hundredsOfPay(t_h5_pay_url, orderNo,
                        new BigDecimal(smlMap.get("t_money").toString()), "GOLD",
                        payDeploy.get("appId").toString(), payDeploy.get("privateKey").toString(),
                        "101");
                logger.info("支付接口key参数查询=" + JSONObject.fromObject(map).toString());
            } else if (payType == -4) {
                orderNo = "code_" + userId + "_" + System.currentTimeMillis();
                String t_h5_pay_url = redisUtil.get("t_h5_pay_url");
                map = PayCodeUtil.payAliPayH5(t_h5_pay_url, orderNo,
                        new BigDecimal(smlMap.get("t_money").toString()), "GOLD",
                        payDeploy.get("appId").toString(), payDeploy.get("privateKey").toString(),
                        "101");
                logger.info("支付接口key参数查询=" + JSONObject.fromObject(map).toString());
            } else if (payType == -5) {
                orderNo = "code_" + userId + "_" + System.currentTimeMillis();
                map = PayCodeUtil.zhifupayUrl(payDeploy.get("appId").toString(), payDeploy.get("mchid").toString(),
                        payDeploy.get("mchidKey").toString(),orderNo, new BigDecimal(smlMap.get("t_money").toString()).setScale(2, BigDecimal.ROUND_DOWN)
                        , userId, "VIP","WXPAY");
                logger.info("支付接口key参数查询=" + JSONObject.fromObject(map).toString());
            } else if (payType == -6) {
                orderNo = "code_" + userId + "_" + System.currentTimeMillis();
                map = new HashMap<>();
                map.put("userName", SystemConfig.getValue("userName"));
                map.put("path", PayCodeUtil.zhifupayH5(
                		payDeploy.get("privateKey").toString(),
                		payDeploy.get("publicKey").toString(),
                		orderNo, new BigDecimal(smlMap.get("t_money").toString()).setScale(2, BigDecimal.ROUND_DOWN)
                        , userId, "VIP","ALIPAY"));
                logger.info("支付接口key参数查询=" + JSONObject.fromObject(map).toString());
            }
            else if (payType == -7){
                orderNo = orderNo + "apple_" + userId + "_" + System.currentTimeMillis();
                map = new HashMap<>();
                map.put("orderNo",orderNo);
                map.put("transactionId",smlMap.get("t_pid").toString());
            }
            // 处理已成功的数据
            if (StringUtils.isNotBlank(alipay) || !map.isEmpty()) {
                // 生产订单记录
                String orderSql = "INSERT INTO t_recharge ( t_user_id, t_recharge_money, t_order_no, t_recharge_type, t_payment_type, t_setmeal_id, t_order_state,t_pay_id , t_create_time) VALUES (?,?,?,?,?,?,?,?,?);";
                this.executeSQL(orderSql, userId, smlMap.get("t_money").toString(), orderNo, 1, payType, setMealId, 0,
                        payDeployId, DateUtils.format(new Date(), DateUtils.FullDatePattern));
                mu = new MessageUtil(1, "订单创建成功!");
                mu.setM_object(payType == -1 ? alipay : map);
            } else {
                mu = new MessageUtil(-1, "创建订单失败!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("用户充值金币异常", e);
            mu = new MessageUtil(0, "程序异常!");
        }
        return mu;
    }

    /*
     * 支付成功回调(non-Javadoc)
     *
     * @see com.yiliao.service.ConsumeService#wxPayNotify(java.lang.String,
     * java.lang.String)
     */
    @Override
    public synchronized void payNotify(String t_order_no, String t_tripartite_order) {

        try {
            logger.info("--进入了支付回调--");
            logger.info("当前接收到的订单号->{},第三方订单号->{}", t_order_no, t_tripartite_order);

            String qSql = "SELECT * FROM t_recharge WHERE t_order_no = ?  ";

            List<Map<String, Object>> list = this.getQuerySqlList(qSql, t_order_no);
            // 如果已经修改了 那么暂停通知
            if (!list.isEmpty() && list.get(0).get("t_order_state").toString().equals("1")) {
                logger.info("已经回调过了，订单号->{}", t_order_no);
                return;
            }

            // 根据订单号更新订单信息
            String upSql = "UPDATE t_recharge SET t_order_state= 1 , t_tripartite_order=?,t_fulfil_time=? WHERE t_order_no= ?";
            this.executeSQL(upSql, t_tripartite_order, DateUtils.format(new Date(), DateUtils.FullDatePattern),
                    t_order_no);

            // 查询当前支付订单的用户编号
            String sql = "SELECT t_id,t_user_id,t_setmeal_id,t_recharge_type,t_recharge_money FROM t_recharge WHERE t_order_no= ? AND t_tripartite_order = ? ";

            Map<String, Object> userData = this.getMap(sql, t_order_no, t_tripartite_order);
            Map<String, Object> userMap = this.getMap("select t_nickName,t_handImg from t_user where t_id=?  ", userData.get("t_user_id"));
            String recharge = "";
            String sendType = "";
            // 删除模拟呼叫视频消息
            HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "delSimulationVideo.do",
                    "userId=" + userData.get("t_user_id"));
            // 推送挂断呼叫

            // 删除模拟呼叫视频消息
            HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "sendNoticeBreakVideo.do",
                    "userId=" + userData.get("t_user_id"));

            /*********** 处理CPS联盟逻辑 ************/
            new ChannelSettlementThread(Integer.valueOf(userData.get("t_user_id").toString()),
                    new BigDecimal(userData.get("t_recharge_money").toString()),
                    Integer.valueOf(userData.get("t_recharge_type").toString()),
                    Integer.valueOf(userData.get("t_id").toString())).start();
            ;
            /************ CPS联盟处理逻辑完成 ************/
            // 判断用户是充值金币还是VIP充值
            // 0 VIP充值 1.金币充值
            if ("0".equals(userData.get("t_recharge_type").toString())) {
                // 根据套餐编号得到套餐数据时长
                String smalSql = "SELECT t_duration,t_gold,t_vip_type FROM t_vip_setmeal WHERE t_id = ?";
                Map<String, Object> smalMap = this.getMap(smalSql, userData.get("t_setmeal_id"));

                if (!smalMap.isEmpty()) {
                    // 根据用户编号得到用户的VIP数据
                    String vipSql = "SELECT * FROM t_vip WHERE t_user_id = ? and t_vip_type = ? ";
                    List<Map<String, Object>> vipData = this.getQuerySqlList(vipSql, userData.get("t_user_id"), smalMap.get("t_vip_type"));

                    // 得到VIP结束时间
                    Date time = null;

                    // 从来没有开通过VIP
                    if (null == vipData || vipData.isEmpty()) {

                        time = DateUtils.addMonth(new Date(), Integer.parseInt(smalMap.get("t_duration").toString()));

                        // 新增VIP记录
                        String inSql = "INSERT INTO t_vip (t_user_id, t_openUp_time, t_end_time,t_vip_type) VALUES (?,?,?,?)";

                        this.executeSQL(inSql, userData.get("t_user_id"),
                                DateUtils.format(new Date(), DateUtils.FullDatePattern),
                                DateUtils.format(time, DateUtils.FullDatePattern),
                                smalMap.get("t_vip_type"));

                    } else {

                        Date endTime = DateUtils.parse(vipData.get(0).get("t_end_time").toString(),
                                DateUtils.FullDatePattern);
                        // 开通过 但是VIP已经过期了
                        if (endTime.getTime() < System.currentTimeMillis()) {

                            time = DateUtils.addMonth(new Date(), Integer.parseInt(smalMap.get("t_duration").toString()));
                            // 修改用户VIP信息
                            String upVipSql = "UPDATE t_vip SET  t_openUp_time=?, t_end_time=?,t_vip_type=? WHERE t_id=? ";
                            this.executeSQL(upVipSql, DateUtils.format(new Date(), DateUtils.FullDatePattern),
                                    DateUtils.format(time, DateUtils.FullDatePattern), smalMap.get("t_vip_type"), vipData.get(0).get("t_id"));

                        } else { // 开通过切VIP没有过期

                            time = DateUtils.addMonth(endTime, Integer.parseInt(smalMap.get("t_duration").toString()));

                            // 修改用户的的VIP到期时间
                            String upVipSql = "UPDATE t_vip SET t_end_time=? WHERE t_id=? and t_vip_type=?";

                            this.executeSQL(upVipSql, DateUtils.format(time, DateUtils.FullDatePattern),
                                    vipData.get(0).get("t_id"), smalMap.get("t_vip_type"));
                        }

                    }

                    // 修改用户为VIP
                    String upVipSql = "UPDATE t_user SET";
                    //判断开通的vip类型
                    if (Integer.parseInt(smalMap.get("t_vip_type").toString()) == 2) {
                        upVipSql = upVipSql + " t_is_svip = 0 ";
                    } else {
                        upVipSql = upVipSql + " t_is_vip = 0 ";
                    }
                    upVipSql = upVipSql + " WHERE t_id = ? ";

                    this.executeSQL(upVipSql, userData.get("t_user_id"));

                    /************* 设置用户消费流水 ******************/

                    sql = "INSERT INTO t_wallet_detail (t_user_id, t_change_type, t_change_category, t_change_front, t_value, t_change_after, t_change_time,t_sorece_id)"
                            + "VALUES ( ?, ?, ?, ?, ?, ?, ?, ?);";

                    this.executeSQL(sql, userData.get("t_user_id"), WalletDetail.CHANGE_TYPE_PAY, // 类别 支出
                            WalletDetail.CHANGE_CATEGOR_VIP, // 类型 购买VIP
                            0, 0, 0, DateUtils.format(new Date(), DateUtils.FullDatePattern), userData.get("t_id"));

                    /************* 设置用户消费流水完 ******************/

                    // 推送消息
                    String message = "恭喜您VIP开通成功!VIP到期时间为:" + DateUtils.format(time, DateUtils.FullDatePattern);
                    if (Integer.parseInt(smalMap.get("t_vip_type").toString()) == 2) {
                        message = "恭喜您SVIP开通成功!SVIP到期时间为:" + DateUtils.format(time, DateUtils.FullDatePattern);
                    }

                    recharge = smalMap.get("t_duration").toString();
                    sendType = "1";

                    // 异步通知
                    this.applicationContext.publishEvent(new PushMesgEvnet(new MessageEntity(
                            Integer.parseInt(userData.get("t_user_id").toString()), message, 0, new Date())));
                }
            } else { // 金币充值
                // 查询套餐
                String smalSql = "SELECT t_gold FROM t_set_meal WHERE t_id = ? ";
                Map<String, Object> smlMap = this.getMap(smalSql, userData.get("t_setmeal_id"));

                // 查询用户的充值金额
                String banSql = "SELECT  t_recharge_money FROM t_balance WHERE t_user_id = ? ";
                Map<String, Object> userBanMap = this.getMap(banSql, userData.get("t_user_id"));

                // 调用用户账户变动记录
                this.goldComputeService.saveChangeRecord(Integer.parseInt(userData.get("t_user_id").toString()),
                        new BigDecimal(userBanMap.get("t_recharge_money").toString()),
                        new BigDecimal(smlMap.get("t_gold").toString()), WalletDetail.CHANGE_TYPE_INCOME,
                        WalletDetail.CHANGE_CATEGORY_RECHARGE, Balance.GOLD_TYPE_RECHARGE,
                        (Integer) userData.get("t_id"));

                String message = "恭喜!本次成功充值" + smlMap.get("t_gold") + "个金币";
                recharge = smlMap.get("t_gold").toString();
                sendType = "2";
                //socket 推送
                // 异步通知
                this.applicationContext.publishEvent(new PushMesgEvnet(new MessageEntity(
                        Integer.parseInt(userData.get("t_user_id").toString()), message, 0, new Date())));

                /**
                 * 调用接口 此接口 用于 如果用户在 进行视频聊天 那么把金币加入到聊天计费中
                 */
                HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "addRoomRate.do",
                        "userId=" + userData.get("t_user_id") + "&gold=" + smlMap.get("t_gold") + "&goldType=" + 2);

//				Map<String, Integer> map = VideoTiming.timingUser
//						.get(Integer.parseInt(userData.get("t_user_id").toString()));
//				// 如果用户正在进行视频聊天中
//				if (null != map && !map.isEmpty()) {
//					// 更新视频聊天中的总金币数
//					map.put("gold", map.get("gold") + Integer.parseInt(smlMap.get("t_gold").toString()));
//				}
                /***** 完成操作 *******/
            }
            //内容 动态效果
            HttpUtil.httpClentJson(SystemConfig.getValue("chat_mina_url") + "sendSocketMsg.do",
                    "t_nickName=" + userMap.get("t_nickName")
                            + "&t_handImg=" + userMap.get("t_handImg")
                            + "&t_cover_nickName=" + ""
                            + "&t_cover_handImg=" + ""
                            + "&sendType=" + sendType
                            + "&mid=" + Mid.allGiftSendSocket
                            + "&recharge=" + recharge
                            + "&t_gift_still_url=" + "");

            new PoolThread(PoolThread.data_query_recharges, list.get(0).get("t_recharge_money"), Integer.valueOf(userData.get("t_user_id").toString())).start();
            //启动线程处理剩下的逻辑
            //1.给推广人赠送用户首冲金币
            //2.切换默认支付方式
            new PayCallbackThread(Integer.valueOf(userData.get("t_user_id").toString())).start();

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("支付成功回调异常;当前第三方订单号：{},系统内部订单号:{}", t_tripartite_order, t_order_no, e);
        }
    }

    /**
     * 获取订单Id
     *
     * @return
     */
    public int getOrderId() {
        List<Map<String, Object>> arr = this.getQuerySqlList("SELECT t_id FROM t_order ORDER BY t_id DESC LIMIT 1;");
        return arr.isEmpty() ? 1 : (Integer) arr.get(0).get("t_id") + 1;
    }

    /**
     * 1、商户需要验证该通知数据中的out_trade_no是否为商户系统中创建的订单号，
     * 2、判断total_amount是否确实为该订单的实际金额（即商户订单创建时的金额），
     * 3、验证app_id是否为该商户本身。上述1、2、3有任何一个验证不通过，则表明本次通知是异常通知，务必忽略。
     * 在上述验证通过后商户必须根据支付宝不同类型的业务通知，正确的进行不同的业务处理，并且过滤重复的通知结果数据。
     * 在支付宝的业务通知中，只有交易通知状态为TRADE_SUCCESS或TRADE_FINISHED时，支付宝才会认定为买家付款成功。
     *
     * @param params
     * @throws AlipayApiException
     */
    @Override
    public void getAlipayPublicKey(Map<String, String> params) {
        try {
            String orderNo = params.get("out_trade_no");
            String appId = params.get("app_id");
            BigDecimal payMoney = new BigDecimal(params.get("total_amount"));

            String qSql = "SELECT * FROM t_recharge WHERE t_order_no = ? ";

            List<Map<String, Object>> list = this.getQuerySqlList(qSql, orderNo);
            // 如果已经修改了 那么暂停通知
            if (null == list || list.isEmpty()) {
                logger.info("订单不存在，订单号->{}", orderNo);
                throw new AlipayApiException("订单不存在!");
            }

            // 获得支付的金额在否一致
            if (payMoney.compareTo(new BigDecimal(list.get(0).get("t_recharge_money").toString())) != 0) {
                logger.info("{}支付金额异常", orderNo);
                throw new AlipayApiException("{}支付金额异常!");
            }
            // 获取支付信息
            Map<String, Object> alipay = redisUtil.hmget("pay_deploy_" + list.get(0).get("t_pay_id"));

            if (null == alipay || alipay.isEmpty()) {

                List<Map<String, Object>> sqlList = this.getQuerySqlList(
                        "SELECT t_alipay_appid appId,t_alipay_public_key publicKey,t_alipay_private_key privateKey FROM t_alipay_setup WHERE t_pay_id = ?",
                        list.get(0).get("t_pay_id"));

                if (null != sqlList && !sqlList.isEmpty()) {
                    alipay.put("appId", sqlList.get(0).get("appId").toString());
                    alipay.put("publicKey", sqlList.get(0).get("publicKey").toString());
                    alipay.put("privateKey", sqlList.get(0).get("privateKey").toString());
                    // 缓存
                    redisUtil.hmset("pay_deploy_" + list.get(0).get("t_pay_id"), alipay, 0l);
                }
            }

            // 验证appId 是否一致
            if (!appId.equals(alipay.get("appId").toString())) {
                logger.info("{} 异常", appId);
                throw new AlipayApiException("appId异常");
            }

            // 调用sdk验证签名是否通过
//            if (!AlipaySignature.rsaCheckV1(params, alipay.get("publicKey").toString(), "UTF-8", "RSA2")) {
//                logger.info("回调支付签名异常");
//                throw new AlipayApiException("回调支付签名异常!");
//            }

            // 调用正常处理逻辑
            payNotify(params.get("out_trade_no"), params.get("trade_no"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取支付宝公钥
     */
    @Override
    public String getAlipayPublicKey() {
        try {
            String qSql = "SELECT t_alipay_public_key FROM t_alipay_setup";

            return this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(qSql).get("t_alipay_public_key")
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getAlipayAppId() {
        try {
            String qSql = "SELECT t_alipay_appid FROM t_alipay_setup";
            return this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(qSql).get("t_alipay_appid")
                    .toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void asynchronousProcessing(int userId) {
        try {
            logger.info("当前用户编号-->{}", userId);
            // 获取用户是否是首次充值
            List<Map<String, Object>> sqlList = getQuerySqlList(
                    "SELECT * FROM t_recharge WHERE t_order_state = 1 AND t_user_id = ?", userId);
            // 获取系统设置首次充值赠送多少金币和是否切换支付配置
            Map<String, Object> sysMap = getMap("SELECT t_id,t_first_prize,t_default_pay FROM t_system_setup");

            logger.info("获取到的数据-->{}", JSONArray.fromObject(sqlList).toString());
            // 用户第一次充值
            if (null != sqlList && sqlList.size() == 1) {
                logger.info("---进入了用户首冲奖励---");
                // 获取当前用户是否存在推荐人
                List<Map<String, Object>> user = getQuerySqlList(
                        "SELECT t_referee FROM t_user WHERE t_id = ? AND t_referee > 0", userId);
                // 表示用户存在推广人
                if (null != user && !user.isEmpty()) {

                    // 查询出推广人的分享金币
                    Map<String, Object> map = getMap("SELECT t_share_money FROM t_balance WHERE t_user_id = ?",
                            user.get(0).get("t_referee"));

                    // 写入订单记录
                    int orderId = saveOrder(userId, Integer.parseInt(user.get(0).get("t_referee").toString()),
                            Integer.valueOf(sysMap.get("t_id").toString()), WalletDetail.CHANGE_CATEGOR_FIRST_PRIZE,
                            new BigDecimal(sysMap.get("t_first_prize").toString()));
                    // 调用用户账户变动记录
                    this.goldComputeService.saveChangeRecord(Integer.parseInt(user.get(0).get("t_referee").toString()),
                            new BigDecimal(map.get("t_share_money").toString()),
                            new BigDecimal(sysMap.get("t_first_prize").toString()), WalletDetail.CHANGE_TYPE_INCOME,
                            WalletDetail.CHANGE_CATEGOR_FIRST_PRIZE, Balance.GOLD_TYPE_SHARE, orderId);
                }
            }
            logger.info("---切换默认支付方式---");
            // 切换系统默认的支付方式
            if ("1".equals(sysMap.get("t_default_pay").toString())) {
                List<Map<String, Object>> payDispose = getQuerySqlList("SELECT * FROM t_pay_dispose where t_is_scrap = 1");

                int index = -1;
                // 得到当前默认的下标
                for (int i = 0; i < payDispose.size(); i++) {
                    if (payDispose.get(i).get("t_is_default").toString().equals("1")) {
                        index = i;
                        executeSQL("UPDATE t_pay_dispose SET t_is_default = -1 WHERE t_id = ?",
                                payDispose.get(i).get("t_id"));
                        break;
                    }
                }
                int t_id = 0;
                if ((index + 1) == payDispose.size()) {
                    t_id = Integer.valueOf(payDispose.get(0).get("t_id").toString());
                } else {
                    t_id = Integer.valueOf(payDispose.get((index + 1)).get("t_id").toString());
                }
                // 执行修改默认支付配置
                executeSQL("UPDATE t_pay_dispose SET t_is_default = 1 WHERE t_id = ? ", t_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public MessageUtil seeQQConsume(int consumeUserId, int coverConsumeUserId) {
        MessageUtil mu = null;
        if (consumeUserId == coverConsumeUserId) {
            Map<String, Object> map = this.getMap("SELECT t_qq FROM t_user WHERE t_id = ?",
                    coverConsumeUserId);

            mu = new MessageUtil();
            mu.setM_istatus(1);
            mu.setM_strMessage("消费成功!");
            mu.setM_object(map.get("t_weixin"));
            return mu;
        }
        try {

            // 获取该用户是否消费过了查看主播微信号
            if (queryOrderExits(consumeUserId, coverConsumeUserId, 0, WalletDetail.CHANGE_CATEGORY_QQ)) {
                mu = new MessageUtil();
                mu.setM_istatus(2);
                return mu;
            }

            String sql = " SELECT t_qq_gold FROM t_anchor_setup WHERE t_user_id = ? ";

            List<Map<String, Object>> sqlList = getQuerySqlList(sql, coverConsumeUserId);

            if (!sqlList.isEmpty()) {
                // 保存消费记录
                int orderId = this.saveOrder(consumeUserId, coverConsumeUserId, 0, WalletDetail.CHANGE_CATEGORY_QQ,
                        new BigDecimal(sqlList.get(0).get("t_qq_gold").toString()));

                // 扣除消费者需要消费的金币
                if (goldComputeService.userConsume(consumeUserId, WalletDetail.CHANGE_CATEGORY_QQ,
                        new BigDecimal(sqlList.get(0).get("t_qq_gold").toString()), orderId)) {

                    // 分配用户的消费的金币
                    goldComputeService.distribution(new BigDecimal(sqlList.get(0).get("t_qq_gold").toString()),
                            consumeUserId, coverConsumeUserId, WalletDetail.CHANGE_CATEGORY_QQ, orderId);

                    // 获取用户的手机号
                    Map<String, Object> map = this.getMap("SELECT t_qq FROM t_user WHERE t_id = ?",
                            coverConsumeUserId);

                    mu = new MessageUtil();
                    mu.setM_istatus(1);
                    mu.setM_strMessage("消费成功!");
                    mu.setM_object(map.get("t_qq"));
                } else {
                    this.executeSQL("DELETE FROM t_order WHERE t_id = ? ", orderId);
                    mu = new MessageUtil();
                    mu.setM_istatus(-1);
                    mu.setM_strMessage("余额不足!请充值.");
                }
            } else {
                mu = new MessageUtil();
                mu.setM_istatus(-1);
                mu.setM_strMessage("主播资料未完善");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查看用户QQ异常!,查看人【{}】,被查看人【{}】", consumeUserId, coverConsumeUserId, e);
            mu = new MessageUtil(0, "程序异常!");
        }
        return mu;
    }

    /**
     * 得到私信剩余条数
     *
     * @param consumeUserId
     * @return
     */
    private Integer getPrivateNumber(int consumeUserId) {
        return Integer.valueOf(String.valueOf(redisUtil.exists("private_chat_number_" + consumeUserId) ? redisUtil.get("private_chat_number_" + consumeUserId) : -1));
    }

    /**
     * 支付派回调地址
     *
     * @param orderId         订单id
     * @param channelOrderNum 第三方订单号
     * @param payAount        金额
     */
    @Override
    public void zhifupaiH5_callback(String orderId, String channelOrderNum, BigDecimal payAount) {
        try {
            String qSql = "SELECT * FROM t_recharge WHERE t_order_no = ? AND t_recharge_money = ? ";
            List<Map<String, Object>> list = this.getQuerySqlList(qSql, orderId, payAount);

            // 如果订单不存在
            if (null == list || list.isEmpty()) {
                logger.info("订单不存在，订单号->{}", orderId);
                throw new AlipayApiException("订单不存在!");
            }
            // 获得支付的金额在否一致
            if (payAount.compareTo(new BigDecimal(list.get(0).get("t_recharge_money").toString())) != 0) {
                logger.info("{}支付金额异常", orderId);
                throw new AlipayApiException("{}支付金额异常!");
            }
            // 调用正常处理逻辑
            payNotify(orderId, channelOrderNum);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
