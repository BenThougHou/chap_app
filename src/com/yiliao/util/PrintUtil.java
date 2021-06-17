package com.yiliao.util;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

/**
 * CopyRright (c)2015-版权所有: 重庆塞特尔网络科技有限公司
 * 
 * @项目工程名 stgWeb
 * @Module ID <(模块)类编号，可以引用系统设计中的类编号> Comments <对此类的描述，可以引用系统设计中的描述>
 * @JDK 版本(version) JDK1.7.0
 * @命名空间 com.stg.util
 * @作者(Author) 石德文
 * @创建日期 2015-4-13 下午5:42:43
 * @修改人
 * @修改时间 <修改日期，格式:YYYY-MM-DD> 修改原因描述：
 * @Version 版本号 V1.0
 * @类名称 PrintUtil
 * @描述 (输出辅助类)
 */
public class PrintUtil {

	/**
	 * @方法名 printWri
	 * @说明 (输出结果)
	 * @param 参数
	 *            @param str 设定文件
	 * @return void 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	public static void printWri(Object str,HttpServletResponse response) {
		try {
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(JSONObject.fromObject(str));
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	/**
	 * @方法名 printWri
	 * @说明 (输出结果)
	 * @param 参数
	 *            @param str 设定文件
	 * @return void 返回类型
	 * @作者 石德文
	 * @throws 异常
	 */
	public static void printWriStr(String str,HttpServletResponse response) {
		try {
			response.setContentType("text/html;charset=utf-8");
			PrintWriter out = response.getWriter();
			out.print(str);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
