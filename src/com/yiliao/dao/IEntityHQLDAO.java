package com.yiliao.dao;

import java.io.Serializable;
import java.util.List;

import com.yiliao.dao.util.SelectParam;

/**
 * 针对单个实体的持久化
 * @author ZhouShuhua 20121121
 * @param
 */
public interface IEntityHQLDAO {
	
	/**
	 * 保存对象
	 * @param obj
	 * @time 2013.4.2
	 */
	public Serializable save(Object obj);
	
	/**
	 * 更新对象
	 * @param obj
	 * @time 2013.4.2
	 */
	public void update(Object obj);
	
	/**
	 * 保存或是更新
	 * @param obj
	 */
	public void saveOrUpdate(Object obj);
	
	/**
	 * 删除对象
	 * @param obj
	 * @time 2013.4.2
	 */
	public void delete(Object obj);
	
	/**
	 * 根据主键删除对象
	 * @param ser
	 * @time 2013.4.2
	 */
	public <T> void delete(Class<T> entityClass, Serializable id);
	
	/**
	 * 删除多个对象
	 * @param t
	 */
	void delete(Object... t);
	
	/**
	 * 根据主键查询某个对象
	 * @param entityClass
	 * @param id
	 * @return
	 */
	public <T> T get(Class<T> entityClass, Serializable id);
	
	/**
	 * 根据检索命令返回集合
	 * @param queryString
	 * @param obj
	 * @return
	 */
	<T> List<T> findByQueryString(String queryString, Object ...obj);
	
	/**
	 * 返回唯一对象
	 * @param queryString
	 * @param objs
	 * @return
	 */
	<T> T findByQueryStringUniqueResult(String queryString, final Object ... objs);
	
	/**
	 * 查询所有对象
	 * @param c		查询对象
	 * @param obj	动态条件
	 * @return
	 */
	<T> List<T> findAll(Class<T> c, final SelectParam ... selectParams);
	
	/**
	 * 
	 * 执行hql语句，返回影响记录行数
	 * @param hql hql语句
	 * @param params 参数列表
	 * @return 影响行数
	 */
	int executeHQL(final String queryString, final Object ... params);
	
	/**
	 * hql分页查找
	 * @param <T>
	 * @param hql sql语句
	 * @param pageSize 页容量
	 * @param pageNo 第几页
	 * @return 结果列表
	 */
	 <T> List<T> findByPageHQL(final String queryString, final int pageNo, final int pageSize, final Object ... params);
	 
	 /**
	 * 
	 * 执行hql查询语句，返回结果列表
	 * @param <T> 结果实体类型
	 * @param hql hql语句
	 * @param params 参数列表
	 * @return 结果列表
	 */
	 <T> List<T> findByHQL(final String queryString, final Object ... params);
	 
	 /**
	 * 
	 * 通过实体主键值获取实体对象
	 * @param <T> 实体类型
	 * @param type 实体类对象
	 * @param id 主键值
	 * @return 实体对象
	 */
	 <T> T findByID(Class<T> clazz, Serializable id);
	 
	 /**
	 * 执行hql查询语句，返回唯一结果
	 * @param <T> 返回结果类型
	 * @param hql hql语句
	 * @param params 参数列表
	 * @return 结果对象
	 */
	 <T> T findByHQLUniqueResult(final String queryString, final Object... params);
}
