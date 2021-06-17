<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge,chrome=1" />
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0"/>
    <title>支付成功</title>
    <script src="js/jquery-2.0.3.min.js"></script>
</head>
<body>
 
<!-- <div id="id_video_container" style="width:100%; height:auto;">11</div> -->
<div id="success">
	<font size="20p">恭喜,支付成功......! </font>
</div>

</body>
</html>
