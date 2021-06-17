package com.yiliao.util;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yiliao.service.ICommService;

public class AcThread extends Thread {

	Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * 查询类型
	 */
	private int query_type;
	/**
	 * 查询sql
	 */
	private String query_sql;
	/**
	 * 查询条件
	 */
	private Object[] obj;
	
	/**
	 * 查询结果 
	 */
	private Map<String, Object> data_map;
	
	/**
	 * 查询结果
	 */
	private List<Map<String, Object>> data_list;

	public AcThread(int query_type,Map<String, Object> data_map,String query_sql,Object... objects) {
		this.query_type = query_type;
		this.query_sql = query_sql;
		this.obj = objects;
		this.data_map = data_map;
	}
	
	public AcThread(int query_type,List<Map<String, Object>> data_list,String query_sql,Object... objects) {
		this.query_type = query_type;
		this.query_sql = query_sql;
		this.obj = objects;
		this.data_list = data_list;
	}

	// 获取 videoChatService
	private static ICommService iCommServiceImpl = null;

	static {
		iCommServiceImpl = (ICommService) SpringConfig.getInstance().getBean("iCommServiceImpl");
	}

	@Override
	public void run() {
		switch (query_type) {
		case 1:
			 data_map.putAll(iCommServiceImpl.getMap(query_sql, obj));
			break;
		case 2:
			data_list.addAll(iCommServiceImpl.getQuerySqlList(query_sql, obj));
			break;
		}
	}

}
