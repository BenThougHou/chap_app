package com.yiliao.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yiliao.service.CpsService;
import com.yiliao.util.DateUtils;
import com.yiliao.util.MessageUtil;

@Service("cpsService")
public class CpsServiceImpl extends ICommServiceImpl implements CpsService {

	
	@Override
	public MessageUtil addCpsMs(int user_id,String cpsName, String cpsUrl, int active,
			int proportions, String realName, int takeOutId,
			String accountNumber, String phone) {
		MessageUtil mu = null;
		try {
			
			String inSql = " INSERT INTO t_cps (t_user_id, t_real_name, t_settlement_type, t_bank, t_phone, t_cps_name, t_cps, t_active_user, t_proportions, t_audit_status, t_create_time) VALUES (?,?,?,?,?,?,?,?,?,?,?);";
			
			this.getFinalDao().getIEntitySQLDAO().executeSQL(inSql, user_id,
					realName,takeOutId,accountNumber,phone,cpsName,cpsUrl,active,proportions,0,DateUtils.format(new Date(), DateUtils.FullDatePattern));
			
			mu = new MessageUtil(1, "已申请,等待审核.");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("申请成为CPS联盟主异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}
	/**
	 * 获取贡献列表
	 */
	@Override
	public MessageUtil getContributionList(int userId, int page) {
		MessageUtil mu = null;
		try {
			
			//获取总记录数
			String cSql = " SELECT COUNT(t_id) AS total FROM t_cps_devote WHERE t_cps_id =(SELECT t_id FROM t_cps WHERE t_user_id = ?) GROUP BY t_user_id ";
			List<Map<String, Object>> findBySQLTOMap = this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(cSql, userId);
			
			int total=0;
			
			if(!findBySQLTOMap.isEmpty()){
				total = Integer.parseInt(findBySQLTOMap.get(0).get("total").toString());
			}
			
			int pageCount = total%10==0?total/10:total/10+1;
			//获取贡献数据
			String qSql = "SELECT u.t_nickName,u.t_handImg,u.t_phone,SUM(d.t_recharge_money) AS recharge_money ,SUM(d.t_devote_value) AS t_devote_value,d.t_ratio FROM t_cps_devote d LEFT JOIN t_user u ON d.t_user_id=u.t_id WHERE d.t_cps_id = (SELECT t_id FROM t_cps WHERE t_user_id = ?) GROUP BY u.t_id LIMIT ?,10";
			List<Map<String, Object>>  dataMap= this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(qSql, userId,(page -1)*10);
			
			for(Map<String, Object> m : dataMap){
				if(null == m.get("t_nickName")){
					m.put("t_nickName", "聊友:"+m.get("t_phone").toString().substring(m.get("t_phone").toString().length()-4));
				}
				m.remove("t_phone");
			}
	 
			mu = new MessageUtil(1,new HashMap<String,Object>(){{
				put("pageCount", pageCount);
				put("data", dataMap);
			}});
		 
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取贡献列表异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}
	@Override
	public MessageUtil getTotalDateil(int userId) {
		MessageUtil mu = null;
		try {
			
			StringBuffer sql = new StringBuffer();
			sql.append("SELECT u.t_handImg,c.t_real_name,u.t_nickName,u.t_phone,u.t_sex,u.t_is_vip,c.t_phone AS realPhone,c.t_id,SUM(d.t_devote_value) AS totalMoney ");
			sql.append("FROM t_cps c LEFT JOIN t_user u ON c.t_user_id = u.t_id LEFT JOIN t_cps_devote d ON d.t_cps_id = c.t_id ");
			sql.append(" WHERE c.t_user_id = ? ");
 
			Map<String, Object> cpsMap = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql.toString(), userId);
			
			if(!cpsMap.isEmpty()){
				//
				if(null == cpsMap.get("t_nickName")){
					cpsMap.put("t_nickName", "聊友:"+cpsMap.get("t_phone").toString().substring(cpsMap.get("t_phone").toString().length()-4));
					cpsMap.remove("t_phone");
				}
				
				if(null == cpsMap.get("totalMoney")){
					cpsMap.put("totalMoney", 0);
				}
				//获取已结算金额
				Map<String, Object> setMap = this.getMap("SELECT SUM(s.t_settlement_amount) AS setMoney FROM t_cps_settlement s WHERE s.t_cps_id = ? ", cpsMap.get("t_id"));
				if(null == setMap.get("setMoney")){
					cpsMap.put("setMoney", 0);
				}else {
					cpsMap.put("setMoney", setMap.get("setMoney"));
				}
				
				//统计贡献人数
				String qSql = "SELECT count(t_id) AS totalUser  FROM t_user WHERE t_referee = ? ;";
				Map<String, Object> totalMap = this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(qSql, userId);
				cpsMap.put("totalUser", totalMap.get("totalUser"));
			}
			
			mu = new MessageUtil();
			mu.setM_istatus(1);
			mu.setM_object(cpsMap);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取cps统计异常!", e);
			mu = new MessageUtil(0, "程序异常!");
		}
		return mu;
	}

}
