package com.yiliao.dao;

import java.util.List;
import java.util.Map;

import com.yiliao.util.PageInfo;


public interface IEntitySQLDAO{
	/**
	 * JDBC分页查询
	 * @param clazz  返回结果的类型
	 * @param pageInfo	分页对象
	 * @param sql	sql查询命令
	 * @return
	 */
	<T> PageInfo<T> queryPagingToObject(Class<T> clazz, String sql, int pageSize, int pageNo, Object ... objs);
	/**
	 * JDBC分页查询
	 * @param clazz  返回结果的类型
	 * @param pageInfo	分页对象
	 * @param sql	sql查询命令
	 * @return
	 */
	<T> PageInfo<T> queryPagingToObject(Class<T> clazz, PageInfo<T> pageInfo, String sql, Object ... objs);
	
	// -------------------------------------------------------------------------------------
	
	/**
	 * jdbc预编译执行sql
	 * @param sql
	 * @return
	 * author 邓勇
	 * time 2012-01-05
	 */
	boolean executeConSQL(final String pstrsql);
	
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
	<T> List<T> findByPageSQL(final String sql, final int pageSize,
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
	 <T> List<T> findBySQL(final String sql,
			final Object... params);
	 
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
	 * @return 返回键值对
	 */
	 List<Map<String, Object>> findBySQLTOMap(final String sql,
				final Object... params);
	 
	 /**
	 * 执行sql语句，返回影响记录行数
	 * @param sql sql语句
	 * @param params 参数列表
	 * @return 影响行数
	 */
	 int executeSQL(final String queryString, final Object... params);
	 
	 /**
	 * 
	 * 执行sql查询语句，返回唯一结果
	 * @param <T> 结果对象类型
	 * @param sql sql语句
	 * @param params 参数列表
	 * @return 结果对象
	 */
	 <T> T findBySQLUniqueResult(final String queryString,
			final Object... params);
	 
	 /**
	 * 
	 * 执行sql查询语句，返回唯一结果
	 * @param <T> 结果对象类型
	 * @param sql sql语句
	 * @param params 参数列表
	 * @return 返回键值对
	 */
	 Map<String, Object> findBySQLUniqueResultToMap(final String queryString,
				final Object... params);
	 
	 /**
	  * 插入数据并返回主键
	  * @param insertString
	  * @param objects
	  * @return
	  */
//	 Integer saveData(final String insertString, final Object ...objects);
	 
	 
	 
	 /**
	   * 插入数据返回主键
	  * @param queryString
	  * @param params
	  * @return
	  */
	 int saveData(final String queryString, final Object... params);
}
