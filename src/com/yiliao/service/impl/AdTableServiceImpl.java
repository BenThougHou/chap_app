package com.yiliao.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

import com.yiliao.service.AdTableService;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.cache.Cache;

@Service
public class AdTableServiceImpl extends ICommServiceImpl implements AdTableService {

	@Override
	public MessageUtil getAdTable(int type, int page, int size,int userId) {
		try {
			size=100;
			StringBuffer sql = new StringBuffer();
			sql.append("select "
					+ "t_ad_table_name,"
					+ "t_ad_table_content,"
					+ "t_ad_table_type,t_ad_table_url,t_ad_table_target "
					+ "	from t_ad_table where t_is_use=1 and t_ad_table_type=? ");
			switch(type) {
			//'1.开机图 2:主页table 3:主页公告 4:直播跑马灯 5:直播广告',
			case 1:
				sql.append("  order by  t_ad_table_sort  limit 1 ");
				break;
			case 2:
				if(userId!=0) {
					String qsql="select t_role from t_user where t_id=? ";
					List<Map<String,Object>> roleInfo = this.getQuerySqlList(qsql, userId);
					if(null!=roleInfo&&!roleInfo.isEmpty()) {
						switch (roleInfo.get(0).get("t_role").toString()) {
						case "0":
							//普通用户
							sql.append(" and t_ad_table_target!=4 ");
							break;
						case "1":
							//主播
							sql.append(" and t_ad_table_target!=5  and t_ad_table_target!=8  ");
							break;
						case "2":
							//普通CPS用户 
							sql.append(" and t_ad_table_target!=4 ");
							break;
						}
					}
				}
				sql.append("  order by  t_ad_table_sort  limit "+(page-1)*size+","+size+" ");
				break;
			case 3:
				sql.append("  order by  t_ad_table_sort  limit "+(page-1)*size+","+size+" ");
				break;
			case 4:
				sql.append("  order by  t_ad_table_sort  limit "+(page-1)*size+","+size+" ");
				break;
			case 5:
				sql.append("  order by  t_ad_table_sort  limit 2 ");
				break;
			}
			List<Map<String,Object>> querySqlList = this.getQuerySqlList(sql.toString(),type);
			MessageUtil mu = new MessageUtil(1,"查询成功");
			mu.setM_object(querySqlList);
			return mu;
		} catch (Exception e) {
			e.printStackTrace();
			return new MessageUtil(0,"服务器繁忙");
		}
		
	}

}
