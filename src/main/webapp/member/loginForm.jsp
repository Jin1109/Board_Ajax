<%@ page language="java" contentType="text/html; charset=EUC-KR"%>
<!doctype html>
<html>
<head>
<title>ȸ������ �ý��� �α��� ������</title>
<link href="css/login.css" rel="stylesheet" type="text/css">
<script src="js/jquery-3.6.0.js"></script>
<script>
$(function(){
	$(".join").click(function(){
		location.href = "join.net"
	});
	
	var id = '${id}';
	if(id){
		$("#id").val(id);
		$("#remember").prop('checked',true);
	}
})
</script>
</head>
<body>
<form name="loginform" action="loginProcess.net" method="post">
	<h1>�α���</h1>
	<hr>
	<b>���̵�</b>
	<input type="text" name="id" placeholder="Enter id" id="id" required>
	
	<b>��й�ȣ</b>
	<input type="password" name="pass" placeholder="Enter password" required>
	<input type="checkbox" id="remember" name="remember" value="store">
	<span>remember</span>
	
	<div class="clearfix">
		<button type="submit" class="submitbtn">�α���</button>
		<button type="button" class="join">ȸ������</button>
	</div>
</form>
</body>
</html>