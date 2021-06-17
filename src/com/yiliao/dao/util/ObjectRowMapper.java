package com.yiliao.dao.util;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;

import com.yiliao.util.DateUtils;

/**
 * jdbc查询转换成Object
 * @author ZhouShuhua
 * @time 2013.5.29
 * @param <T>
 */
@SuppressWarnings("unused")
public class ObjectRowMapper<T> implements RowMapper<T> {

	@SuppressWarnings("unchecked")
	private Class<T> elementType;
	
	@SuppressWarnings("unchecked")
	public ObjectRowMapper(Class<T> elementType) {
		this.elementType = elementType;
	}

	@Override
	public T mapRow(ResultSet rs, int rowNum) throws SQLException {
		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		Object dto = org.springframework.beans.BeanUtils.instantiateClass(elementType);
		for (int i = 1; i <= columnCount; i++) {
			String key = JdbcUtils.lookupColumnName(rsmd, i);
			Object obj = JdbcUtils.getResultSetValue(rs, i);
			try {
				if(obj instanceof BigDecimal || obj instanceof Timestamp){//如果取出数据为 BigDecimal 则转换为 dto 对应的数据类型
					String[] properties = BeanUtilss.getProperties(dto);
					for (String p : properties) {
						if (p.equalsIgnoreCase(key)) {
							Field field = BeanUtilss.getDeclaredField(elementType, p);
							
							String propertyTypeName = field.getType().getSimpleName();
							if(propertyTypeName.equalsIgnoreCase("Long")){
								obj = rs.getLong(i);
							}else if(propertyTypeName.equalsIgnoreCase("int") || propertyTypeName.equalsIgnoreCase("Integer")){
								obj = rs.getInt(i);
							}else if (propertyTypeName.equalsIgnoreCase("Double")){
								obj = rs.getDouble(i);
							}else if (propertyTypeName.equalsIgnoreCase("Float")){
								obj = rs.getFloat(i);
							}else if (propertyTypeName.equalsIgnoreCase("Calendar")){
								obj = DateUtils.date2Cal(new Date(rs.getTimestamp(i).getTime()));
							}else if (propertyTypeName.equalsIgnoreCase("String")){
								obj = rs.getString(i);
							}
							break;
						}
					}
				}
				
				if (key.split("__").length > 1){ //如果取出数据名称为 xx.xx ,这创建实体并赋值
					String[] properties = BeanUtilss.getProperties(dto);
					String[] asName =  key.split("__");
					for (String p : properties) {
						if (p.equalsIgnoreCase(asName[0])){
							Object[] innerObj = new Object[asName.length];
							Field[] fields = new Field[asName.length];
							for (int k = 0;k < asName.length;k++){
								if (k == 0) {
									fields[k] = BeanUtilss.getDeclaredField(dto.getClass(), p);
									if (BeanUtilss.getDeclaredProperty(dto,fields[k])==null){
										innerObj[k] = org.springframework.beans.BeanUtils.instantiateClass(fields[k].getType());
										BeanUtilss.setProperty(dto, fields[k].getName(), innerObj[k]);
									}else{
										innerObj[k] = BeanUtilss.getDeclaredProperty(dto,fields[k]);
									}
								}else if (k != asName.length -1){
									String[] innerproperties = BeanUtilss.getProperties(innerObj[k-1]);
									for (String inp : innerproperties) {
										if (inp.equalsIgnoreCase(asName[k])){											
											fields[k] = BeanUtilss.getDeclaredField(innerObj[k-1].getClass(), inp);
											break;
										}
									}
									
									if (BeanUtilss.getDeclaredProperty(innerObj[k-1],fields[k])==null){
										innerObj[k] = org.springframework.beans.BeanUtils.instantiateClass(fields[k].getType());
										BeanUtilss.setProperty(innerObj[k-1], fields[k].getName(), innerObj[k]);
									}else{
										innerObj[k] = BeanUtilss.getDeclaredProperty(innerObj[k-1],fields[k]);
									}
								}else{
									String[] innerproperties = BeanUtilss.getProperties(innerObj[k-1]);
									for (String inp : innerproperties) {
										if (inp.equalsIgnoreCase(asName[k])){											
											fields[k] = BeanUtilss.getDeclaredField(innerObj[k-1].getClass(), inp);
											
											if(obj instanceof BigDecimal || obj instanceof Timestamp){												
												String propertyTypeName = fields[k].getType().getSimpleName();
												if(propertyTypeName.equalsIgnoreCase("Long")){
													obj = rs.getLong(i);
												}else if(propertyTypeName.equalsIgnoreCase("int") || propertyTypeName.equalsIgnoreCase("Integer")){
													obj = rs.getInt(i);
												}else if (propertyTypeName.equalsIgnoreCase("Double")){
													obj = rs.getDouble(i);
												}else if (propertyTypeName.equalsIgnoreCase("Float")){
													obj = rs.getFloat(i);
												}else if (propertyTypeName.equalsIgnoreCase("Calendar")){
													obj = DateUtils.date2Cal(rs.getDate(i));
												}
											}
											
											break;
										}
									}
									BeanUtilss.setProperty(innerObj[k-1], fields[k].getName(), obj);
								}
								
							}
						}
					}
				}else{
					BeanUtilss.setProperty(dto, key, obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		return (T)dto;
	}

	@SuppressWarnings("unchecked")
	public Class getElementType() {
		return elementType;
	}

	@SuppressWarnings("unchecked")
	public void setElementType(Class elementType) {
		this.elementType = elementType;
	}
}
