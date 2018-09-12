package com.lnwazg.httpkit.proxy;

import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;

import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.httpkit.filter.ControllerCallback;
import com.lnwazg.httpkit.filter.CtrlFilter;
import com.lnwazg.httpkit.filter.CtrlFilterChain;
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
                    //自有方法
                    //                    Logs.i(String.format("开始用过滤器代理自有方法: %s", method.getName()));
                    //依次调用过滤器的链子，达成目的即可
                    //最终调用完毕后的回调，其实就是调用的当前的这个controller
                    ctrlFilterChain.setCallback(new ControllerCallback()
                    {
                        @Override
                        public void call()
                        {
                            try
                            {
                                //最后调用Controller的方法
                                proxy.invokeSuper(obj, args);
                            }
                            catch (Throwable e)
                            {
                                e.printStackTrace();
                            }
                        }
                    });
                    
                    //取出第0个过滤器
                    CtrlFilter ctrlFilter = ctrlFilterChain.getFilters().get(0);
                    //开启过滤模式（责任链模式）
                    ctrlFilter.doFilter(ctrlFilterChain);
                    return null;
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
