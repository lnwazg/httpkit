package com.lnwazg.httpkit.proxy;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;

import com.lnwazg.httpkit.anno.JsonResponse;
import com.lnwazg.httpkit.anno.XmlResponse;
import com.lnwazg.httpkit.anno.httpmethod.CONNECT;
import com.lnwazg.httpkit.anno.httpmethod.DELETE;
import com.lnwazg.httpkit.anno.httpmethod.GET;
import com.lnwazg.httpkit.anno.httpmethod.HEAD;
import com.lnwazg.httpkit.anno.httpmethod.OPTIONS;
import com.lnwazg.httpkit.anno.httpmethod.PATCH;
import com.lnwazg.httpkit.anno.httpmethod.POST;
import com.lnwazg.httpkit.anno.httpmethod.PUT;
import com.lnwazg.httpkit.anno.httpmethod.TRACE;
import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.httpkit.filter.ControllerCallback;
import com.lnwazg.httpkit.filter.CtrlFilter;
import com.lnwazg.httpkit.filter.CtrlFilterChain;
import com.lnwazg.kit.common.model.FrontObj;
import com.lnwazg.kit.reflect.ClassKit;
import com.lnwazg.kit.singleton.B;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 控制器动态代理生成器
 * @author lnwazg@126.com
 * @version 2017年3月15日
 */
public class ControllerProxy
{
    /**
     * 用过滤器链来代理controller
     * @author lnwazg@126.com
     * @param clazz
     * @param ctrlFilterChain
     * @return
     */
    public static BaseController proxyControllerWithFilterChain(Class<BaseController> clazz, CtrlFilterChain ctrlFilterChain)
    {
        // ctrlFilterChain有可能是空的
        if (ctrlFilterChain == null || ctrlFilterChain.getFilters().size() == 0)
        {
            //既然过滤器链都是空的，那么也就是根本不用过滤器，直接使用Controller即可
            return ClassKit.newInstance(clazz);
        }
        
        //否则，注入CtrlFilterChain到单例框架
        B.s(ctrlFilterChain);
        
        //该controller自有方法（非继承过来的方法）
        Method[] methods = clazz.getDeclaredMethods();
        //是否xml格式的响应
        boolean classXmlResponse = clazz.isAnnotationPresent(XmlResponse.class);
        //是否json格式的响应
        boolean classJsonResponse = clazz.isAnnotationPresent(JsonResponse.class);
        
        //否则，为其生成动态代理实例
        MethodInterceptor methodInterceptor = new MethodInterceptor()
        {
            //            obj - "this", the enhanced object   obj是生成的那个代理对象实例
            //            method - intercepted Method 
            //            args - argument array; primitive types are wrapped 
            //            proxy - used to invoke super (non-intercepted method); may be called as many times as needed 
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                throws Throwable
            {
                //                Logs.i(String.format("Begin to invoke proxy method: %s", method.getName()));
                if (!ArrayUtils.contains(methods, method))
                {
                    //继承过来的方法
                    //                    Logs.i(String.format("继承过来的方法 %s，直接调用即可！", method.getName()));
                    return proxy.invokeSuper(obj, args);
                }
                else
                {
                    //是否xml格式的响应
                    boolean methodXmlResponse = method.isAnnotationPresent(XmlResponse.class);
                    //是否json格式的响应
                    boolean methodJsonResponse = method.isAnnotationPresent(JsonResponse.class);
                    
                    boolean GET = method.isAnnotationPresent(GET.class);
                    boolean POST = method.isAnnotationPresent(POST.class);
                    boolean CONNECT = method.isAnnotationPresent(CONNECT.class);
                    boolean DELETE = method.isAnnotationPresent(DELETE.class);
                    boolean HEAD = method.isAnnotationPresent(HEAD.class);
                    boolean OPTIONS = method.isAnnotationPresent(OPTIONS.class);
                    boolean PATCH = method.isAnnotationPresent(PATCH.class);
                    boolean PUT = method.isAnnotationPresent(PUT.class);
                    boolean TRACE = method.isAnnotationPresent(TRACE.class);
                    
                    //是否有http方法注解
                    boolean hasHttpMethodAnno = (GET || POST || CONNECT || DELETE || HEAD || OPTIONS || PATCH || PUT || TRACE);
                    
                    //自有方法
                    //Logs.i(String.format("开始用过滤器代理自有方法: %s", method.getName()));
                    //依次调用过滤器的链子，达成目的即可
                    //最终调用完毕后的回调，其实就是调用的当前的这个controller
                    ctrlFilterChain.setCallback(new ControllerCallback()
                    {
                        @Override
                        public Object call()
                        {
                            //最后调用Controller的方法
                            //此处需要根据注解声明，来拦截指定的非法请求http method
                            
                            //如果是不在注解列表内的方法，那么真正调用之前要拦截，直接返回报错信息：不支持的http method
                            
                            //请求的http方法
                            String requestHttpMethod = ((BaseController)obj).getRequestType();
                            boolean checkPass = true;
                            if (hasHttpMethodAnno)
                            {
                                //有注解时，需要做实际调用方法校验
                                switch (requestHttpMethod)
                                {
                                    case "GET":
                                        checkPass = GET;
                                        break;
                                    case "POST":
                                        checkPass = POST;
                                        break;
                                    case "CONNECT":
                                        checkPass = CONNECT;
                                        break;
                                    case "DELETE":
                                        checkPass = DELETE;
                                        break;
                                    case "HEAD":
                                        checkPass = HEAD;
                                        break;
                                    case "OPTIONS":
                                        checkPass = OPTIONS;
                                        break;
                                    case "PATCH":
                                        checkPass = PATCH;
                                        break;
                                    case "PUT":
                                        checkPass = PUT;
                                        break;
                                    case "TRACE":
                                        checkPass = TRACE;
                                        break;
                                    default:
                                        checkPass = false;
                                        break;
                                }
                            }
                            if (!checkPass)
                            {
                                ((BaseController)obj).okHtml("该Controller不支持此http调用方式： " + requestHttpMethod);
                                return null;
                            }
                            
                            Object returnObj = null;
                            try
                            {
                                returnObj = proxy.invokeSuper(obj, args);
                                
                                //方法级注解优先于类级别注解
                                if (methodXmlResponse)
                                {
                                    //将返回对象转换成xml输出
                                    ((BaseController)obj).okXml(returnObj);
                                }
                                else if (methodJsonResponse)
                                {
                                    //将返回对象转换成json输出
                                    ((BaseController)obj).okJson(returnObj);
                                }
                                //类级别注解
                                else if (classXmlResponse)
                                {
                                    //将返回对象转换成xml输出
                                    ((BaseController)obj).okXml(returnObj);
                                }
                                else if (classJsonResponse)
                                {
                                    //将返回对象转换成json输出
                                    ((BaseController)obj).okJson(returnObj);
                                }
                                //无注解
                                else
                                {
                                    //原方法返回void，已经在代码里自行做了输出，无须二次干预
                                }
                            }
                            catch (Throwable e)
                            {
                                e.printStackTrace();
                                
                                //获取Controller方法的返回对象类型
                                Class<?> returnType = method.getReturnType();
                                Object errObj = null;
                                //根据返回对象的类型做自动错误对象输出
                                if (returnType == FrontObj.class)
                                {
                                    errObj = FrontObj.newInstance().fail(10001, e.getMessage());
                                }
                                else
                                {
                                    errObj = e.getMessage();
                                }
                                
                                //方法级注解优先于类级别注解
                                if (methodXmlResponse)
                                {
                                    //将返回对象转换成xml输出
                                    ((BaseController)obj).okXml(errObj);
                                }
                                else if (methodJsonResponse)
                                {
                                    //将返回对象转换成json输出
                                    ((BaseController)obj).okJson(errObj);
                                }
                                //类级别注解
                                else if (classXmlResponse)
                                {
                                    //将返回对象转换成xml输出
                                    ((BaseController)obj).okXml(errObj);
                                }
                                else if (classJsonResponse)
                                {
                                    //将返回对象转换成json输出
                                    ((BaseController)obj).okJson(errObj);
                                }
                                //无注解
                                else
                                {
                                    //原方法返回void，已经在代码里自行做了输出，无须二次干预
                                }
                            }
                            return returnObj;
                        }
                    });
                    //取出第0个过滤器
                    CtrlFilter ctrlFilter = ctrlFilterChain.getFilters().get(0);
                    //开启过滤模式（责任链模式）
                    ctrlFilter.doFilter(ctrlFilterChain);
                    //返回过滤器链的最终调用结果
                    return ctrlFilterChain.getResult();
                }
            }
        };
        
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);//设置动态代理的父类信息
        // 回调方法
        enhancer.setCallback(methodInterceptor);//设置方法过滤器
        // 创建代理对象
        return (BaseController)enhancer.create();
    }
}
