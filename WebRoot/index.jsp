<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <title>前端播放m3u8格式视频</title>
    <link rel="stylesheet" href="./video.css">
    <script src="${pageContext.request.contextPath}/js/video.min.js"></script>
    <script src="./videojs-contrib-hls.js"></script>
</head>
<body>
    <video id="myVideo" class="video-js vjs-default-skin vjs-big-play-centered" controls preload="auto" width="1080" height="708" data-setup='{}'>    
        <source id="source" src="https://www.the5fire.com/static/demos/diaosi.m3u8"  type="application/x-mpegURL">
    </video>
</body>
<script>    
    // videojs 简单使用
    var myVideo = videojs('myVideo',{
        bigPlayButton : true, 
        textTrackDisplay : false, 
        posterImage: false,
        errorDisplay : false,
    });
    myVideo.play(); // 视频播放
    myVideo.pause(); // 视频暂停
</script>
</html>