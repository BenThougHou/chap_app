package com.yiliao.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.yiliao.dao.core.FinalDao;
import com.yiliao.exception.BusinessException;
import com.yiliao.service.ICommService;

/**
 * 公共业务接口实现类
 * 
 */
@Service("iCommServiceImpl")
public class ICommServiceImpl implements ICommService, ApplicationContextAware {

	private FinalDao finalDao;

	public String m_strResult;

	@Autowired
	private DataSourceTransactionManager txManager;

	
	ExecutorService pool = Executors.newFixedThreadPool(5);
	
	public ApplicationContext applicationContext;

	public DataSourceTransactionManager getTxManager() {
		return txManager;
	}

	public String getM_strResult() {
		return m_strResult;
	}

	public void setM_strResult(String mStrResult) {
		m_strResult = mStrResult;
	}

	protected Logger logger = LoggerFactory.getLogger(getClass());

	// setter/gette
	public FinalDao getFinalDao() {
		return finalDao;
	}

	public void setFinalDao(FinalDao finalDao) {
		this.finalDao = finalDao;
	}

	/**
	 * 保存对象
	 */
	@Override
	public Serializable add(Object obj) throws BusinessException {

		logger.debug("添加对象" + obj.getClass().getName());
		// 添加
		return this.finalDao.getIEntityHQLDAO().save(obj);
	}

	public void update(Object obj) {

		logger.debug("更新对象" + obj.getClass().getName());

		this.finalDao.getIEntityHQLDAO().update(obj);
		;

	}

	/***
	 * 查询列表
	 * 
	 * @param sql
	 * @param objects
	 * @return
	 */
	@Override
	public List<Map<String, Object>> getQuerySqlList(String sql, final Object... objects) {
		return this.getFinalDao().getIEntitySQLDAO().findBySQLTOMap(sql, objects);
	}

	/**
	 * 单个查询
	 * 
	 * @param sql
	 * @param objects
	 * @return
	 */
	@Override
	public Map<String, Object> getMap(String sql, final Object... objects) {
		return this.getFinalDao().getIEntitySQLDAO().findBySQLUniqueResultToMap(sql, objects);
	}

	/**
	 * 执行 新增或者修改或者删除代码
	 * 
	 * @param sql
	 * @param objects
	 * @return
	 */
	public int executeSQL(String sql, final Object... objects) {
		TransactionStatus status = getStatus();
		try {
			 int executeSQL = this.getFinalDao().getIEntitySQLDAO().executeSQL(sql, objects);
			 this.getTxManager().commit(status);
			 return executeSQL;
		} catch (Exception e) {
			e.printStackTrace();
			getTxManager().rollback(status);
		}
		return 0;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	// 创建事物且获取事物状态
	public TransactionStatus getStatus() {
		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);// 事物隔离级别，开启新事务
		return this.getTxManager().getTransaction(def);
	}

}
