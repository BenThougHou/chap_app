package com.yiliao.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yiliao.service.BlackUserService;
import com.yiliao.service.impl.ICommServiceImpl;
import com.yiliao.util.MessageUtil;
import com.yiliao.util.cache.Cache;
import com.yiliao.util.cache.CacheRemove;



/**
 * 黑名单
 *
 */
@Service
public class BlackUserServiceImpl extends ICommServiceImpl implements BlackUserService {

	@Override
	@CacheRemove(cacheKey = "#APP_BLACK_USER_LIST_&userId#")
	public MessageUtil addBlackUser(String userId,String t_cover_user_id) {
		MessageUtil mu = null;
		String sql="select count(1) count from t_black_user where t_user_id=? and t_cover_user_id=?";
		Map<String, Object> map = this.getMap(sql, userId,t_cover_user_id);
		if(map!=null&&Integer.parseInt(map.get("count").toString())>0) {
			mu=new MessageUtil(0,"请勿重复添加黑名单!");
			return mu;
		}
		sql="insert into t_black_user (t_user_id,t_cover_user_id,t_create_time) values (?,?,now())";
		this.executeSQL(sql, userId,t_cover_user_id);
		mu=new MessageUtil(1,"新增成功");
		return mu;
	}

	@Override
	@CacheRemove(cacheKey = "#APP_BLACK_USER_LIST_&userId#")
	public MessageUtil delBlackUser(String userId,String t_id) {
		MessageUtil mu = null;
		String sql="delete from t_black_user where t_id=?";
		this.executeSQL(sql, t_id);
		mu=new MessageUtil(1,"删除成功");
		return mu;
	}

	@Override
	@Cache(cacheKey="#APP_BLACK_USER_LIST_&userId#",setArguments = false,expire = 12*60L)
	public MessageUtil getBlackUserList(String userId,int page,int size) {
		MessageUtil mu = null;
		String sql="select "
				+ " b.t_id,"
				+ " b.t_cover_user_id,"
				+ " u.t_sex,"
				+ " u.t_role,"
				+ "ifnull(u.t_age,18) t_age," 
				+ "u.t_handImg,"
				+ "u.t_nickName,"
				+ "unix_timestamp(b.t_create_time) AS t_create_time "
				+ "from t_black_user b"
				+ " left join t_user u on u.t_id=b.t_cover_user_id"
				+ " where b.t_user_id=? order by b.t_create_time desc limit ?,?";
		List<Map<String,Object>> querySqlList = this.getQuerySqlList(sql, userId,(page-1)*size,size);
		mu=new MessageUtil(1,"查询成功");
		mu.setM_object(querySqlList);
		return mu;
	}
	
	
	
}
