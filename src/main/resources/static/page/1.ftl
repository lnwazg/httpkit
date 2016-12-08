<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <title>学生信息</title>
  </head>
  <body>
        <#include "2.ftl"/><br>
                    姓名：${student.studentName}<br>
                    性别：${student.studentSex}<br>
	         描述：${description}<br/>  
                    集合大小:${nameList?size}<br/>  
        
                     迭代list集合：<br/>  
		<#list nameList as names>  
			这是第${names_index+1}个人，叫做：<label style="color:red">${names}</label> <br/>  
			<#if (names=="陈靖仇")>  
			 	他的武器是: 十五~~  
			<#elseif (names=="宇文拓")>    
			 	他的武器是: 轩辕剑~·  
			<#else>  
				她的绝招是：蛊毒~~  
			</#if>  
			<br/>  
		</#list>  
        
                      迭代map集合：  <br/>  
		<#list weaponMap?keys as key>  
		key ${key} <br/>  
		value ${weaponMap[key]!("null")}  
		
			<#--   
			fremarker 不支持null, 可以用！ 来代替为空的值。  
			其实也可以给一个默认值    
			value-----${weaponMap[key]?default("null")}  
			还可以 在输出前判断是否为null  
			<#if weaponMap[key]??></#if>都可以  
			-->  
		  
		  
		<br/>  
		</#list>  
        
        <#--                   
		include导入文件：  
		-->
		<br/>  
  </body>
</html>