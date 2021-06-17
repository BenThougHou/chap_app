package com.yiliao.util;

import java.io.IOException;
import java.util.List;


import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;


/**
 * 为对象添加公共的属性
 * @author ZhouShuhua
 * @time 2012.1.9
 */
public class JSON {
	
	private static ObjectMapper objMapper;
	
	static {
		objMapper = new ObjectMapper();
	}
	
	/**
	 * 给集合中的每个对象添加额外的pageInfo对象属性
	 * @param list						集合
	 * @param pstrJson					要添加的json格式的数据
	 * @param pbIsFilterByAttr			是否根据属性过滤
	 * @param pstrFilter				需要过滤的名称
	 * @return
	 * @throws SysGlobalException
	 * ZhouShuhua     2012.2.8
	 */
	public static String addExtraAttr(List<?> list, String pstrJson, String ... pstrAttrs){
		// 判断List是否为空
		if(list == null) {
			return "[]";
		}
		StringBuilder json = new StringBuilder("[");
		// 循环遍历集合
		for(Object obj : list) {
			json.append(addExtraAttr(obj, pstrJson, pstrAttrs)).append(",");
		}
		if(json.length()>1) {
			return json.substring(0, json.length()-1)+"]";
		} else {
			return "[]";
		}

	} 
	
	/**
	 * 给对象添加额外的pageInfo对象属性
	 * @param list						集合
	 * @param pJson						要添加的json格式的数据
	 * @param pbIsFilterByAttr			是否根据属性过滤
	 * @param pstrFilter				需要过滤的名称
	 * @return
	 * @throws SysGlobalException
	 * ZhouShuhua     2012.2.8
	 */
	public static String addExtraAttr(Object obj, String pstrJson, String ... pstrAttrs ) {
		
		// 判断对象是否为空
		if(obj == null) {
			return "{}";
		}
		
		try {
			return convertNullToEmpty((toJosn(obj, pstrAttrs)+pstrJson).replaceAll("\\}\\{", ","));
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return "{}";
	}

	/**
	 *  解析JAVA对象为json对象字符串
	 * @param obj				对象
	 * @param attrs				属性数组
	 * @return
	 * @throws JsonGenerationException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	public static String toJosn(Object obj, String ... pstrAttrs) throws JsonGenerationException, JsonMappingException, IOException {
		
		FilterProvider filter = new SimpleFilterProvider().addFilter("filter", 
				SimpleBeanPropertyFilter.serializeAllExcept(pstrAttrs));
		return convertNullToEmpty(objMapper.filteredWriter(filter).writeValueAsString(obj));
	}
	
	// 将Json字符串转换为相应的对象
	public static <T> T toObject(String pstrJson, Class<T> cla) throws JsonParseException, JsonMappingException, IOException {
		
		return objMapper.readValue(pstrJson, cla);
	}
	
	/**
	 * 将json格式中的null值转换成""串
	 * @return
	 */
	private static String convertNullToEmpty(String pStrJson) {
		
		// 使用正则表达式转
		return pStrJson.replaceAll("(:null|:NULL)", ":\"\"");
	}
}
