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
    <title>直播</title>
    <script src="js/jquery-2.0.3.min.js"></script>
</head>
<body>
 
<!-- <div id="id_video_container" style="width:100%; height:auto;">11</div> -->
<div id="video">

</div>

 
<script src="//qzonestyle.gtimg.cn/open/qcloud/video/live/h5/live_connect.js" charset="utf-8"></script>
<script type="text/javascript">
    (function () {
        /*  var player = new qcVideo.Player("id_video_container", {
        	"live_url" : "http://27940.liveplay.myqcloud.com/live/27940_82663509550b25df09d0df512e5377c3.m3u8",
        	"live_url2" : "http://27940.liveplay.myqcloud.com/live/27940_82663509550b25df09d0df512e5377c3.flv",
        	"width" : 200,
        	"height" : 340
        }); */
        setInterval("aa()",5000);
    })();
    
    var index = 1;
    
    function aa(){
    	
    	var obj = '<div style="float: left;margin-left: 15px;">';
    	obj = obj + '<div id="id_video_container'+index+'" style="width:200px; height:340px;"></div>';
    	  
    	obj = obj +  '<span>房间号:100024</span>';
    	obj = obj +  '<span>连线人:张三</span>';
    	obj = obj +  '</br>';
    	obj = obj +  '<span>连线人:张三</span>';
    	obj = obj +  '<span>连线人:张三</span>';
    	obj = obj +  '</div>';
    	$('#video').append(obj);
    	
    	new qcVideo.Player("id_video_container"+index, {
        	"live_url" : "http://27940.liveplay.myqcloud.com/live/27940_82663509550b25df09d0df512e5377c3.m3u8",
        	"live_url2" : "http://27940.liveplay.myqcloud.com/live/27940_82663509550b25df09d0df512e5377c3.flv",
        	"width" : 200,
        	"height" : 340
        });
    	index ++;
    }
    
    
</script>
</body>
</html>
