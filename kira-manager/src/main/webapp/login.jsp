<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
	<base href="<%=basePath%>">
	<title>Kira调度平台</title>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
	<meta charset="utf-8" />
	<title>Login - Kira-monitor</title>
	<meta name="description" content="Kira-monitor" />
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />

	<!-- bootstrap & fontawesome -->
	<link rel="stylesheet" href="/statics/ace/css/bootstrap.min.css" />
	<link rel="stylesheet" href="/statics/ace/font-awesome/4.2.0/css/font-awesome.min.css" />

	<!-- ace styles -->
	<link rel="stylesheet" href="/statics/ace/css/ace.min.css" />

	<!--[if lte IE 9]>
	<link rel="stylesheet" href="/statics/ace/css/ace-part2.min.css" />
	<![endif]-->
	<link rel="stylesheet" href="/statics/ace/css/ace-rtl.min.css" />

	<!--[if lte IE 9]>
	<link rel="stylesheet" href="/statics/ace/css/ace-ie.min.css" />


	<![endif]-->

</head>


<body class="login-layout">
<div class="main-container">
	<div class="main-content">
		<div class="row">
			<div class="col-sm-10 col-sm-offset-1">
				<div class="login-container">
					<div class="center">
						<br/><br/>
						<h1>
							<i class="ace-icon fa fa-leaf green"></i>
							<span class="red">Kira</span>
<%--
							<span class="white" id="id-text2">Monitor</span>
--%>
						</h1>
						<h4 class="blue" id="id-company-text"></h4>
						<br/>
					</div>

					<div class="space-6"></div>

					<div class="position-relative">
						<div id="login-box" class="login-box visible widget-box no-border">
							<div class="widget-body">
								<div class="widget-main">
									<h4 class="header blue lighter bigger">
										<i class="ace-icon fa fa-coffee green"></i>
										Please enter ERP account:
									</h4>

									<div class="space-6"></div>

									<form>
										<fieldset>
											<label class="block clearfix">
														<span class="block input-icon input-icon-right">
															<input id="txtUsername" type="text" class="form-control" placeholder="Username" />
															<i class="ace-icon fa fa-user"></i>
														</span>
											</label>

											<label class="block clearfix">
														<span class="block input-icon input-icon-right">
															<input id="txtPassword" type="password" class="form-control" placeholder="Password" />
															<i class="ace-icon fa fa-lock"></i>
														</span>
											</label>

											<div class="space"></div>

											<div class="clearfix">
												<label class="inline">
													<input type="checkbox" id="ckbRemember" class="ace" />
													<span class="lbl"> Remember Me</span>
												</label>

												<button id="btn-login-action" type="button" class="width-35 pull-right btn btn-sm btn-primary">
													<i class="ace-icon fa fa-key"></i>
													<span class="bigger-110">Login</span>
												</button>
											</div>

											<div class="space-4"></div>
										</fieldset>
									</form>
								</div><!-- /.widget-main -->
							</div><!-- /.widget-body -->
						</div><!-- /.login-box -->

					</div><!-- /.position-relative -->

					<div class="navbar-fixed-top align-right">
						<br />
						&nbsp;
						<a id="btn-login-dark" href="#">Dark</a>
						&nbsp;
						<span class="blue">/</span>
						&nbsp;
						<a id="btn-login-blur" href="#">Blur</a>
						&nbsp;
						<span class="blue">/</span>
						&nbsp;
						<a id="btn-login-light" href="#">Light</a>
						&nbsp; &nbsp; &nbsp;
					</div>
				</div>
			</div><!-- /.col -->
		</div><!-- /.row -->
	</div><!-- /.main-content -->
</div><!-- /.main-container -->

<!-- basic scripts -->

<!--[if !IE]> -->

<script src="/statics/ace/js/jquery.2.1.1.min.js"></script>

<!-- <![endif]-->

<!--[if IE]>
<script src="/statics/ace/js/jquery.1.11.1.min.js"></script>
<![endif]-->

<!--[if !IE]> -->
<script type="text/javascript">
	window.jQuery || document.write("<script src='/statics/ace/js/jquery.min.js'>"+"<"+"/script>");
</script>

<!-- <![endif]-->

<!--[if IE]>
<script type="text/javascript">
	window.jQuery || document.write("<script src='/statics/ace/js/jquery1x.min.js'>"+"<"+"/script>");
</script>
<![endif]-->
<script type="text/javascript">
	if('ontouchstart' in document.documentElement) document.write("<script src='/statics/ace/js/jquery.mobile.custom.min.js'>"+"<"+"/script>");
</script>

<script src="/statics/ace/js/bootstrap.min.js"></script>

<!-- page specific plugin scripts -->
<script src="/statics/ace/js/jquery-ui.min.js"></script>
<script src="/statics/ace/js/jquery.ui.touch-punch.min.js"></script>
<script src="/statics/ace/js/grid.locale-en.js"></script>

<!-- ace scripts -->
<script src="/statics/ace/js/ace-elements.min.js"></script>
<script src="/statics/ace/js/ace.min.js"></script>
<script src="/statics/ace/js/bootbox.min.js"></script>

<!-- dgate scripts -->
<script src="/statics/web/js/jquery.cookie.js"></script>

<!-- inline scripts related to this page -->
<script type="text/javascript">

	jQuery(function($) {
		$(document).on('click', '.toolbar a[data-target]', function(e) {
			e.preventDefault();
			var target = $(this).data('target');
			$('.widget-box.visible').removeClass('visible');//hide others
			$(target).addClass('visible');//show target
		});

		$('#btn-login-dark').on('click', function(e) {
			changeTheme("dark");
			e.preventDefault();
		});
		$('#btn-login-light').on('click', function(e) {
			changeTheme("light");
			e.preventDefault();
		});
		$('#btn-login-blur').on('click', function(e) {
			changeTheme("blur");
			e.preventDefault();
		});

		function doEnter(){
			$("#txtUsername,#txtPassword,#btn-login-action").keydown(function(e){
				if(e.keyCode == 13){
					if(navigator.userAgent.toUpperCase().indexOf('MSIE') >= 0){
						$('#btn-login-action').trigger('click');
					}else{
						$("#btn-login-action").click();
					}
				}
			});
		};
		function setTheme(theme){
			$.cookie('theme', theme, { expires: 365, path: '/' });
		}
		function changeTheme(theme){
			if(theme=="dark"){
				$('body').attr('class', 'login-layout');
				$('#id-text2').attr('class', 'white');
				$('#id-company-text').attr('class', 'blue');
			}else if(theme=="light"){
				$('body').attr('class', 'login-layout light-login');
				$('#id-text2').attr('class', 'grey');
				$('#id-company-text').attr('class', 'blue');
			}else if(theme=="blur"){
				$('body').attr('class', 'login-layout blur-login');
				$('#id-text2').attr('class', 'white');
				$('#id-company-text').attr('class', 'light-blue');
			}
			setTheme(theme);
		}
		function getTheme(){
			return $.cookie('theme');
		}
		function getUserId(){
			return $.cookie('userId');
		}
		doEnter();
		$("#txtUsername").focus();
		var theme = getTheme();
		if(null != theme){
			changeTheme(theme);
		}
		var userId = getUserId();
		if(null != userId){
			$("#txtUsername").val(userId);
			$("#ckbRemember").prop("checked",true);
			$("#txtPassword").focus();
		}
		//login action
		$('#btn-login-action').on('click', function(e) {
			var userId = $("#txtUsername").val();
			var password = $("#txtPassword").val();

			if(userId === "") {
				bootbox.alert("please enter username.");
				$("#txtUsername").focus();
				return;
			}
			if(password === "") {
				bootbox.alert("please enter password.");
				$("#txtPassword").focus();
				return;
			}

			var remember = $("#ckbRemember").prop("checked");
			if(true === remember){
				$.cookie('userId', userId,{ expires: 365, path: '/' });
			}else{
				//$.cookie('userId', null);

				$.cookie('userId', null, { expires: -1, path: '/' });
			}

			var url = "controller?";
			var params = {
				"userName" : userId,
				"password" : password,
				"method" : "loginCheck"
			};
			$.post(url, params, function callback(data) {
				var result = JSON.parse(data);
				if(result.success){
					//redirect main page
					var redirectUrl = "controller?method=index";
					window.location=redirectUrl;
				}else{
					bootbox.alert(result.msg);
				}
			});
		});
	});
</script>

</body>


</html>


<!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->

<!--[if lt IE 9]>
<script src="http://dgate-monitor/statics/ace/js/html5shiv.min.js"></script>
<script src="http://dgate-monitor/statics/ace/js/respond.min.js"></script>

<%--<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<!--
<link rel="stylesheet" href="css/ico.css" type="text/css"></link>
<link rel="stylesheet" href="css/layout.css" type="text/css"></link>
-->
<%--
<link rel="icon" href="favicon.ico" type="image/x-icon" />
<link rel="shortcut icon" href="favicon.ico" type="image/x-icon" />
<link rel="stylesheet" href="extjs/resources/css/icon.css" type="text/css">
<link rel="stylesheet" href="extjs/resources/css/ext-all.css" type="text/css">
	<link rel="stylesheet" href="common/css/login.css" type="text/html">
<script type="text/javascript" src="extjs/bootstrap.js"></script>
<script type="text/javascript" src="extjs/local/ext-lang-zh_CN.js"></script>&ndash;%&gt;
</head>

<body>
	<script>
	 Ext.Loader.setConfig({
        enabled: true,
        paths: {
            'Kira': 'app'
        }
    });
    Ext.require('Kira.Login');
    Ext.onReady(function () {
        if (Ext.BLANK_IMAGE_URL.substr(0, 4) != "data") {
            Ext.BLANK_IMAGE_URL = "extjs/resources/images/s.gif";
        }
        Kira.Login.show();
    })
	</script>
</body>
</html>--%>
