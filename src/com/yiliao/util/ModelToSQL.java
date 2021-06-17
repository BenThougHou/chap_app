package com.yiliao.util;


import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Field;

/**
 * 根据实体自动生成sql语句
 */
public class ModelToSQL {
    /**
     * 生成插入语句
     * @param tablename 表明
     * @param clazz 与数据库中字段一一对应的类
     * @param t 有数据的实体
     * @param <T> 数据实体类型 如 User
     */
    public static  <T> String getInsertSql(String tablename, Class<T> clazz, T t){
        //insert into table_name (column_name1,column_name2, ...) values (value1,value2, ...)
        String sql = "";
        Field[] fields = ReflectUtil.getFieldsDirectly(clazz, false);
        StringBuffer topHalf = new StringBuffer("insert into "+tablename+" (");
        StringBuffer afterAalf = new StringBuffer("values (");
        for (Field field : fields) {
            if ("T_ID".equals(field.getName()) || "t_id".equals(field.getName())){
                continue;   //id 自动生成无需手动插入
            }
            topHalf.append(field.getName() + ",");
            if (ReflectUtil.getFieldValue(t, field.getName()) instanceof String) {
                afterAalf.append("'" + ReflectUtil.getFieldValue(t, field.getName()) + "',");
            } else {
                afterAalf.append(ReflectUtil.getFieldValue(t, field.getName()) + ",");
            }
        }
        topHalf = new StringBuffer(StrUtil.removeSuffix(topHalf.toString(), ","));
        afterAalf = new StringBuffer(StrUtil.removeSuffix(afterAalf.toString(), ","));
        topHalf.append(") ");
        afterAalf.append(") ");
        sql = topHalf.toString() + afterAalf.toString();
        return sql;
    }

    /**
     * 生成更新语句
     * 必须含有id
     * 数据实体中 null 与 空字段不参与更新
     * @param tablename 数据库中的表明
     * @param clazz 与数据库中字段一一对应的类
     * @param t 有数据的实体
     * @param <T> 数据实体类型,如 User
     */
    public static  <T> String getUpdateSql(String tablename, Class<T> clazz, T t){
        String sql = "";
        String id = ""; //保存id名：ID or id
        Field[] fields = ReflectUtil.getFieldsDirectly(clazz, false);
        sql = "update "+tablename+" set ";
        for (Field field : fields) {
            StringBuffer tmp = new StringBuffer();
            if ("T_ID".equals(field.getName()) || "t_id".equals(field.getName())){
                id = field.getName();
                continue;//更新的时候无需set id=xxx
            }
            if (ReflectUtil.getFieldValue(t, field.getName()) != null && ReflectUtil.getFieldValue(t, field.getName()) != "") {
                tmp.append( field.getName() + "=");
                if (ReflectUtil.getFieldValue(t, field.getName()) instanceof String) {
                    tmp.append( "'" + ReflectUtil.getFieldValue(t, field.getName()) + "',");
                } else {
                    tmp.append(ReflectUtil.getFieldValue(t, field.getName()) + ",");
                }
                sql += tmp;
            }
        }
        sql = StrUtil.removeSuffix(sql, ",") + " where " + id + "='" + ReflectUtil.getFieldValue(t, id)+"'";
        return sql;
    }

}
