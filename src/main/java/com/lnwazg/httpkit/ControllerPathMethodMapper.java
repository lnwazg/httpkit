package com.lnwazg.httpkit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.httpkit.filter.CtrlFilter;
import com.lnwazg.httpkit.filter.CtrlFilterChain;
import com.lnwazg.httpkit.io.IOInfo;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.singleton.B;

/**
 * 映射的Controller类的路由执行器<br>
 * 内置了controller以及method的对象，以便在路由的时候执行
 */
public class ControllerPathMethodMapper
{
    /**
     * 实际将要被路由到的全路径
     */
    private final String routingPath;
    
    /**
     * 该路径将会被调用的实体类对象
     */
    private BaseController controllerObj;
    
    /**
     * 该路径将会被调用的方法
     */
    private Method controllerMethod;
    
    /**
     * 额外参数表
     */
    private Map<String, String> extraParamMap;
    
    public ControllerPathMethodMapper(String path)
    {
        this.routingPath = path;
    }
    
    public ControllerPathMethodMapper(String path, Method method, BaseController controller)
    {
        this.routingPath = path;
        this.controllerObj = controller;
        this.controllerMethod = method;
    }
    
    /**
     * 检查并注入调用参数
     * @author nan.li
     * @param method
     * @param parent
     * @return
     */
    public void setParam(Method method, BaseController controller)
    {
        this.controllerObj = controller;
        this.controllerMethod = method;
    }
    
    /**
     * 调用Controller具体的方法，获得一个handler对象
     * @param extraMap 
     * @param writer 
     * @param reader 
     */
    public void invokeControllerMethod(IOInfo ioInfo)
        throws InvocationTargetException, IllegalAccessException
    {
        ExecMgr.cachedExec.execute(
            () -> {
                //保证controller类始终只有一份，大大降低了内存的占用量
                //注入线程所需要的参数
                controllerObj.setThreadLocalIoInfo(ioInfo);
                controllerObj.setThreadLocalPageParamMap(new HashMap<>());
                
                //同样，所有的filter也要注入这些信息
                List<CtrlFilter> filters = B.g(CtrlFilterChain.class).getFilters();
                if (filters.size() > 0)
                {
                    for (CtrlFilter ctrlFilter : filters)
                    {
                        ((BaseController)ctrlFilter).setThreadLocalIoInfo(ioInfo);
                    }
                }
                try
                {
                    //令该方法可以被访问到（即便是私有方法）
                    controllerMethod.setAccessible(true);
                    //调用该方法
                    controllerMethod.invoke(controllerObj);
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
        return "Route [path=" + routingPath + ", method=" + controllerMethod + "]";
    }
    
    public void setExtraParamMap(Map<String, String> paramMap)
    {
        this.extraParamMap = paramMap;
    }
    
    public String getRoutingPath()
    {
        return routingPath;
    }
}
