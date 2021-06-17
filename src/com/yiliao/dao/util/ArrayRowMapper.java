package com.yiliao.dao.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * jdbc查询转换成Object
 * @author ZhouShuhua
 * @time 2013.5.29
 * @param <T>
 */
@SuppressWarnings("unused")
public class ArrayRowMapper implements RowMapper<Object[]> {

	// 返回数组集合
	@Override
	public Object[] mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData tsmd = rs.getMetaData();
		Object[] obj = new Object[tsmd.getColumnCount()];
		for(int i = 0; i < obj.length; i++) {
			obj[i] = rs.getObject(i+1);
		}
		return obj;
	}
}
