package com.yiliao.dao.impl;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.yiliao.dao.IEntityHQLDAO;
import com.yiliao.dao.core.BaseHibernateTemplate;
import com.yiliao.dao.util.SelectParam;

/**
 * 针对单个实体的持久化实现
 * @author zhoushuhua
 *
 */
@Repository("iEntityHQLDAOImpl")
public class IEntityHQLDAOImpl extends BaseHibernateTemplate implements IEntityHQLDAO {
	
	public IEntityHQLDAOImpl() {
		logger.info("正在加载......");
	} 
	
	/**
	 * 根据主键标示删除对象
	 * @param entityClass
	 * @param id
	 */
	@Override
	public <T> void delete(Class<T> entityClass, Serializable id) {
		this.delete(this.get(entityClass, id));
	}
	
	/**
	 * 查询某个对象的所有信息
	 */
	@Override
	public <T> List<T> findAll(Class<T> c, final SelectParam ... selectParams) {
		
		// 检索命令
		StringBuilder queryString = new StringBuilder("from ").append(c.getName());
		// 对检索命令设置占位符
		Object[] obj = this.setPlaceholderToQueryString(queryString, selectParams);
		// 返回结果
		return findByQueryString(queryString.toString(), obj);
	}

	/**
	 * 根据检索命令返回结果
	 */
	@Override
	public <T> List<T> findByQueryString(String queryString, final Object... objs) {
		
		return this.executeQuery(queryString, new BaseHibernateTemplate.IQueryAdapter<List<T>>() {

			@SuppressWarnings("unchecked")
			@Override
			public List<T> doInHibernateImpl(Query query) {
				
				return this.setDynamicParams(query, objs).list();
			}
		});
	}
	
	/**
	 * 返回唯一值
	 * @param queryString
	 * @param objs
	 * @return
	 */
	public <T> T findByQueryStringUniqueResult(String queryString, final Object ... objs) {
		
		return this.executeQuery(queryString, new BaseHibernateTemplate.IQueryAdapter<T>() {

			@SuppressWarnings("unchecked")
			@Override
			public T doInHibernateImpl(Query query) {
				
				return (T) this.setDynamicParams(query, objs).uniqueResult();
			}
		});		
	}

	/**
	 * 删除多个对象
	 */
	@Override
	public void delete(Object ... objs) {
		for(Object obj : objs) {
			this.delete(obj);
		}
	}

	/**
	 * Hql 更新命令
	 */
	@Override
	public int executeHQL(String queryString, Object... params) {
		return this.executeQuery(queryString, new IQueryAdapter<Integer>() {
			@Override
			public Integer doInHibernateImpl(Query query){
				
				return query.executeUpdate();
			}
		});
	}

	/**
	 * 分页查询
	 */
	@Override
	public <T> List<T> findByPageHQL(String queryString, final int pageNo,
			final int pageSize, final Object... params) {
		return this.executeQuery(queryString, new IQueryAdapter<List<T>>() {
			@Override
			public List<T> doInHibernateImpl(Query query){
				
				return this.setDynamicParams(query, params).
						setFirstResult((pageNo-1)*pageSize).
						setMaxResults(pageSize).list();
			}
		});
	}

	/**
	 * HQL查询
	 */
	@Override
	public <T> List<T> findByHQL(final String queryString, final Object... params) {
		return this.executeQuery(queryString, new IQueryAdapter<List<T>>() {
			@Override
			public List<T> doInHibernateImpl(Query query){
				
				return this.setDynamicParams(query, params).list();
			}
		});
	}

	/**
	 * 根据对象标示符得到对象
	 */
	@Override
	public <T> T findByID(Class<T> clazz, Serializable id) {
		return this.get(clazz, id);
	}

	@Override
	public <T> T findByHQLUniqueResult(final String queryString, Object... params) {
		return this.findByQueryStringUniqueResult(queryString, params);
	}
}
