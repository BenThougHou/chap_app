package com.yiliao.dao.core;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import com.yiliao.dao.util.ArrayRowMapper;
import com.yiliao.dao.util.ObjectRowMapper;
import com.yiliao.util.PageInfo;

/**
 * jdbc操作
 * @author ZhouShuhua
 *
 */
public class BaseJdbcTemplate extends JdbcTemplate {
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private String dbType;
	
	
	/** ORACLE 分页查询模版 */
	private static final String ORACLE_PAGESQL_TEMPLATE = "SELECT * FROM (SELECT XX.*,ROWNUM ROW_NUM FROM (${sql}) XX) ZZ where ZZ.ROW_NUM BETWEEN ${startNum} AND ${endNum}";
	
	/**
	 * SQLSERVER 分页查询模板
	 */
	private static final String SQLSERVER_PAGESQL_TEMPLATE = "SELECT * FROM (SELECT TOP(${endNum}) ROW_NUMBER() OVER(Order by isNull(1,1)) as NUMS,* FROM (${sql}) TB) TB WHERE NUMS > ${startNum}";
	
	/**
	 * 注入数据库类型
	 * @param dbType
	 */
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	/**
	 * JDBC分页查询
	 * @param clazz  返回结果的类型
	 * @param pageInfo	分页对象
	 * @param sql	sql查询命令
	 * @return
	 */
//	@SuppressWarnings("unchecked")
//	public <T> PageInfo<T> queryPagingToObject(Class<T> clazz, PageInfo<T> pageInfo, String sql, Object ... objs) {
//		try {
//			sql = sql.toUpperCase();
//			logger.info(sql.replaceFirst("(SELECT).*?(FROM)", "$1 COUNT(*) $2"));
//			// 总记录数
//			Long totalCount = super.queryForLong(sql.replaceFirst("(SELECT).*?(FROM)", "$1 COUNT(*) $2"), objs);
//			if(totalCount != null && totalCount != 0) {
//				// 分页起始位置
//				int startNum = pageInfo.getPagesize()
//						* (pageInfo.getPagenum()-1);
//				// 分页结束位置
//				int endNum = startNum + pageInfo.getPagesize();
//				
//				// 分页起始位置+1
//				startNum = startNum + 1;
//				
//				// 判断结束位置是否是大于总的条数
//				if (endNum > totalCount) {
//					endNum = Integer.valueOf(totalCount.toString());
//				}
//				
//				// 默认使用的是ORACLE数据库
//				String pageSql = ORACLE_PAGESQL_TEMPLATE;
//				// 判断是否是使用的SQLSERVER数据库
//				if("SQLSERVER".equals(dbType)) {
//					pageSql = SQLSERVER_PAGESQL_TEMPLATE;
//				}
//				
//				// 替换
//				pageSql = StringUtils.replace(pageSql, "${sql}", sql);
//				pageSql = StringUtils.replace(pageSql, "${startNum}", String
//						.valueOf(startNum));
//				pageSql = StringUtils.replace(pageSql, "${endNum}", String
//						.valueOf(endNum));
//				
//				List<T> result = null;
//				
//				// 判断是否是需要返回Map对象
//				if("Map".equals(clazz.getSimpleName())) {
//					result = (List<T>) query(pageSql, objs, new ColumnMapRowMapper());
//				} else if("Array".equals(clazz.getSimpleName())) {
//					result = (List<T>) query(pageSql, objs, new ArrayRowMapper());
//				} else {
//					result = query(pageSql, objs,new ObjectRowMapper<T>(clazz));
//				}
//				pageInfo.setList(result);
//				pageInfo.setRowsCount(totalCount);
//			}
//			
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		
//		return pageInfo;
//	}
	/**
	 * JDBC分页查询
	 * @param clazz  返回结果的类型
	 * @param pageInfo	分页对象
	 * @param sql	sql查询命令
	 * @return
	 */
//	@SuppressWarnings("unchecked")
//	public <T> PageInfo<T> queryPagingToObject(Class<T> clazz, String sql, int pageSize, int pageNo, Object ... objs) {
//		
//		PageInfo<T> pageInfo = null;
//		try {
//			
//			pageInfo = new PageInfo<T>();
//			pageInfo.setPagesize(pageSize);
//			pageInfo.setPagenum(pageNo);
//			
//			String orderInfo = null;
//			int index = -1;
//			sql = sql.toUpperCase();	// 统一转大写
//			if((index = sql.indexOf("ORDER")) > 0) {
//				orderInfo = sql.substring(index);
//				sql = sql.substring(0, index);
//			} else {
//				orderInfo = "";
//			}
//			
//			// 将select到from部分的代码替换成count(*)
//			String queryCountSQL = "SELECT COUNT(*) FROM (" + sql+") TB";
//			
//			// 总记录数
//			Long totalCount = super.queryForLong(queryCountSQL, objs);
//			
//			if(totalCount != null && totalCount != 0) {
//				// 分页起始位置
//				int startNum = pageSize
//						* (pageNo - 1);
//				// 分页结束位置
//				int endNum = startNum + pageSize;
//				
//				
//				// 判断结束位置是否是大于总的条数
//				if (endNum > totalCount) {
//					endNum = Integer.valueOf(totalCount.toString());
//				}
//				
//				// 默认使用的是ORACLE数据库
//				String pageSql = ORACLE_PAGESQL_TEMPLATE;
//				// 判断是否是使用的SQLSERVER数据库
//				if("SQLSERVER".equals(dbType)) {
//					pageSql = SQLSERVER_PAGESQL_TEMPLATE;
//				}
//				
//				// 替换
//				pageSql = StringUtils.replace(pageSql, "${sql}", sql);
//				pageSql = StringUtils.replace(pageSql, "${startNum}", String
//						.valueOf(startNum));
//				pageSql = StringUtils.replace(pageSql, "${endNum}", String
//						.valueOf(endNum));
//				
//				// 添加排序
//				pageSql = pageSql + " "+orderInfo.replaceFirst("\\s", "&nmsp;").replaceAll("\\s.*?\\.", " ").replaceFirst("&nmsp;", " ");
//				
//				List<T> result = null;
//				
//				// 判断是否是需要返回Map对象
//				if("Map".equals(clazz.getSimpleName())) {
//					result = (List<T>) query(pageSql, objs, new ColumnMapRowMapper());
//				} else if("Array".equals(clazz.getSimpleName())) {
//					result = (List<T>) query(pageSql, objs, new ArrayRowMapper());
//				} else {
//					result = query(pageSql, objs, new ObjectRowMapper<T>(clazz));
//				}
//				
//				pageInfo.setList(result);
//				pageInfo.setRowsCount(totalCount);
//			}
//			
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		
//		return pageInfo;
//	}

}
