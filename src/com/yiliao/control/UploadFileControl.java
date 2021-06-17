package com.yiliao.control;

import java.io.File;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.yiliao.util.MessageUtil;
import com.yiliao.util.PrintUtil;
import com.yiliao.util.SystemConfig;


@Controller
@RequestMapping(value = "/share")
public class UploadFileControl {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	
	/**
	 *  鉴黄图片上传
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "uploadAPPFile", method = RequestMethod.POST)
	public String uploadAPP(HttpServletRequest request,HttpServletResponse response) {

		MessageUtil mu = null;

		try {
			long startTime = System.currentTimeMillis();
			// 将当前上下文初始化给 CommonsMutipartResolver （多部分解析器）
			CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
					request.getSession().getServletContext());
			// 检查form中是否有enctype="multipart/form-data"
			if (multipartResolver.isMultipart(request)) {
	           
				// 将request变成多部分request
//				MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
				MultipartResolver resolver = new CommonsMultipartResolver(request.getSession().getServletContext());
		        MultipartHttpServletRequest multiRequest = resolver.resolveMultipart(request);
				// 获取multiRequest 中所有的文件名
				Iterator iter = multiRequest.getFileNames(); 
				while (iter.hasNext()) {
					// 一次遍历所有文件
					MultipartFile mf = multiRequest.getFile(iter.next()
							.toString());
					if (mf != null) {
						
						// 原始文件名
						CommonsMultipartFile cf = (CommonsMultipartFile) mf;
						String fileName = mf.getOriginalFilename(); 
						String route = "/speed/img/";
						if(Config().indexOf("Windows") > -1){
							route ="D:/speed/img/";
						}
						//获取当前系统是否什么类型的 
						String filePath = getFileName(fileName);
						File uploadFile = new File(route + filePath);  //文件保存路径
						
						if(!uploadFile.exists()) {
							uploadFile.getParentFile().mkdir();
						}
						cf.transferTo(uploadFile);
						//上传到
						mu = new MessageUtil(1, "上传成功!");
						mu.setM_object(SystemConfig.getValue("video_img_url")+filePath);
					}
				}
			}
			long endTime = System.currentTimeMillis();
			System.out.println("文件上传耗时：" + String.valueOf(endTime - startTime)
					+ "ms");
			PrintUtil.printWri(mu, response);

		} catch (Exception e) {
			e.printStackTrace();
			logger.error("文件上传异常!", e);
		}
		return null;
	}

	/**
	 * 判断操作系统
	 */
	public static String  Config() {
		try {
			InetAddress addr = InetAddress.getLocalHost();
			String ip = addr.getHostAddress().toString(); // 获取本机ip
			String hostName = addr.getHostName().toString(); // 获取本机计算机名称
			System.out.println("本机IP：" + ip + "\n本机名称:" + hostName);
			Properties props = System.getProperties();
			System.out.println("操作系统的名称：" + props.getProperty("os.name"));
			System.out.println("操作系统的版本号：" + props.getProperty("os.version"));
			return props.getProperty("os.name");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "Centos";
	}
	
	/**
	 * 定义文件名称
	 * @return
	 * @throws Exception
	 */
	public String getFileName(String suffix) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
		String fileName = sdf.format(new Date());
		//随机数
		String number = Math.round((Math.random() * 10000)) + "";
		fileName = fileName+number;
		//获取文件格式
        suffix = suffix.indexOf(".") != -1 ? suffix.substring(suffix.lastIndexOf("."), suffix.length()) : null;
        //新的文件名
        fileName = new SimpleDateFormat("yyyyMMdd").format(new Date())+"/"+fileName+suffix;
		return fileName;
	}
	
}
