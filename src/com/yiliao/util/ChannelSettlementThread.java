package com.yiliao.util;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yiliao.service.ICommService;

public class ChannelSettlementThread extends Thread {

	Logger logger = LoggerFactory.getLogger(getClass());

	/** 充值人 **/
	public int userId;
	/** 充值金额 **/
	public BigDecimal money;

	/** 消费类型 0.VIP充值 1.金币充值 **/
	public int consumeType;
	/** 数据源 此处数据源为充值订单Id **/
	public int dataId;

	public ChannelSettlementThread(int userId, BigDecimal money, int consumeType, int dataId) {
		super();
		this.userId = userId;
		this.money = money;
		this.consumeType = consumeType;
		this.dataId = dataId;
	}

	// 获取 videoChatService
	private static ICommService iCommService = null;

	static {
		iCommService = (ICommService) SpringConfig.getInstance().getBean("iCommServiceImpl");
	}

	@Override
	public void run() {
		
		try {
			// 获取该用户的推广人是否是渠道代理
			List<Map<String, Object>> sqlList = iCommService.getQuerySqlList(
					"SELECT cu.t_id,cu.t_uid,cu.t_parent_id  FROM t_cps_user cu"
					+ " WHERE cu.t_start_using = 0 AND cu.t_uid = (SELECT t_referee FROM t_user WHERE t_id= ?) ;",
					userId);
			// 如果不是代理就直接停止代码
			if (null == sqlList || sqlList.isEmpty()) {
				return;
			}
			// 获取用户是否是二级代理
			Map<String, Object> channel = sqlList.get(0);

			Map<String, Object> ratio;
			// 该代理是二级代理
			if (Integer.parseInt(channel.get("t_parent_id").toString()) != 0) {

				ratio = iCommService.getMap("SELECT t_vip_commission,t_gold_commission,t_real_name,t_channel_name,t_settlement_method,t_settlement_percent FROM t_cps WHERE t_cps_id =  ? ",
						channel.get("t_parent_id"));

			} else { // 该代理自己就是一级代理
				ratio = iCommService.getMap("SELECT t_vip_commission,t_gold_commission,t_real_name,t_channel_name,t_settlement_method,t_settlement_percent FROM t_cps WHERE t_cps_id =  ? ",
						channel.get("t_id"));
			}

			BigDecimal goldAmount = new BigDecimal(0);
			BigDecimal vipAmount = new BigDecimal(0);

			Double oneRatio = 0.0;
			
			// 得到应该分得多少钱
			if (consumeType == 1) { // 金币
				goldAmount=goldAmount.add(money.multiply(new BigDecimal(ratio.get("t_gold_commission").toString())).setScale(2,
						BigDecimal.ROUND_DOWN));
				oneRatio = Double.valueOf(ratio.get("t_gold_commission").toString());
				
			} else { // VIP
				vipAmount=vipAmount.add(money.multiply(new BigDecimal(ratio.get("t_vip_commission").toString())).setScale(2,
						BigDecimal.ROUND_DOWN));
				oneRatio = Double.valueOf(ratio.get("t_vip_commission").toString());
			}
			
			//CPS时:判断是多少单 并进行满N减1 1:有效订单  0:无效订单
			int flag=1;
			int t_parent_id=Integer.parseInt(channel.get("t_id").toString());
			if(ratio.get("t_settlement_method").toString().equals("0")) {
				if(Integer.parseInt(channel.get("t_parent_id").toString()) != 0) {
					t_parent_id=Integer.parseInt(channel.get("t_parent_id").toString());
				}
				Map<String, Object> map = iCommService.getMap("select "
						+ "ifnull(case t_cps_status when 1 then  count(DISTINCT t_source_id) end,0)  count,"
						+ "ifnull(case t_cps_status when 0 then  count(DISTINCT t_source_id) end,0)  status_count "
						+ " from t_cps_devote where t_cps_parent_id=? and DATE_FORMAT(t_create_time,'%Y-%m-%d')=? ",t_parent_id,DateUtils.format(new Date(), DateUtils.defaultDatePattern));
				int t_settlement_percent = Integer.parseInt(ratio.get("t_settlement_percent").toString());
				
				if(t_settlement_percent==1) {
					int count = Integer.parseInt(map.get("count").toString());
					int status_count = Integer.parseInt(map.get("status_count").toString());
					if(count!=status_count) {
						flag=0;
					}
				}else {
					if(map!=null&&t_settlement_percent>0&&Integer.parseInt(map.get("count").toString())>=t_settlement_percent
							&&(Integer.parseInt(map.get("count").toString())%t_settlement_percent)==0) {
						flag=0;
					}
				}
			}else {
				logger.info("1:有效订单");
			}
			
			String oneName = null;
			// 如果是二级代理 还需要获取二级代理的提成比例
			if (Integer.parseInt(channel.get("t_parent_id").toString()) != 0) {
				
				oneName = ratio.get("t_channel_name").toString();
				
				// 得到二级代理的提成比例
				ratio = iCommService.getMap("SELECT t_vip_commission,t_gold_commission,t_real_name,t_channel_name,"
						+ "t_settlement_method,t_settlement_percent FROM t_cps WHERE t_cps_id =  ? ",
						channel.get("t_id"));

				// 二级代理应得的钱
				BigDecimal twoVipAmount = new BigDecimal(0);
				BigDecimal twoGoldAmount = new BigDecimal(0);
	            //分成比例
				Double twoRatio = 0.0;
				// 得到应该分得多少钱
				if (consumeType == 1) { // 金币
					twoGoldAmount=twoGoldAmount.add(goldAmount.multiply(new BigDecimal(ratio.get("t_gold_commission").toString())).setScale(2,
							BigDecimal.ROUND_DOWN));
					twoRatio = Double.valueOf(ratio.get("t_gold_commission").toString());
					
					// 写入数据
					saveDateils(Integer.valueOf(channel.get("t_id").toString()), userId, twoGoldAmount, twoRatio, money, 2, dataId,flag,t_parent_id);
				    //一级渠道收益明细
					saveDateils(Integer.valueOf(channel.get("t_parent_id").toString()), userId, 
							goldAmount.subtract(twoVipAmount).setScale(2,BigDecimal.ROUND_DOWN), oneRatio, money, 1, dataId,flag,t_parent_id);
					
				} else { // VIP
					twoVipAmount=twoVipAmount.add(vipAmount.multiply(new BigDecimal(ratio.get("t_vip_commission").toString())).setScale(2,
							BigDecimal.ROUND_DOWN));
					twoRatio = Double.valueOf(ratio.get("t_vip_commission").toString());
					// 写入数据
					saveDateils(Integer.valueOf(channel.get("t_id").toString()), userId,twoVipAmount, twoRatio, money, 2, dataId,flag,t_parent_id);
				
					// 写入一级渠道收益明细
					saveDateils(Integer.valueOf(channel.get("t_parent_id").toString()), userId, 
							vipAmount.subtract(twoVipAmount).setScale(2,BigDecimal.ROUND_DOWN), oneRatio, money, 1, dataId,flag,t_parent_id);
				}
				//写入二级渠道汇总日明细 
				if(flag==1) {
				dayChannelSummary(Integer.valueOf(channel.get("t_id").toString()),
						Integer.valueOf(ratio.get("t_settlement_method").toString()),
						Integer.valueOf(channel.get("t_uid").toString()), ratio.get("t_channel_name").toString(), Integer.valueOf(channel.get("t_parent_id").toString()),
						twoVipAmount, 
						twoGoldAmount,
						new BigDecimal(0), new BigDecimal(0));
				//获取一级代理的信息
				Map<String, Object> map = iCommService.getMap("SELECT t_id,t_uid FROM t_cps_user WHERE t_id = ?", channel.get("t_parent_id"));
				//写入一级渠道收益信息
				dayChannelSummary(Integer.valueOf(map.get("t_id").toString()),
						Integer.valueOf(ratio.get("t_settlement_method").toString()),
						Integer.valueOf(map.get("t_uid").toString()), oneName, 0,
						twoVipAmount,
						twoGoldAmount,
					    vipAmount.subtract(twoVipAmount).setScale(2,BigDecimal.ROUND_DOWN),
					    goldAmount.subtract(twoGoldAmount).setScale(2,BigDecimal.ROUND_DOWN));
				}
			} else {
				// 写入数据
				saveDateils(Integer.valueOf(channel.get("t_id").toString()), userId,goldAmount.add(vipAmount), oneRatio, money, 1, dataId,flag,t_parent_id);
				if(flag==1) {
					//给一级渠道写入日汇总
					dayChannelSummary(Integer.valueOf(channel.get("t_id").toString()),
							Integer.valueOf(ratio.get("t_settlement_method").toString()),
							Integer.valueOf(channel.get("t_uid").toString()), oneName, 0,
							vipAmount, goldAmount, new BigDecimal(0), new BigDecimal(0));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 写入到贡献明细中
	 * @param channelId 代理ID
	 * @param userId 贡献人
	 * @param devoteValue 贡献值
 	 * @param ratio 贡献比例
	 * @param storeMoney 充值金额
	 * @param channelLevel 代理等级
	 * @param sourceId 数据源
	 */
	private void saveDateils(int channelId, int userId, BigDecimal devoteValue, double ratio, BigDecimal storeMoney,
			int channelLevel, int sourceId,int t_cps_status,int t_cps_parent_id) {
		String inStr = "INSERT INTO t_cps_devote (t_cps_id, t_user_id, t_devote_value, "
				+ "t_create_time, t_ratio, t_recharge_money,t_channel_level, t_source_id,"
				+ "t_cps_status,t_cps_parent_id) VALUES (?, ?, ?, "
				+ "?, ?, ?, ?, ?,"
				+ " ?, ?);";
		iCommService.executeSQL(inStr, channelId, userId, devoteValue,
				DateUtils.format(new Date(), DateUtils.FullDatePattern), ratio, storeMoney, channelLevel,sourceId,
				t_cps_status,t_cps_parent_id);
	}

	/**
	 * 渠道日汇总明细
	 * @param userId
	 * @param channelName
	 * @param parentId
	 * @param vipIncome
	 * @param goldIncome
	 * @param vipIncomeTwo
	 * @param goldIncomeTwo
	 */
	// 写入到日汇总中
	private void dayChannelSummary(int channelId,int t_settlement_method,
			int userId,String channelName,int parentId,
			BigDecimal vipIncome,BigDecimal goldIncome,BigDecimal vipIncomeTwo,BigDecimal goldIncomeTwo) {
		
		String qSql = "SELECT t_id FROM t_cps_day_income WHERE t_day = ? AND t_uid = ? ";
		int t_is_release=0;
		if(t_settlement_method==0) {
				//CPS
			t_is_release=1;
		}
		
		List<Map<String,Object>> sqlList = iCommService.getQuerySqlList(qSql, DateUtils.format(new Date(), DateUtils.defaultDatePattern),userId);
		//新增
		if(null ==  sqlList || sqlList.isEmpty()) {
			
			String  inStr = "INSERT INTO t_cps_day_income (t_uid, t_channel_name, t_parent_id, "
					+ "t_install_amount, t_vip_income, t_gold_income, t_vip_income_two, t_gold_income_two, "
					+ "t_day, t_settle_accounts,t_cps_id,t_is_release) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
			iCommService.executeSQL(inStr, userId,channelName,parentId,0,vipIncome,goldIncome,
					vipIncomeTwo,goldIncomeTwo,DateUtils.format(new Date(), DateUtils.defaultDatePattern),0,channelId,t_is_release);
			
		} else { 
			//修改
			
			String uSql ="UPDATE t_cps_day_income SET  "
					+ "t_vip_income= t_vip_income+?,"
					+ " t_gold_income=t_gold_income+?, "
					+ "t_vip_income_two=t_vip_income_two+?, "
					+ "t_gold_income_two=t_gold_income_two+?,"
					+ "t_settle_accounts=0 WHERE t_id=?;";
			
			iCommService.executeSQL(uSql, vipIncome,goldIncome,vipIncomeTwo,goldIncomeTwo,sqlList.get(0).get("t_id"));
		}
	
	}
	/**
	 * parentId cps渠道
	 * user_Android 安卓用户
	 * user_iPhone 苹果用户
	 * */
	public static void channelRegister(int parentId,int user_Android,int user_iPhone) {
		//获取推广人是否是CPS代理
		List<Map<String, Object>> sqlList2 = iCommService.getQuerySqlList(
				"SELECT cp.t_id,cp.t_uid,cp.t_parent_id ,c.t_settlement_method  FROM t_cps_user cp left join t_cps c on cp.t_id=c.t_cps_id"
				+ " WHERE cp.t_start_using = 0 AND cp.t_uid = ? ;",parentId);
		if(null == sqlList2 || sqlList2.isEmpty()) {
			return ;
		}
		
		//
		int t_is_release=0;
		if(sqlList2.get(0).get("t_settlement_method").toString().equals("0")) {
//			cps渠道 自动发布
			t_is_release=1;
		}
		
		String qSql = "SELECT t_id FROM t_cps_day_income WHERE t_day = ? AND t_uid = ? ";
		
		 List<Map<String, Object>> sqlList = iCommService.getQuerySqlList(qSql, DateUtils.format(new Date(), DateUtils.defaultDatePattern),parentId);
		
		if(null == sqlList || sqlList.isEmpty()) {
			
			Map<String, Object> map = iCommService.getMap("SELECT cu.t_id,c.t_real_name,c.t_channel_name,cu.t_parent_id FROM t_cps_user cu LEFT JOIN t_cps c ON c.t_cps_id = cu.t_id WHERE cu.t_uid = ?", parentId);
			
            String  inStr = "INSERT INTO t_cps_day_income (t_uid, t_channel_name, t_parent_id, t_install_amount,t_user_android,"
            		+ "t_user_iphone, t_vip_income, t_gold_income, t_vip_income_two, t_gold_income_two, t_day, t_settle_accounts,"
            		+ "t_is_release,t_cps_id) VALUES (?, ?, "
            		+ "?, ?, ?, ?,?, ?,  ?, ?,"
            		+ " ?, ?,?,?);";
			iCommService.executeSQL(inStr, parentId,map.get("t_channel_name"),
					map.get("t_parent_id"),1,user_Android, user_iPhone,0,0,0,0,DateUtils.format(new Date(), 
							DateUtils.defaultDatePattern),0,t_is_release,map.get("t_id"));
			//判断当前渠道是否为二级渠道
			if(!map.get("t_parent_id").toString().equals("0")) {
				Map<String, Object> map2 = iCommService.getMap("SELECT cu.t_uid FROM t_cps_user cu"
						+ "   WHERE cu.t_id = ?", map.get("t_parent_id"));
				channelOneRegister( Integer.parseInt(map2.get("t_uid").toString()), t_is_release, user_Android, user_iPhone);
			}
			
		}else {
			String uSql = "UPDATE t_cps_day_income SET t_install_amount = t_install_amount + 1,"
					+ "t_user_android = t_user_android + ?,"
					+ "t_user_iphone = t_user_iphone + ?"
					+ " WHERE t_id = ?";
			iCommService.executeSQL(uSql,user_Android, user_iPhone, sqlList.get(0).get("t_id"));
			
			//判断当前渠道是否为二级渠道
			if(!sqlList2.get(0).get("t_parent_id").toString().equals("0")) {
				Map<String, Object> map2 = iCommService.getMap("SELECT cu.t_uid FROM t_cps_user cu"
						+ "   WHERE cu.t_id = ?", sqlList2.get(0).get("t_parent_id"));
				channelOneRegister( Integer.parseInt(map2.get("t_uid").toString()), t_is_release, user_Android, user_iPhone);
			}
		}
	}
	public static void channelOneRegister(int t_uid,int t_is_release,int user_Android,int user_iPhone) {
		
		String qSql = "SELECT t_id FROM t_cps_day_income WHERE t_day = ? AND t_uid = ? ";
		
		List<Map<String, Object>> sqlList  = iCommService.getQuerySqlList(qSql, DateUtils.format(new Date(), DateUtils.defaultDatePattern),t_uid);
		
		if(null == sqlList || sqlList.isEmpty()) {
			
			Map<String, Object> map = iCommService.getMap("SELECT cu.t_id,c.t_real_name,c.t_channel_name,cu.t_parent_id "
					+ "FROM t_cps_user cu LEFT JOIN t_cps c ON c.t_cps_id = cu.t_id WHERE cu.t_uid = ?", t_uid);
			
            String  inStr = "INSERT INTO t_cps_day_income (t_uid, t_channel_name, t_parent_id, t_install_amount,t_user_android,"
            		+ "t_user_iphone, t_vip_income, t_gold_income, t_vip_income_two, t_gold_income_two, t_day, t_settle_accounts,"
            		+ "t_is_release,t_cps_id) VALUES (?, ?, "
            		+ "?, ?, ?, ?,?, ?,  ?, ?,"
            		+ " ?, ?,?,?);";
			iCommService.executeSQL(inStr, t_uid,map.get("t_channel_name"),
					map.get("t_parent_id"),1,user_Android, user_iPhone,0,0,0,0,DateUtils.format(new Date(), 
							DateUtils.defaultDatePattern),0,t_is_release,map.get("t_id"));
		}else {
			String uSql = "UPDATE t_cps_day_income SET t_install_amount = t_install_amount + 1,"
					+ "t_user_android = t_user_android + ?,"
					+ "t_user_iphone = t_user_iphone + ?"
					+ " WHERE t_id = ?";
			iCommService.executeSQL(uSql,user_Android, user_iPhone, sqlList.get(0).get("t_id"));
		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		   new ChannelSettlementThread(377,
           		new BigDecimal("6"),
           		Integer.valueOf(1),
           		Integer.valueOf(44)).start();;	
	}
}
