package com.yiliao.dao.core;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.Assert;

import com.yiliao.dao.util.SelectParam;

/**
 * 数据库核心操作类
 * @author ZhouShuhua	20121120
 *
 * @param <T>
 * @param <PK>
 */
public class BaseHibernateTemplate extends HibernateTemplate {

	// 日志接口
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * 返回Query或SQLQuery后具体的实现接口，由用户自定义
	 * @author ZhouShuhua
	 * @param <T> 返回的类型
	 * @time 2013.4.2
	 */
	protected static interface IQuery<T> {
		
		T doInHibernateImpl(Query query);
		
		T doInHibernateImpl(SQLQuery sqlQuery);
	}
	
	/**
	 * 返回Query或SQLQuery后具体的实现接口适配器
	 * @author ZhouShuhua
	 * @param <T>	返回的类型
	 * @time 2013.4.2
	 */
	protected static abstract class IQueryAdapter<T> extends BaseHibernateTemplate  implements IQuery<T> {
		
		public T doInHibernateImpl(Query query){
			return null;
		}
		
		public T doInHibernateImpl(SQLQuery sqlQuery) {
			return null;
		}
	}
	
	/**
	 * hql命令
	 * @param queryString	
	 * @param iQuery
	 * @author ZhouShuhua
	 * @return
	 */
	protected <T> T executeQuery(final String queryString, final IQuery<T> iQuery) {
		
		// 断言queryString变量一定有内容
		Assert.hasText(queryString);
		// 执行并返回
		return this.execute(
				new HibernateCallback<T>() {

					@Override
					public T doInHibernate(Session session)
							throws HibernateException, SQLException {
						return iQuery.doInHibernateImpl(session.createQuery(queryString));
					}
				}
		);
	}
	
	/**
	 * 纯sql命令
	 * @param queryString
	 * @param iQuery
	 * @author ZhouShuhua
	 * @return
	 */
	protected <T> T executeSqlQuery(final String queryString, final IQuery<T> iQuery) {
		// 断言queryStrign变量一定有内容
		Assert.hasText(queryString);
		// 执行sql命令
		return this.execute(
				new HibernateCallback<T>() {

					@Override
					public T doInHibernate(Session session)
							throws HibernateException, SQLException {
						
						return iQuery.doInHibernateImpl(session.createSQLQuery(queryString));
					}
				}
		);
	}
	
	/**
	 * 设置动态参数
	 * @return 
	 */
	protected <T extends Query> T setDynamicParams(T query, Object ... objs) {
		
		// 判断是否有动态参数
		if(objs != null && objs.length != 0) {
			
			// 设置动态参数
			int i = 0;
			for(Object obj : objs) {
				query.setParameter(i++, obj);
			}
		}
		
		// 返回同一个query
		return query;
	}
	
	/**
	 * 设置动态参数
	 * @return 
	 */
	protected Object[] setPlaceholderToQueryString(StringBuilder queryString, SelectParam ... selectParams) {
		// 断言一定不是空对象
		Assert.notNull(queryString);
		
		// 判断是否有动态参数
		if(selectParams != null && selectParams.length != 0) {
			Object[] obj = new Object[selectParams.length];
			int i = 0;
			// 设置动态参数
			for(SelectParam param : selectParams) {
				queryString.append(" and ").append(param.toString());
				obj[i++] = param.getValue();
			}
			return obj;
		}
		
		// 返回同一个query
		return null;
	}
}