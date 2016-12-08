	<#include "../include/doctype.ftl"/>
	<title>首页</title>
	<link href="css/main.css" rel="stylesheet" />
	<script type="text/javascript" src="js/jquery-2.1.1.js"></script>
	<script type="text/javascript" src="js/ns.js"></script>
</head>
<body>
	<h1>你成功地运行了freemarker内嵌系统！</h1>
	<h2>Hello world！</h2>
	<script>
		$(function(){
			$.ajax({
				url:"http://hzweb.cekid.com/goods/queryProductDetailInfo.do?sku_id=1",
				dataType:'json',
				cache:false,
				type: 'post', 
				success:function(data){
					console.log(data);
				}
			});
		});
	</script>
</body>
</html>