<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!doctype html>
<html>
<head design-width="750">
<meta charset="utf-8">
<meta name="viewport"
	content="width=device-width,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no" />
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<meta name="format-detection" content="telephone=no">
<title>立即下载</title>
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css" />
<!--页面样式-->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/common.css" />
<!--常用样式-->
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/animate.min.css" />
<!--CSS3动画库-->
<script src="${pageContext.request.contextPath}/js/auto-size.js"></script>
<!--设置字体大小-->
<script src="${pageContext.request.contextPath}/js/mobile-detect.js"></script>
<!--设置字体大小-->
<script src="${pageContext.request.contextPath}/js/clipboard.min.js"></script>
<script src="http://pv.sohu.com/cityjson?ie=utf-8"></script>
<!--设置字体大小-->
<script type="text/javascript">
	//获取初始信息
	var app = navigator.appVersion;
	//根据括号进行分割
	var left = app.indexOf('(');
	var right = app.indexOf(')');
	var str = app.substring(left + 1, right);

	var Str = str.split(";");
	//手机型号--苹果 iPhone
	var Mobile_Iphone = Str[0];
	//手机型号--安卓 
	var Mobile_Android = Str[2];
	// 红米手机等特殊型号处理 匹配字符
	var res = /Android/;
	var reslut = res.test(Mobile_Android);
	//设备
	var equipment = '';
	//系统
	var system_moble = '';

	//根据设备型号判断设备系统
	if (Mobile_Iphone.indexOf('iPhone') >= 0) {
		equipment = 'iPhone';
		system_moble = Str[1].substring(4, Str[1].indexOf('like'));
	} else if (Mobile_Iphone == 'Linux') {
		if (reslut) {
			equipment = 'Android';
			system_moble = Str[2];
		} else {
			equipment = 'Android';
			system_moble = Str[1];
		}
	}
</script>
</head>
<body class="bg">
	<div class="mobile-wrap center download">
		<input type="hidden" id="userId" value="${userId}">
		<input type="hidden" id="jumpToB" value="${jumpToB}">
		 <input
			id="t_android_download" type="hidden" value="${t_android_download}">
		<input id="t_ios_download" type="hidden" value="${t_ios_download}">
		<div class="download-btn">
		    <span></span>
			<a href="javascript:void(0);" onclick="on_click_doload(this);" class="copy_salq">立即下载</a>
		</div>
		<div class="download-prompt">
			<img src="${pageContext.request.contextPath}/images/title.png" />
		</div>
	</div>
	<!--mobile_wrap-->
</body>
<script src="${pageContext.request.contextPath}/js/jquery-2.2.4.min.js"></script>
<!--jQ库-->
<script src="${pageContext.request.contextPath}/js/Swiper.js"></script>
<!--轮播库-->
<script src="${pageContext.request.contextPath}/js/version-3.2.8.js"></script>
<!--封装函数-->
<script type="text/javascript">
	/* 跳转到某一个B域名 */
	window.location.href = $('#jumpToB').val();
</script>
</html>


