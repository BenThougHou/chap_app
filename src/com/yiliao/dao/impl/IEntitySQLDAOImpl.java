package com.yiliao.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.yiliao.dao.IEntitySQLDAO;
import com.yiliao.dao.core.BaseJdbcTemplate;
import com.yiliao.dao.util.ArrayRowMapper;
import com.yiliao.util.PageInfo;

@Repository("iEntitySQLDAOImpl")
public class IEntitySQLDAOImpl extends BaseJdbcTemplate implements IEntitySQLDAO {

	public IEntitySQLDAOImpl() {
		logger.info("正在加载......");
	}

	// --------------------------------------------------------
	@Override
	public boolean executeConSQL(String pstrsql) {
		super.execute(pstrsql);
		return false;
	}

//	@Override
//	public <T> List<T> findByPageSQL(final String sql, int pageSize, int pageNo,
//			Object ... params) {
//		return super.queryPagingToObject(Array.class, new PageInfo(pageSize, pageNo), sql, params).getList();
//	}

	@Override
	public <T> List<T> findBySQL(final String sql, Object... params) {

		return (List<T>) super.query(sql, params, new ArrayRowMapper());
	}

	@Override
	public List<Map<String, Object>> findBySQLTOMap(final String sql, Object... params) {

		return super.query(sql, params, new ColumnMapRowMapper());
	}

	/**
	 * 执行sql
	 */
	@Override
	public int executeSQL(final String sql, final Object... params) {
		return super.execute(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				return con.prepareStatement(sql);
			}
		}, new PreparedStatementCallback<Integer>() {

			@Override
			public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

				for (int i = 0; i < params.length; i++) {
					ps.setObject(i + 1, params[i]);
				}
				return ps.executeUpdate();
			}
		});
	}

	@Override
	public <T> T findBySQLUniqueResult(final String sql, Object... params) {
		return (T) super.queryForObject(sql, params, new ArrayRowMapper());
	}

	@Override
	public Map<String, Object> findBySQLUniqueResultToMap(final String queryString, final Object... params) {
		return super.queryForObject(queryString, params, new ColumnMapRowMapper());
	}

	@Override
	public <T> PageInfo<T> queryPagingToObject(Class<T> clazz, String sql, int pageSize, int pageNo, Object... objs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> PageInfo<T> queryPagingToObject(Class<T> clazz, PageInfo<T> pageInfo, String sql, Object... objs) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> findByPageSQL(String sql, int pageSize, int pageNo, Object... params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int saveData(String insertString, Object... objects) {

		return super.execute(new PreparedStatementCreator() {

			@Override
			public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
				return con.prepareStatement(insertString, Statement.RETURN_GENERATED_KEYS);
			}
		}, new PreparedStatementCallback<Integer>() {

			@Override
			public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException, DataAccessException {

				for (int i = 0; i < objects.length; i++) {
					ps.setObject(i + 1, objects[i]);
				}
				Integer primary_key = 0;
				if (ps.executeUpdate() > 0) {
					ResultSet rs = ps.getGeneratedKeys();
					if (rs.next()) {
						primary_key = Integer.valueOf(rs.getInt(1));
					}
				}
				return primary_key;
			}
		});
	}
}
