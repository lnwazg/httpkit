package com.lnwazg.httpkit;

import java.util.HashMap;
import java.util.Map;

import com.lnwazg.httpkit.handler.HttpHandler;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.httpkit.util.RenderUtils;

/**
 * 通用的几种响应码的封装
 * @author lnwazg@126.com
 * @version 2016年11月26日
 */
public class CommonResponse
{
    static Map<HttpResponseCode, HttpHandler> cache = new HashMap<>();
    
    private CommonResponse()
    {
    }
    
    public static HttpHandler ok()
    {
        return CommonResponse.code(HttpResponseCode.OK);
    }
    
    public static HttpHandler ok(String msg)
    {
        return CommonResponse.code(HttpResponseCode.OK, msg);
    }
    
    /**
     * 正常的回复
     * @author lnwazg@126.com
     * @param msg
     * @return
     */
    public static HttpHandler response(String msg)
    {
        return ok(msg);
    }
    
    public static HttpHandler badRequest()
    {
        return CommonResponse.code(HttpResponseCode.BAD_REQUEST);
    }
    
    public static HttpHandler internalServerError()
    {
        return CommonResponse.code(HttpResponseCode.INTERNAL_SERVER_ERROR);
    }
    
    public static HttpHandler notFound()
    {
        return CommonResponse.code(HttpResponseCode.NOT_FOUND);
    }
    
    /**
     * 将常用的消息结果缓存化
     * @author nan.li
     * @param code
     * @return
     */
    public static HttpHandler code(HttpResponseCode code)
    {
        if (!cache.containsKey(code))
        {
            cache.put(code, code(code, String.format("<h1>%s %s. </h1><h3>Powered by %s</h3>", code.value(), code.message(), HttpServer.SERVER_NAME)));
        }
        return cache.get(code);
    }
    
    private static HttpHandler code(HttpResponseCode code, String msg)
    {
        return (ioInfo) -> {
            RenderUtils.renderHtml(ioInfo, code, msg);
        };
    }
}
