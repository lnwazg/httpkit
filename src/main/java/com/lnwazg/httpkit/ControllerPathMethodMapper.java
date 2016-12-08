package com.lnwazg.httpkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.lnwazg.httpkit.controller.Controller;
import com.lnwazg.httpkit.io.IOInfo;
import com.lnwazg.kit.executor.ExecMgr;

/**
 * 映射的Controller类的路由执行器<br>
 * 内置了controller以及method的对象，以便在路由的时候执行
 * @author Ian Caffey
 * @since 1.0
 */
public class ControllerPathMethodMapper
{
    /**
     * 实际将要被路由到的路径
     */
    private final String path;
    
    /**
     * 该路径将会被调用的方法
     */
    private Method method;
    
    /**
     * 该路径将会被调用的实体类对象
     */
    private Controller controller;
    
    public ControllerPathMethodMapper(String path)
    {
        this.path = path;
    }
    
    public String path()
    {
        return path;
    }
    
    /**
     * 检查并注入调用参数
     * @author nan.li
     * @param method
     * @param parent
     * @return
     */
    public void setParam(Method method, Controller controller)
    {
        this.method = method;
        //要调用的对象
        this.controller = controller;
    }
    
    /**
     * 调用Controller具体的方法，获得一个handler对象
     * @param writer 
     * @param reader 
     */
    public void invokeControllerMethod(IOInfo ioInfo)
        throws InvocationTargetException, IllegalAccessException
    {
        ExecMgr.cachedExec.execute(() -> {
            //保证controller类始终只有一份，大大降低了内存的占用量
            //注入线程所需要的参数
            controller.setThreadLocal(ioInfo);
            try
            {
                //令该方法可以被访问到（即便是私有方法）
                method.setAccessible(true);
                //调用该方法
                method.invoke(controller);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public String toString()
    {
        return "Route [path=" + path + ", method=" + method + "]";
    }
}
