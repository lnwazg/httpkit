package com.lnwazg.httpkit.controller;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.lnwazg.httpkit.HttpResponseCode;
import com.lnwazg.httpkit.io.HttpReader;
import com.lnwazg.httpkit.io.HttpWriter;
import com.lnwazg.httpkit.util.Utils;
import com.lnwazg.kit.common.model.FrontObj;
import com.lnwazg.kit.gson.GsonHelper;
import com.lnwazg.kit.url.UriParamUtils;

/**
 * Controller的基础类
 */
public class Controller
{
    /**
     * 线程本地对象
     */
    private static final ThreadLocal<ImmutablePair<HttpReader, HttpWriter>> THREAD_LOCAL = new ThreadLocal<ImmutablePair<HttpReader, HttpWriter>>();
    
    public void setThreadLocal(HttpReader reader, HttpWriter writer)
    {
        ImmutablePair<HttpReader, HttpWriter> pair = new ImmutablePair<HttpReader, HttpWriter>(reader, writer);
        THREAD_LOCAL.set(pair);
    }
    
    public HttpReader getReader()
    {
        return THREAD_LOCAL.get().getLeft();
    }
    
    public HttpWriter getWriter()
    {
        return THREAD_LOCAL.get().getRight();
    }
    
    /**
     * 获取请求参数表
     * @author nan.li
     * @return
     */
    public Map<String, String> getParamMap()
    {
        String uri = getReader().readUri();
        return UriParamUtils.URLRequest(uri);
    }
    
    public String getParam(String key)
    {
        String value = getParamMap().get(key);
        if (StringUtils.isEmpty(value))
        {
            value = "";
        }
        return value;
    }
    
    /**
     * 正常返回的响应
     * @author nan.li
     * @param msg
     */
    public void ok(String msg)
    {
        Utils.handleMsg(getReader(), getWriter(), HttpResponseCode.OK, msg);
    }
    
    /**
     * JSON形式的响应
     * @author lnwazg@126.com
     * @param json
     */
    public void okJson(String json)
    {
        Utils.handleMsg(getReader(), getWriter(), HttpResponseCode.OK, json, "json");
    }
    
    /**
     * 将对象转换成JSON形式的响应
     * @author lnwazg@126.com
     * @param obj
     */
    public void okJson(Object obj)
    {
        okJson(GsonHelper.gson.toJson(obj));
    }
    
    public void okFile(File file)
    {
        Utils.handleMsg(getReader(), getWriter(), HttpResponseCode.OK, file);
    }
    
    /**
     * 成功后输出的对象
     * @author nan.li
     * @param obj
     * @return
     */
    protected FrontObj success(Object obj)
    {
        return new FrontObj().success().setData(obj);
    }
    
    /**
     * 失败后输出的对象
     * @author nan.li
     * @param obj
     * @return
     */
    protected FrontObj fail(Object obj)
    {
        return new FrontObj().fail().setData(obj);
    }
    
    protected FrontObj fail(int errcode)
    {
        return fail(errcode, null);
    }
    
    protected FrontObj fail(int errcode, String errmsg)
    {
        return fail(null, errcode, errmsg);
    }
    
    /**
     * 失败后输出的对象<br>
     * 指定错误码和错误消息
     * @author nan.li
     * @param obj
     * @param errcode
     * @param errmsg
     * @return
     */
    protected FrontObj fail(Object obj, int errcode, String errmsg)
    {
        return new FrontObj().fail(errcode, errmsg).setData(obj);
    }
}
