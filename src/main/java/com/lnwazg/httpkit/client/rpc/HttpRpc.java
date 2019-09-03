package com.lnwazg.httpkit.client.rpc;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.log.Logs;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * 基于Http的rpc客户端
 * @author nan.li
 * @version 2019年9月2日
 */
public class HttpRpc
{
    /**
    * 单线程的线程池
    */
    public static ExecutorService singleExec = Executors.newSingleThreadExecutor();
    
    /**
     * 客户端列表
     */
    static Map<String, HttpRpc> clients = new HashMap<String, HttpRpc>();
    
    /**
     * 服务资源的位置
     */
    private String uri;
    
    public HttpRpc(String uri)
    {
        this.uri = uri;
    }
    
    /**
     * 使用某个位置的http服务资源
     * @author nan.li
     * @param uri
     * @return
     */
    public static HttpRpc use(String uri)
    {
        if (!clients.containsKey(uri))
        {
            clients.put(uri, new HttpRpc(uri));
        }
        return clients.get(uri);
    }
    
    /**
     * 引用某个interface，生产这个interface的访问代理工具
     * @author nan.li
     * @param interfaceClazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T reference(Class<T> interfaceClazz)
    {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(interfaceClazz);//设置动态代理的父类信息
        // 回调方法
        enhancer.setCallback(createMethodInterceptor(interfaceClazz));//设置方法过滤器
        // 创建代理对象
        return (T)enhancer.create();
    }
    
    private MethodInterceptor createMethodInterceptor(Class<?> interfaceClazz)
    {
        return new MethodInterceptor()
        {
            //            obj - "this", the enhanced object   obj是生成的那个代理对象实例
            //            method - intercepted Method 
            //            args - argument array; primitive types are wrapped 
            //            proxy - used to invoke super (non-intercepted method); may be called as many times as needed 
            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy)
                throws Throwable
            {
                //被代理的示例：  UserResponse processUserRequest(UserRequest userRequest);
                //1.请求参数最多只有一个。将其序列化成json
                //2.请求url的拼接方式： url + interfaceName
                //3.请求的方法为：method.getName(),放在请求头中即可
                //4.如果该method的返回类型为void，则做成异步调用（起线程）；否则做成同步调用
                //5.对于同步调用，等待调用结果（json ），然后将其返序列化成对象
                Object returnObj = null;
                //                returnObj = proxy.invokeSuper(obj, args);
                String requestUri = createRequestUri(interfaceClazz);
                String methodHead = method.getName();
                String requestParam = "";//请求体中的json参数
                Class<?> returnClass = method.getReturnType();//响应的类
                int paramCount = method.getParameterCount();
                if (paramCount > 1)
                {
                    Logs.e("invalid method call, parameter count is larger than 1!");
                    return null;
                }
                else if (paramCount == 1)
                {
                    requestParam = GsonKit.parseObject2String(args[0]);
                }
                else
                {
                    //无参数
                }
                if (returnClass == void.class)
                {
                    //异步调用
                    final String requestParamFinal = requestParam;
                    //在线程池中排队调用
                    singleExec.execute(() -> {
                        callHttp(requestUri, methodHead, requestParamFinal);
                    });
                }
                else
                {
                    //同步调用
                    String responseJson = callHttp(requestUri, methodHead, requestParam);
                    returnObj = GsonKit.parseString2Object(responseJson, returnClass);
                }
                return returnObj;
            }
        };
    }
    
    /**
     * 调用Http服务
     * @author nan.li
     * @param requestUri
     * @param methodHead
     * @param requestParam
     * @return
     */
    private String callHttp(String requestUri, String methodHead, String requestParam)
    {
        return null;
    }
    
    /**
     * 生成请求服务地址
     * @author nan.li
     * @param interfaceClazz
     * @return
     */
    private String createRequestUri(Class<?> interfaceClazz)
    {
        //http://127.0.0.1:8080/root/__httpRpc__/{interfaceName}
        return String.format("%s/__httpRpc__/%s", uri, interfaceClazz.getName());
    }
}
