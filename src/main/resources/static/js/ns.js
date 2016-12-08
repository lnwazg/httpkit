/**
 * @author LiNan
 * 这里是我的专属命名空间<br>
 * 我的JS命名空间的解释：<br>
 * 简称	 	释义
 * NS		namespace的缩写，表示通用的命名空间。这里面放置的全部是工具类。当你不确定你的工具类方法应该放在什么地方的时候，你就可以将其放在这里。<br>
 * 			其中，NS里面放置的是基础性的通用方法，便于在任何的业务代码中方便地调用。<br>
 * 			而NS.webExt里面放置的是Web领域方向专用方法。其相比通用方法，更加领域化。<br>
 * 			NS和NS.webExt之间的关系，好比JAVA SE与JAVA EE之间的关系。前者是基础，是基石，是赖以生存的根基。后者是前者的有力补充、重要扩充，是资料片。<br>
 * 			好的设计模式，必然是经久不衰的！在实践的过程中，不断地提炼出结构化、模块化、可复用的、通用性强的代码，形成自己的个性代码库，从长远来看，都是超值的。<br>
 * 			这个框架就是JS最佳实践所提取出来的干货！是理论的脱水版！<br>
 * PB		pubSub，发布订阅设计模式的一个实现。利用此种模式，可以方便的为指定的对象绑定事件、触发事件。<br>
 */
var NS = {
	browser:{
		versions : function() {
			var u = navigator.userAgent, app = navigator.appVersion;
			return { // 移动终端浏览器版本信息
				iOS : !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/), 		// ios终端
				android : u.indexOf('Android') > -1 || u.indexOf('Linux') > -1, 	// android终端或uc浏览器
				iPhone : u.indexOf('iPhone') > -1, 			// 是否为iPhone或者QQHD浏览器
				iPad : u.indexOf('iPad') > -1				// 是否iPad
			};
		}()},
	/**
	 * 动态获取cookie的域的方法<br>
	 * 该方法可作为适配器方法
	 */	
	getCookieDomain:function(){
//		return (NS.cookieDomain? (";domain="+ NS.cookieDomain):"");
		return (NS.cookieDomainFn? (";domain="+ NS.cookieDomainFn()):"");
	},
	/**
	 * 写cookies
	 * name键
	 * value值
	 */	
	setCookie:function(name,value,expireDays){
		var Days = 36000;//(默认为永远不过期)写入之后有效期设置为100年，也就是相当于永不过期
		if(expireDays){
			//假如传了过期时间这个参数
			Days = expireDays;
		}
		var exp = new Date();    
		exp.setTime(exp.getTime() + Days*24*60*60*1000);
		document.cookie=(name + "="+ escape(value) + ";expires=" + exp.toGMTString()+";path=/" + this.getCookieDomain()); //设置cookie的键值对信息以及过期信息 (指定path真的很重要！) 
	},
	//写cookie。该cookie的生命周期为：session
	setCookieInSession:function(name,value){
		var isIE=!-[1,];//判断是否是ie核心浏览器  
		if(isIE){  
			document.cookie = (name + "="+ escape(value) + ";expires=At the end of the Session;path=/" + this.getCookieDomain());//设置cookie的键值对信息以及过期信息 (指定path真的很重要！) 
		}else{
			document.cookie = (name + "="+ escape(value) + ";expires=Session;path=/" + this.getCookieDomain());//设置cookie的键值对信息以及过期信息 (指定path真的很重要！) 
		}
	},
	//读取cookies
	getCookie:function(name){   
		var arr,reg=new RegExp("(^| )"+name+"=([^;]*)(;|$)");   
		if(arr=document.cookie.match(reg)){
			return unescape(arr[2]);
		}else{
			return null;   
		}
	},
	//删除cookies   
	delCookie:function(name){   
		var exp = new Date();   
		exp.setTime(exp.getTime() - 1);//将过期时间设置为上一毫秒   
		var cval=this.getCookie(name);   
		if(cval!=null){
			document.cookie= (name + "="+cval+";expires="+exp.toGMTString()+";path=/" + this.getCookieDomain());//若能取到这个cookie的键值对，那么就将其设置为过期    (指定path真的很重要！) 
		}
	},
	//删除cookies,未指定域名的
	delCookieWithoutDomain:function(name){
		var exp = new Date();   
		exp.setTime(exp.getTime() - 1);//将过期时间设置为上一毫秒   
		var cval=this.getCookie(name);   
		if(cval!=null){
			document.cookie= (name + "="+cval+";expires="+exp.toGMTString()+";path=/");//若能取到这个cookie的键值对，那么就将其设置为过期    (指定path真的很重要！) 
		}
	},
	//判断某个字符串是否为空
	isNull:function(obj){
		if(!obj || obj.length ==0){
			return true;
		}
		return false;
	},
	//判断是否为空
	isEmpty:function(obj){
		return this.isNull(obj);
	},
	/**
	 * 是否不为空
	 */
	isNotEmpty:function(obj){
		return !this.isNull(obj);
	},
	//某个数组中是否包含某个元素
	contains:function(element,array){
		if(this.indexOf(element,array)!=-1){
			return true;
		}
		return false;
	},
	/**
	 * 更适用于对象比较的版本
	 * @param element
	 * @param array
	 */
	objContains:function(element,array){
		if(this.objIndexOf(element,array)!=-1){
			return true;
		}
		return false;
	},
	//返回某个元素在数组中的位置
	indexOf:$.inArray,
	/**
	 * 更适用于对象比较的版本<br>
	 * 将每个元素分别转换成string之后再进行查找。(此种查找方法适用于复杂对象的查找，并且那种情况下indexOf()方法将会失效)
	 */
	objIndexOf:function(element,array){
		var eleNew = JSON.stringify(element);
		var arrayNew = [];
		for(var i=0;i<array.length;i++){
			arrayNew[i] = JSON.stringify(array[i]);
		}
		return this.indexOf(eleNew,arrayNew);
	},
	/**
	 * 删除数组中某个索引的数据
	 * @param idx
	 * @param array
	 * @returns
	 */
	removeIdx:function(idx, array){
		if(isNaN(idx)||idx>array.length){
			return false;
		}
		for(var i=0,n=0; i<array.length; i++){
			if(array[i]!=array[idx]){
				array[n++]=array[i];
			}
		}
		array.length-=1;//如果数组已经没元素了，再删除就会报错！
		return array;
	},	
	/**
	 * 更适用于对象比较的版本
	 * @param idx
	 * @param array
	 * @returns
	 */
	objRemoveIdx:function(idx, array){
		return this.removeIdx(idx, array);
	},
	/**
	 * 删除数组中的某个元素
	 * @param element
	 * @param array
	 * @returns
	 */
	removeElement:function(element,array){
		return this.removeIdx(this.indexOf(element,array),array);
	},
	/**
	 * 更适用于对象比较的版本<br>
	 * 删除数组中的某个元素
	 * @param element
	 * @param array
	 * @returns
	 */
	objRemoveElement:function(element,array){
		return this.objRemoveIdx(this.objIndexOf(element,array),array);
	},		
	/**
	 * 判断某个字符串是不是以某个字符串开头
	 * 为何要写这个方法？因为令人无语的微信浏览器竟然不支持该方法啊！！！
	 * @param original  原有的字符串
	 * @param str    待搜索的字符串
	 * @returns {Boolean}
	 */
	startsWith:function(original,str){  
        if(str==null||str==""||original.length==0||str.length>original.length){
        	return false;  
        }  
		if(original.substr(0,str.length)==str){
			return true;  
		}else{
			return false;  
		}  
		return true;  
      },
      /**
       * 判断某个字符串是不是以某个字符串结尾
       * @param original
       * @param str
       * @returns {Boolean}
       */
	endWith:function(original,str){  
		if(str==null||str==""||original.length==0||str.length>original.length){
			return false;  
		}  
		if(original.substring(original.length-str.length)==str){
			return true;  
		}else{
			return false;  
		}  
		return true;  
	  },        
	//返回短名称
	shortName:function(str, maxLength){
		if(str && str.length>0){
			var len = str.length;
			if(len<=maxLength){
				return str;
			}else{
				return (str.substr(0,maxLength)+"...");
			}
		}else{
			return "";
		}
	},
	//检查是否是数字
	checkNumber:function(value){
		var reg = /^[1-9]\d*$/;
		if("0" == value || reg.test(value)){
			return true;
		}else{
			return false;
		}
	},
	//将指定map的指定的key初始化成空array
	initMapArray:function(){
		var len = arguments.length;//可变参数的方法
		if(len==0 || len ==1){
			return;
		}
		var map = arguments[0];
		for(var i=1;i<arguments.length;i++){
			var paramName = arguments[i];
			map[paramName]=[];
		}
	},
	//匿名
	anoym:function(name){
		if(!name || name.length ==0){
			return "***";
		}
		var length = name.length;
		if(length == 1){
			return (name +"***");
		}else{
			return (name[0]+"***"+name[name.length-1]); 
		}
	},
	/**
	 * 动态加载脚本
	 * 不推荐使用，因为这样加载的脚本会到最后才执行。如果该脚本被其他脚本依赖，则不推荐这样的方式
	 * @param script
	 */
    loadScript:function(url) {
        var content = '<script type="text/javascript" src="' + url + '"></script>';
        document.writeln(content);
    },
    /**
     * 动态加载样式
     * @param url
     */
    loadCss:function(url) {
        var content = '<link rel="stylesheet" type="text/css" href="'+url+'"/>';
        document.writeln(content);
    },
	//转换为用于显示用的字符串（去除undefined等等不合理的显示）
	toStr:function(obj){
		if(this.isNull(obj)){
			return "";
		}
		return obj;
	},
    //兼容方式获取滚动条距离顶部的距离
    getScrollTop:function(){
    	var scrollTop = document.documentElement.scrollTop || window.pageYOffset || document.body.scrollTop;
		return scrollTop;
    },
    //兼容方式设置滚动条距离顶部的距离
    setScrollTop:function(scroll_top) {
    	document.documentElement.scrollTop = scroll_top;
    	window.pageYOffset = scroll_top;
    	document.body.scrollTop = scroll_top;
    },
    /**
     * 获取两个日期的天数差(起始时间-结束时间) 
     */
    getDateDiffDays:function(startDate,endDate){  
        var startTime = new Date(Date.parse(startDate.replace(/-/g,"/"))).getTime();//得到毫秒数     
        var endTime = new Date(Date.parse(endDate.replace(/-/g,"/"))).getTime();     
        var dates = Math.abs((startTime - endTime))/(1000*60*60*24);//取绝对值     
        return dates;    
    },
	//防重复提交的事件注册表
	//用于检测某个事件是否已经在队列里先行一步了
	_timerReg:{},
	/**
	 * 防止快速点击所导致的重复提交
	 * @param id 标记，用于区分是否是同一种业务场景
	 * @param fn 回调函数
	 * @param wait 等待的毫秒数
	 * @returns
	 */
	preventDuplicateSubmission:function(id, fn, wait){
		if(!wait){
			wait=300;//默认延迟时间是300毫秒
		}
		if (NS._timerReg[id]) {
			console.log("检测到重复点击...");
	        window.clearTimeout(NS._timerReg[id]);
	        delete NS._timerReg[id];
	    }
	    return NS._timerReg[id] = window.setTimeout(function() {
	        fn();
	        delete NS._timerReg[id];
	    }, wait);
	},
	/**
	 * 将对象保存到localStorage
	 * @param key
	 * @param obj
	 */
	saveLocalStorageObj:function(key,obj){
		if(localStorage){
			localStorage[key] = JSON.stringify(obj);
		}
	},
	/**
	 * 从localStorage读取出对象
	 * @param key
	 */
	readLocalStorageObj:function(key){
		if(localStorage && localStorage[key]){
			if(NS.isNotEmpty(localStorage[key])){
				return JSON.parse(localStorage[key]);
			}
		}
		return null;
	},
	/**
	 * 指定的Url中，如果不存在某个参数，则添加该参数
	 */
	appendUrlParamIfNotExist:function(url,name,value){
		if(!this.urlHasParam(url,name)){
			//没有参数，则加上参数。有参数，则什么都不做
			url = this.urlAppendParam(url,name,value);
		}
		return url;
	},
	/**
	 * 给指定的url增加参数
	 * @param url
	 * @param name
	 * @param value
	 */
	urlAppendParam:function(url,name,value){
		url+=((url.indexOf("?")==-1?"?":"&")+name+"="+value);
		return url;
	},
	//某个url中，是否含有某个参数
	urlHasParam:function(url,name){
		var paramArray = this.getUrlParamArray(url);
		if(this.contains(name,paramArray)){
			return true;
		}else{
			return false;
		}
	},
	/**
	 * 获取指定URL的参数的数组
	 * 只要是在这个URL中出现过的参数，都可以算在里面
	 * @param url
	 * @returns {Object}
	 */
	getUrlParamArray:function(url) {
	   var retArray =[];
	   if (url.indexOf("?") != -1) {
	      var str = url.substr(url.indexOf("?")+1);
	      strs = str.split("&");
	      for(var i = 0; i < strs.length; i ++) {
//	         theRequest[strs[i].split("=")[0]]=(strs[i].split("=")[1]);
				retArray.push(strs[i].split("=")[0]);
	      }
	   }
	   return retArray;
	},
	/**
	 * 替换所有字符
	 * @param str 
	 * @param reallyDo 想要替换的
	 * @param replaceWith 替换成的
	 * @param ignoreCase 是否忽略大小写
	 * @returns
	 */
	replaceAll:function(str, reallyDo, replaceWith, ignoreCase) {  
	    if (!RegExp.prototype.isPrototypeOf(reallyDo)) {  
	        return str.replace(new RegExp(reallyDo, (ignoreCase ? "gi": "g")), replaceWith);  
	    } else {  
	        return str.replace(reallyDo, replaceWith);  
	    }  
	},
	/**
	 * 为文字包裹上一层颜色
	 */
	wrapperColor:function(original,color){
		return "<font color='"+color+"'>"+original+"</font>";
	},
	/**
	 * 将毫秒数格式化成正常显示的格式
	 */
	formatDateMillsToShow:function(millseconds){
		if(this.isEmpty(millseconds)){
			return "";
		}
		return (new Date(millseconds)).format("yyyy-MM-dd hh:mm:ss");
	},
	formatDateMillsToShowDate:function(millseconds){
		if(this.isEmpty(millseconds)){
			return "";
		}
		return (new Date(millseconds)).format("yyyy-MM-dd");
	},
	/**
	 * 一个闭包计数器工厂<br>
	 * 默认从1开始计数<br>
	 * 可以自己指定开始的计数值
	 * @param startNum
	 * @returns {Function}
	 */
	counterFnFactory:function(startNum){
		var num = 1;
		if(typeof startNum !== 'undefined'){
			num = Number(startNum);
		}
		//获取下一个数（默认返回的方法）
		var getNextNumFn = function(){
			return num++;
		}
		return getNextNumFn;
	},
	/**
	 * dom中是否存在某个selector<br>
	 * 可以存在一个或者多个
	 * @param selector
	 * @returns {Boolean}
	 */
	existsDomNode:function(selector){
		return $(selector).get().length > 0;
	},
	/**
	 * dom中是否有且只存在一个某个selector
	 * @param selector
	 * @returns {Boolean}
	 */
	existsOnlyOneDomNode:function(selector){
		return $(selector).get().length == 1;
	},
	/**
	 * 将数字格式转换成千分位
	 *@param{Object}num
	 */
	commafy:function(num) {
		if ((num + "").trim() == "") {
			return "";
		}
		if (isNaN(num)) {
			return "";
		}
		num = num + "";
		if (/^.*\..*$/.test(num)) {
			var pointIndex = num.lastIndexOf(".");
			var intPart = num.substring(0, pointIndex);
			var pointPart = num.substring(pointIndex + 1, num.length);
			intPart = intPart + "";
			var re = /(-?\d+)(\d{3})/
			while (re.test(intPart)) {
				intPart = intPart.replace(re, "$1,$2")
			}
			num = intPart + "." + pointPart;
		} else {
			num = num + "";
			var re = /(-?\d+)(\d{3})/
			while (re.test(num)) {
				num = num.replace(re, "$1,$2")
			}
		}
		return num;
	},
	/**
	 * 对千分位货币值去除千分位
	 *@param{Object}num
	 */
	delcommafy:function(num) {
		if ((num + "").trim() == "") {
			return "";
		}
		num = num.replace(/,/gi, '');
		return num;
	},
	/**
	 * 获取用户识别码<br>
	 * 该识别码每次调用的返回值均不相同<br>
	 * 该功能主要服务于统一登录系统，用于获取验证码的凭证
	 * @returns
	 */
	getIdentity : function(){
		var s = [];
		var hexDigits = "0123456789abcdef";
		for (var i = 0; i < 36; i++) {
			s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
		}
		s[14] = "4"; // bits 12-15 of the time_hi_and_version field to 0010
		s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1); // bits 6-7 of the
															// clock_seq_hi_and_reserved
															// to 01
		s[8] = s[13] = s[18] = s[23] = "-";
		var uid = s.join("");
		var identity = window.Base64.encode(uid);
		NS.currentIdentity = identity;//设置系统当前的识别码
		return identity;
	},
	getCurrentIdentity:function(){
		return NS.currentIdentity;
	},
	/**
	 * 绑定系统的回车事件
	 * @param fn
	 */
	bindGlobalEnterEvent:function(fn){
	   $(document).keypress(function(e) {  
	       if(e.which == 13) {  
	    	   fn();
	       }  
	   }); 
	},
	//从页面上提取数据到对象中
	getPageValueToObj:function(obj,toHandleArray){
//		var toHandleArray = ['insure_sex_limit','insure_min_age','insure_min_age_unit','insure_max_age','insure_max_age_unit'];
//		//获取页面中的字段信息
////		obj.insure_sex_limit = $.trim($("#insure_sex_limit").val());
////		obj.insure_min_age= $.trim($("#insure_min_age").val());
////		obj.insure_min_age_unit= $.trim($("#insure_min_age_unit").val());
////		obj.insure_max_age= $.trim($("#insure_max_age").val());
////		obj.insure_max_age_unit= $.trim($("#insure_max_age_unit").val());
		if(obj && toHandleArray){
			toHandleArray.forEach(function(e){
				obj[e] = $.trim($("#"+e).val());
			});
			return obj;
		}else{
			return null;
		}
	},
	//将对象的属性值设置到页面中
	setObjPropToPageValue:function(extraData,toHandleArray){
		if(extraData && toHandleArray){
			toHandleArray.forEach(function(e){
				$("#"+e).val(extraData[e]);
			});
		}
	}
};

/**
 * 发布、订阅模式的工厂类<br>
 * 如果想用，请实例化，例如:var pubSub = new NS.PubSub();<br>
 * 绑定事件的方法：bind(eventType, handler)、on(eventType, handler)<br>
 * 解除事件的方法：clear(eventType, handler)、off(eventType,
 * handler)，可以解除绑定某个事件，也可以解除绑定所有事件<br>
 * 查看已绑事件列表：show(eventType)、view(eventType)<br>
 * 手动触发某个事件：fire(eventType)、trigger(eventType)、emit(eventType)<br>
 * 重置该对象，移出所有绑定的事件; reset()
 */
NS.PubSub = function(){
	//是否开启日志
	this.debugMode=true;
	/**
	 * 事件处理器列表
	 * 结构如下：
	 * 	click:[functionA, functionB, functionC, ...]
	 *  blur:[functionD, functionE, functionF, ...]
	 *  ...
	 */
	this.handlers={};
	/**
	 * 为某种事件名成绑定（追加）一个新的事件监听器<br>
	 * 例如，PubSub.on("click",function(){ doSth...})
	 */
	this.bind=this.on=function(eventType, handler) {
		if (!(eventType in this.handlers)) {
			// 若还从未绑定过此种事件类型，例如click事件
			this.handlers[eventType] = [];// 为该种类的事件类型初始化一个空的处理器数组
		}
		// 此时，this.handlers[eventType]可能是一个空数组，也可能是一个已经有了若干个处理器的数组了！
		this.handlers[eventType].push(handler);
		if (this.debugMode) {
			console.log("成功绑定" + eventType + "事件！");
		}
		// 返回this，便于后续的继续的链式调用
		return this;
	};
	/**
	 * 移除事件处理器
	 */
	this.clear = this.off = function(eventType, handler) {
		if(eventType){
			//对指定的事件类型进行移除
			if (handler) {
				// 如果传参了，那么只移除指定的handler
				this.handlers[eventType].pop(handler);
				if (this.debugMode) {
					console.log("成功解除指定参数类型的" + eventType + "事件！");
				}
			} else {
				// 移除所有的handler
				this.handlers[eventType] = [];
				if (this.debugMode) {
					console.log("成功解除所有的" + eventType + "事件！");
				}
			}
		}else{
			//移除所有的事件类型
			for(var et in this.handlers){
				 if (typeof(et)=="function"){
					 //是一个function，不可以删除 
				 }else if(typeof(et) =="string"){
					 //可以删除
					 delete this.handlers[et];
				 }
			}
		}
		return this;
	};
	/**
	 * 打印所有的事件列表
	 */
	this.show=this.view=function(eventType) {
		if(eventType){
			if(this.handlers[eventType]){
				console.log("当前绑定" + eventType + "类型的事件回调函数列表为：\n"+ this.handlers[eventType]);
			}
		}else{
			for(var p in this.handlers){
				 if (typeof(p)=="string"){
					 console.log("当前绑定" + p + "类型的事件回调函数列表为：\n"+ this.handlers[p]);
				 }
			}
		}
		return this;
	};
	/**
	 * 手工触发某个事件<br>
	 * 例如:PubSub.emit("click","param1","param2");<br>
	 * 那么，就会将PubSub对象上面绑定的所有的click事件函数依次触发（调用）一遍
	 */
	this.fire = this.trigger = this.emit= function(eventType) {
		// 从第一个参数往后截取所有的后续参数的数组，例如，此例中，就是["param1","param2"]
		var handlerArgs = Array.prototype.slice.call(arguments, 1);
		if(this.handlers[eventType] && this.handlers[eventType].length>0){
			for (var i = 0; i < this.handlers[eventType].length; i++) {
				// this.handlers[eventType][i]就是那个绑定的函数，调用该函数，并传入参数，即可
				this.handlers[eventType][i].apply(this, handlerArgs);
			}
		}else{
			if (this.debugMode) {
				console.log("未绑定任何 【"+ eventType+"】 事件！忽略调用！");
			}
		}
		// 返回this，便于后续的继续的链式调用
		return this;
	};
	/**
	 * 重置整个对象，清除其上面绑定的所有的事件<br>
	 * 该方法用于重新使用该对象
	 */
	this.reset = function(){
		this.handlers={};
		console.log("对象已重置，绑定的事件已被清空！");
	}
};

//一个开箱即用的发布订阅模式的实例，你可以立刻使用它！
var PB = new NS.PubSub();

/**
 * js语言级别的扩展
 */
NS.jsLanguageExtension = function(){
	//如果还没有扩展这个语言级工具类，则扩展之
	if(!Date.prototype.format){
		//对Date的扩展，将 Date 转化为指定格式的String 
		//月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符， 
		//年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字) 
		//例子： 
		//(new Date()).format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423 
		//(new Date()).format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18 
		Date.prototype.format = function(fmt) { // author: meizz
			var o = {
				"M+" : this.getMonth() + 1, // 月份
				"d+" : this.getDate(), // 日
				"h+" : this.getHours(), // 小时
				"m+" : this.getMinutes(), // 分
				"s+" : this.getSeconds(), // 秒
				"q+" : Math.floor((this.getMonth() + 3) / 3), // 季度
				"S" : this.getMilliseconds()
			// 毫秒
			};
			if (/(y+)/.test(fmt))
				fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
						.substr(4 - RegExp.$1.length));
			for (var k in o)
				if (new RegExp("(" + k + ")").test(fmt))
					fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
							: (("00" + o[k]).substr(("" + o[k]).length)));
			return fmt;
		};
	}
	
	//其余的扩展here
}
//让该扩展即刻生效
NS.jsLanguageExtension();

/**
 * JS开发TIPS：
 * 1.框架层尽可能少的使用全局变量，具体措施为：使用命名空间模式或是函数立即自动执行
 * 
 * 2.this与上下文中可执行代码的类型有直接关系，this值在进入上下文时确定，并且在上下文运行期间永久不变。
 * this是进入上下文时确定，在一个函数代码中，这个值在每一次完全不同。
 * 
 * 3.闭包是代码块和创建该代码块的上下文中数据的结合。
 * 这里说明一下，开发人员经常错误将闭包简化理解成从父上下文中返回内部函数，甚至理解成只有匿名函数才能是闭包。
 * 再说一下，因为作用域链，使得所有的函数都是闭包（与函数类型无关： 匿名函数，FE，NFE，FD都是闭包）。
 * 这里只有一类函数除外，那就是通过Function构造器创建的函数，因为其[[Scope]]只包含全局对象。
 * 
 * 4.
 */