package com.yiliao.dao.impl;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import com.yiliao.dao.IBaseDao;

@Repository("iTemplateDaoImpl")
public class TemplateDAO extends HibernateDaoSupport implements IBaseDao {

	public Session getSessions() {
		return this.getHibernateTemplate().getSessionFactory().getCurrentSession();
	}

	/**
	 * 保存对象
	 */
	public <T> T save(Object t) {
		try {
			return (T) getHibernateTemplate().save(t);

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	/**
	 * 修改对象
	 */
	public void update(Object t) {
		try {
			getHibernateTemplate().update(t);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * 删除对象
	 */
	public void delete(Object... t) {
		try {
			for (int i = 0; i < t.length; i++) {
				getHibernateTemplate().delete(t[i]);

			}

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean executeConSQL(final String pstrsql) {
		try {
			return (Boolean) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session sess)
								throws HibernateException, SQLException {
							return sess.connection().prepareStatement(pstrsql)
									.execute();
						}
					});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int executeHQL(final Object hql, final Object... params) {
		try {

			return (Integer) getHibernateTemplate().execute(
					new HibernateCallback() {

						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							Query query = session.createQuery(hql.toString());
							for (int i = 0; i < params.length; i++) {
								query.setParameter(i, params[i]);
							}
							return query.executeUpdate();
						}
					});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int executeSQL(final Object sql, final Object... params) {

		try {
			return (Integer) getHibernateTemplate().execute(
					new HibernateCallback() {

						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							Query query = session
									.createSQLQuery(sql.toString());
							for (int i = 0; i < params.length; i++) {
								query.setParameter(i, params[i]);
							}

							return query.executeUpdate();

						}
					});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> List<T> findByPageHQL(final Object hql, final int pageNo,
			final int pageSize, final Object... params) {
		try {
			final int begin = pageSize * (pageNo - 1);
			return (List<T>) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							Query query = session.createQuery(hql.toString());
							for (int i = 0; i < params.length; i++) {
								query.setParameter(i, params[i]);
							}
							return query.setMaxResults(pageSize)
									.setFirstResult(begin).list();
						}
					}

			);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> List<T> findByHQL(final Object hql, final Object... params) {
		try {
			return (List<T>) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							Query query = session.createQuery(hql.toString());
							for (int i = 0; i < params.length; i++) {
								query.setParameter(i, params[i]);
							}

							return query.list();
						}
					}

			);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T findByHQLUniqueResult(final Object hql, final Object... params) {
		try {
			return (T) getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query query = session.createQuery(hql.toString());
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
					return (T) query.uniqueResult();
				}
			});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T findByID(Class<T> type, Serializable id) {
		return (T) this.getHibernateTemplate().get(type, id);
	}

	@Override
	public <T> List<T> findByPageSQL(final Object sql, final int pageSize,
			final int pageNo, final Object... params) {
		try {
			final int begin = pageSize * (pageNo - 1);
			return (List<T>) getHibernateTemplate().execute(
					new HibernateCallback() {

						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {

							Query query = session
									.createSQLQuery(sql.toString());
							for (int i = 0; i < params.length; i++) {
								query.setParameter(i, params[i]);
							}
							return query.setMaxResults(pageSize)
									.setFirstResult(begin).list();
						}
					});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> List<T> findBySQL(final Object sql, final Object... params) {
		try {
			return (List<T>) getHibernateTemplate().execute(
					new HibernateCallback() {
						public Object doInHibernate(Session session)
								throws HibernateException, SQLException {
							Query query = session
									.createSQLQuery(sql.toString());
							for (int i = 0; i < params.length; i++) {
								query.setParameter(i, params[i]);
							}
							return query.list();

						}
					});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public <T> T findBySQLUniqueResult(final Object sql, final Object... params) {
		try {
			return (T) getHibernateTemplate().execute(new HibernateCallback() {
				public Object doInHibernate(Session session)
						throws HibernateException, SQLException {
					Query query = session.createSQLQuery(sql.toString());
					for (int i = 0; i < params.length; i++) {
						query.setParameter(i, params[i]);
					}
					return (T) query.uniqueResult();

				}
			});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void clear() {
		
		try {
			
			this.getSession().clear();

		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		
	}

}
