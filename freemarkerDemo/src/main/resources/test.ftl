<html>
<head>
	<meta charset="utf-8">
	<title>Freemarker入门小DEMO</title>
	<!-- 商品详情 -->
	<#-- 111 -->
</head>
<body>
	<#include 'head.ftl'>
	${name},你好.${message}
	<br>
	<#assign linkman="喵小姐">
	联系人：${linkman}
	<br>
	<#assign info={"address":"广州",'mobile':'123456789'}>
	地址:${info.address} ,电话:${info.mobile}
	<br>
	<#if success==true>
		你好,你成功了
	<#else>
		你好,你失败了
	</#if>
	<br>
	
	----商品价格表----<br>
	<#list goodsList as goods>
		${goods_index*2}商品:${goods.name},价格:${goods.price}<br>
	</#list>
	共  ${goodsList?size}  条记录
	<br>
	<#assign text="{'bank':'工商银行','account':'10101920201920212'}" />
	<#assign data=text?eval />
	开户行：${data.bank}  账号：${data.account}
	<br>
	当前日期：${today?date} <br>
	当前时间：${today?time} <br>   
	当前日期+时间：${today?datetime} <br>        
	日期格式化：  ${today?string("yyyy年MM月dd日 HH:ss:mm")}	
	<br>
	累计积分：${point}	<br>
	累计积分：${point?c}
	<br>
	 	${aaa!'1'}
	<br>
	<#if aaa??>
	  aaa变量存在
	<#else>
	  aaa变量不存在
	</#if>	

</body>


</html>