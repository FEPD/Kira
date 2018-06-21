<%@ page language="java" import="java.util.*,com.yihaodian.architecture.kira.manager.security.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
if (!SecurityUtils.isUserAuthenticated(request)) {
	response.sendRedirect(path+"/login.jsp");
}
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>Kira调度平台</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<meta http-equiv="content-type" content="text/html; charset=UTF-8" />
	<!--  
	<link rel="stylesheet" href="css/ico.css" type="text/css"></link>
	<link rel="stylesheet" href="css/layout.css" type="text/css"></link>
	-->
	<link rel="icon" href="favicon.ico" type="image/x-icon" />
	<link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
	<link rel="stylesheet" href="extjs/resources/css/icon.css" type="text/css"></link>
	<link rel="stylesheet" href="extjs/resources/css/ext-all.css" type="text/css"></link>
	<script type="text/javascript" src="extjs/bootstrap.js"></script>
	<script type="text/javascript" src="extjs/local/ext-lang-zh_CN.js"></script>
	<script type="text/javascript" src="app.js"></script>
	<script type="text/javascript" src="app/initMenu.js"></script>
  </head>
  
  <body>
    
  </body>
</html>
