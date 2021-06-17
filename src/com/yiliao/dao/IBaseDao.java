package com.yiliao.dao;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

/**
 * 数据访问接口
 * @author 邓勇
 *  2012-01-15
 * 
 */
public interface IBaseDao {

	public Session getSessions();
	
	
	
	/**
	 * jdbc预编译执行sql
	 * @param sql
	 * @return
	 * author 邓勇
	 * time 2012-01-05
	 */
	boolean executeConSQL(final String pstrsql);
	
	/**
	 * 
	 * 执行hql语句，返回影响记录行数
	 * @param hql hql语句
	 * @param params 参数列表
	 * @return 影响行数
	 */
	 int executeHQL(final Object hql, final Object... params);
	
	
	/**
	 * hql分页查找
	 * @param <T>
	 * @param hql sql语句
	 * @param pageSize 页容量
	 * @param pageNo 第几页
	 * @return 结果列表
	 */
	 <T> List<T> findByPageHQL(final Object hql,final int pageNo, final int pageSize,
			final Object... params);
	
	/**
	 * 
	 * 执行hql查询语句，返回结果列表
	 * @param <T> 结果实体类型
	 * @param hql hql语句
	 * @param params 参数列表
	 * @return 结果列表
	 */
	 <T> List<T> findByHQL(final Object hql,
			final Object... params);

	
	

	/**
	 * 
	 * 通过实体主键值获取实体对象
	 * @param <T> 实体类型
	 * @param type 实体类对象
	 * @param id 主键值
	 * @return 实体对象
	 */
	 <T> T findByID(Class<T> type, Serializable id);

	/**
	 * <p>sql分页查找,返回数据为List结构，每个List元素为T。</p>
	 * <p>
	 * <ul>
	 * <dd><b>数据库字段类型对应JAVA类型：</b></dd>
	 * <dl>数字类型对象为java.math.BigDecimal</dl>
	 * <dl>日期时间类型为java.sql.Timestamp</dl>
	 * <dl>字符串类型对象为java.lang.String</dl>
	 * </ul>
	 *  <p> 
	 * @param <T> 当sql语句仅返回一列时，T为Object，当返回多列时，T为Object数组
	 * @param sql sql语句
	 * @param pageSize 每页最大记录数
	 * @param pageNo 第几页
	 * @return List
	 */
	 <T> List<T> findByPageSQL(final Object sql, final int pageSize,
			final int pageNo, final Object... params);
	
	/**
	 * <p>执行查询SQL语句,返回数据为List结构，每个List元素为T。</p>
	 * <p>
	 * <ul>
	 * <dd><b>数据库字段类型对应JAVA类型：</b></dd>
	 * <dl>数字类型对象为java.math.BigDecimal</dl>
	 * <dl>日期时间类型为java.sql.Timestamp</dl>
	 * <dl>字符串类型对象为java.lang.String</dl>
	 * </ul>
	 *  <p> 
	 * @param T 当sql语句仅返回一列时，T为Object，当返回多列时，T为Object数组
	 * @param sql 执行sql语句
	 * @param params 参数列表
	 * @return 查询结果
	 */
	 <T> List<T> findBySQL(final Object sql,
			final Object... params);
	
		/**
		 *  
		 * 持久化对象，返回对象主键值
		 * @param <T> 对象主键类型
		 * @param o 对象实体
		 * @return 主键值
		 */
	public <T>T save(Object pmodel);
	

	/**
	 * 通过对象实体删除对象
	 * @param t 需删除的对象
	 */
	 void delete(Object... t);
	 /**
	 * 清除对象
	 */
	 void clear();

	/**
	 * 
	 * 执行hql语句，返回影响记录行数
	 * @param sql sql语句
	 * @param params 参数列表
	 * @return 影响行数
	 */
	 int executeSQL(final Object sql, final Object... params);

	/**
	 * 执行hql查询语句，返回唯一结果
	 * @param <T> 返回结果类型
	 * @param hql hql语句
	 * @param params 参数列表
	 * @return 结果对象
	 */
	 <T> T findByHQLUniqueResult(final Object hql,
			final Object... params);
	 
	/**
	 * 
	 * 执行sql查询语句，返回唯一结果
	 * @param <T> 结果对象类型
	 * @param sql sql语句
	 * @param params 参数列表
	 * @return 结果对象
	 */
	 <T> T findBySQLUniqueResult(final Object sql,
			final Object... params);

	/**
	 * 
	 * 更新持久化对象
	 * @param t 对象
	 */
	 void update(Object t);
	
}
