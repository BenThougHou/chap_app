package com.yiliao.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.yiliao.exception.BusinessException;

/**
 * 公共的业务接口
 * @datetime 2013.5.15
 */
@Service
public interface ICommService {

	public Serializable add(Object obj) throws BusinessException;
	
	public void update(Object obj);
	
	
	List<Map<String,Object>> getQuerySqlList(String sql, final Object ... objects);
	
	Map<String, Object> getMap(String sql,final Object ... objects);
	
	int executeSQL(String sql,final Object ... objects);
}
