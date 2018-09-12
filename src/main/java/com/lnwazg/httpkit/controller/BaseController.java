package com.lnwazg.httpkit.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.Validate;

import com.lnwazg.httpkit.HttpResponseCode;
import com.lnwazg.httpkit.io.HttpReader;
import com.lnwazg.httpkit.io.HttpWriter;
import com.lnwazg.httpkit.io.IOInfo;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.httpkit.util.RenderUtils;
import com.lnwazg.kit.common.model.FrontObj;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.io.StreamUtils;
import com.lnwazg.kit.log.Logs;

/**
 * Controller的基础类
 */
public class BaseController
{
    /**
     * 线程本地对象-IO信息
     */
    private static final ThreadLocal<IOInfo> THREAD_LOCAL_IO_INFO = new ThreadLocal<IOInfo>();
    
    /**
     * 线程本地对象-ftl页面的参数表
     */
    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL_PAGE_PARAM_MAP = new ThreadLocal<Map<String, Object>>();
    
    /**
     * 将输入输出流对象设置到线程本地对象中
     * @author nan.li
     * @param ioInfo
     */
    public void setThreadLocalIoInfo(IOInfo ioInfo)
    {
        THREAD_LOCAL_IO_INFO.set(ioInfo);
    }
    
    /**
     * 将页面参数设置到本地线程对象
     * @author nan.li
     * @param map
     */
    public void setThreadLocalPageParamMap(Map<String, Object> map)
    {
        THREAD_LOCAL_PAGE_PARAM_MAP.set(map);
    }
    
    /**
     * 获取当前线程中的reader对象
     * @author nan.li
     * @return
     */
    public HttpReader getReader()
    {
        return THREAD_LOCAL_IO_INFO.get().getReader();
    }
    
    /**
     * 获取当前线程中的writer对象
     * @author nan.li
     * @return
     */
    public HttpWriter getWriter()
    {
        return THREAD_LOCAL_IO_INFO.get().getWriter();
    }
    
    /**
     * 获取当前线程绑定的IO对象
     * @author nan.li
     * @return
     */
    public IOInfo getIOInfo()
    {
        return THREAD_LOCAL_IO_INFO.get();
    }
    
    /**
     * 获取所有的头消息map
     * @author nan.li
     * @return
     */
    public Map<String, String> getHeaders()
    {
        return getReader().getHeaders();
    }
    
    /**
     * 获取某个消息头信息
     * @author nan.li
     * @param key
     * @return
     */
    public String getHeader(String key)
    {
        return getReader().getHeader(key);
    }
    
    /**
     * 获取所有的cookie信息map
     * @author nan.li
     * @return
     */
    public Map<String, String> getCookieMap()
    {
        return getReader().getCookieMap();
    }
    
    /**
     * 获取某一条请求cookie信息
     * @author nan.li
     * @param key
     * @return
     */
    public String getCookie(String key)
    {
        return getReader().getCookie(key);
    }
    
    /**
     * 获取当前访问的uri信息
     * @author nan.li
     * @return
     */
    public String getUri()
    {
        return getReader().getUri();
    }
    
    /**
     * 获取请求参数表
     * @author nan.li
     * @return
     */
    public Map<String, String> getParamMap()
    {
        return getReader().getParamMap();
    }
    
    /**
     * 获取某个请求参数的值
     * @author nan.li
     * @param key
     * @return
     */
    public String getParam(String key)
    {
        return getReader().getParam(key);
    }
    
    /**
     * 获取某个请求参数的值，并且将其作为String返回
     * @author nan.li
     * @param key
     * @return
     */
    public String getParamAsString(String key)
    {
        return getParam(key);
    }
    
    /**
     * 获取某个请求参数的值，并且将其作为Double返回
     * @author nan.li
     * @param key
     * @return
     */
    public double getParamAsDouble(String key)
    {
        Validate.notEmpty(getParam(key), String.format("%s不能为空！", key));
        return Double.valueOf(getParam(key));
    }
    
    /**
     * 获取某个请求参数的值，并且将其作为Float返回
     * @author nan.li
     * @param key
     * @return
     */
    public float getParamAsFloat(String key)
    {
        Validate.notEmpty(getParam(key), String.format("%s不能为空！", key));
        return Float.valueOf(getParam(key));
    }
    
    /**
     * 获取某个请求参数的值，并且将其作为Long返回
     * @author nan.li
     * @param key
     * @return
     */
    public long getParamAsLong(String key)
    {
        Validate.notEmpty(getParam(key), String.format("%s不能为空！", key));
        return Long.valueOf(getParam(key));
    }
    
    /**
     * 获取某个请求参数的值，并且将其作为Int返回
     * @author nan.li
     * @param key
     * @return
     */
    public int getParamAsInt(String key)
    {
        Validate.notEmpty(getParam(key), String.format("%s不能为空！", key));
        return Integer.valueOf(getParam(key));
    }
    
    /**
     * 获取某个请求参数的值，并且将其作为Byte返回
     * @author nan.li
     * @param key
     * @return
     */
    public byte getParamAsByte(String key)
    {
        Validate.notEmpty(getParam(key), String.format("%s不能为空！", key));
        return Byte.valueOf(getParam(key));
    }
    
    /**
     * 获取某个请求参数的值，并且将其作为Short返回
     * @author nan.li
     * @param key
     * @return
     */
    public short getParamAsShort(String key)
    {
        Validate.notEmpty(getParam(key), String.format("%s不能为空！", key));
        return Short.valueOf(getParam(key));
    }
    
    /**
     * 增加一条预输出消息头（会在真正输出响应的时候一起输出）
     * @author nan.li
     * @param key
     * @param value
     */
    public void addHeaderPre(String key, String value)
    {
        getWriter().addHeaderPre(key, value);
    }
    
    /**
     * 增加一条预输出消息头（会在真正输出响应的时候一起输出）
     * @author nan.li
     * @param key
     * @param value
     */
    public void addHeaderPre(String key, Object value)
    {
        addHeaderPre(key, ObjectUtils.toString(value));
    }
    
    /**
     * 增加一条预输出消息头，并且加上CORS允许的字段信息<br>
     * 
     * 参考：http://www.ruanyifeng.com/blog/2016/04/cors.html<br>
     * Access-Control-Expose-Headers<br>
     * 该字段可选。CORS请求时，XMLHttpRequest对象的getResponseHeader()方法<br>
     * 只能拿到6个基本字段：Cache-Control、Content-Language、Content-Type、Expires、Last-Modified、Pragma。<br>
     * 如果想拿到其他字段，就必须在Access-Control-Expose-Headers里面指定。上面的例子指定，getResponseHeader('FooBar')可以返回FooBar字段的值。<br>
     * 
     * 实例如下：
     * Access-Control-Expose-Headers: X-My-Custom-Header, X-Another-Custom-Header
     * @author nan.li
     * @param key
     * @param value
     */
    public void addHeaderPreWithCORS(String key, String value)
    {
        addHeaderPre(key, value);
        
        //追加额外的CORS字段信息
        String accessControlExposeHeadersKey = "Access-Control-Expose-Headers";
        Map<String, String> extraResponseHeaders = getWriter().getExtraResponseHeaders();
        String accessControlExposeHeadersValue = "";
        if (!extraResponseHeaders.containsKey(accessControlExposeHeadersKey))
        {
            accessControlExposeHeadersValue = key;
        }
        else
        {
            accessControlExposeHeadersValue = String.format("%s, %s", extraResponseHeaders.get(accessControlExposeHeadersKey), key);
        }
        addHeaderPre(accessControlExposeHeadersKey, accessControlExposeHeadersValue);
    }
    
    /**
     * 添加一条预输出cookie（会在真正输出响应的时候一起输出）
     * @author nan.li
     * @param string
     */
    public void addCookiePre(String keyValueAndOthers)
    {
        //Set-Cookie:__bsi=1785812050190439417_00_0_I_R_11_0303_C02F_N_I_I_0; expires=Thu, 16-Mar-17 02:52:46 GMT; domain=www.baidu.com; path=/
        addHeaderPre("Set-Cookie", keyValueAndOthers);
    }
    
    /**
     * 添加一条预输出cookie（会在真正输出响应的时候一起输出）
     * @author nan.li
     * @param key
     * @param value
     * @param path
     */
    public void addCookiePre(String key, String value, String path)
    {
        //Set-Cookie:__bsi=1785812050190439417_00_0_I_R_11_0303_C02F_N_I_I_0; expires=Thu, 16-Mar-17 02:52:46 GMT; domain=www.baidu.com; path=/
        addHeaderPre("Set-Cookie", String.format("%s=%s; path=%s", key, value, path));
    }
    
    /**
     * 添加一条预输出cookie（会在真正输出响应的时候一起输出）
     * @author nan.li
     * @param key
     * @param value
     * @param path
     * @param domain
     */
    public void addCookiePre(String key, String value, String path, String domain)
    {
        //Set-Cookie:__bsi=1785812050190439417_00_0_I_R_11_0303_C02F_N_I_I_0; expires=Thu, 16-Mar-17 02:52:46 GMT; domain=www.baidu.com; path=/
        addHeaderPre("Set-Cookie", String.format("%s=%s; domain=%s; path=%s", key, value, domain, path));
    }
    
    //    setCookie:function(name,value,expireDays){
    //        var Days = 36000;//(默认为永远不过期)写入之后有效期设置为100年，也就是相当于永不过期
    //        if(expireDays){
    //            //假如传了过期时间这个参数
    //            Days = expireDays;
    //        }
    //        var exp = new Date();    
    //        exp.setTime(exp.getTime() + Days*24*60*60*1000);
    //        document.cookie=(name + "="+ escape(value) + ";expires=" + exp.toGMTString()+";path=/" + this.getCookieDomain()); //设置cookie的键值对信息以及过期信息 (指定path真的很重要！) 
    //    },
    
    /**
     * 添加一条预输出cookie（会在真正输出响应的时候一起输出）
     * @author nan.li
     * @param key
     * @param value
     * @param path
     * @param domain
     * @param expireDays
     */
    public void addCookiePre(String key, String value, String path, String domain, int expireDays)
    {
        //Set-Cookie:__bsi=1785812050190439417_00_0_I_R_11_0303_C02F_N_I_I_0; expires=Thu, 16-Mar-17 02:52:46 GMT; domain=www.baidu.com; path=/
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, expireDays);
        addHeaderPre("Set-Cookie", String.format("%s=%s; expires=%s; domain=%s; path=%s", key, value, calendar.getTime().toGMTString(), domain, path));
    }
    
    /**
     * 正常返回的响应，输出一段文本信息到页面中
     * @author nan.li
     * @param msg
     */
    public void ok(String msg)
    {
        okHtml(msg);
    }
    
    /**
     * 正常返回的响应，输出一段文本信息到页面中
     * @author nan.li
     * @param msg
     */
    public void okHtml(String msg)
    {
        RenderUtils.renderHtml(getIOInfo(), HttpResponseCode.OK, msg);
    }
    
    /**
     * JSON形式的响应
     * @author lnwazg@126.com
     * @param json
     */
    public void okJson(String json)
    {
        RenderUtils.renderMsg(getIOInfo(), HttpResponseCode.OK, json, "json");
    }
    
    /**
     * 将对象转换成JSON形式的响应
     * @author lnwazg@126.com
     * @param obj
     */
    public void okJson(Object obj)
    {
        okJson(GsonKit.gson.toJson(obj));
    }
    
    /**
     * 输出一条JSON形式的响应,，默认成功的
     * @author nan.li
     */
    public void okJsonDefaultSuccess()
    {
        okJson(new FrontObj().success());
    }
    
    /**
     * 渲染一个文件
     * @author nan.li
     * @param file
     */
    public void okFile(File file)
    {
        RenderUtils.renderFile(getIOInfo(), HttpResponseCode.OK, file);
    }
    
    /**
     * 渲染一个文件，参数为文件的全路径
     * @author nan.li
     * @param fileFullPath
     */
    public void okFile(String fileFullPath)
    {
        okFile(new File(fileFullPath));
    }
    
    /**
     * 为“线程本地对象-ftl页面的参数表”增加待渲染的ftl页面的渲染参数
     * @author nan.li
     * @param key
     * @param value
     */
    public void setAttr(String key, Object value)
    {
        getThreadLocalPageParamMap().put(key, value);
    }
    
    /**
     * 获取线程本地对象-ftl页面的参数表
     * @author nan.li
     * @return
     */
    public Map<String, Object> getThreadLocalPageParamMap()
    {
        return THREAD_LOCAL_PAGE_PARAM_MAP.get();
    }
    
    /**
     * 渲染默认位置的页面
     * @author nan.li
     * @param fileFullPath
     */
    public void okPage(String fileFullPath)
    {
        okFtl(fileFullPath);
    }
    
    /**
     * 渲染默认位置的页面
     * @author nan.li
     * @param fileFullPath
     */
    public void okFtl(String fileFullPath)
    {
        okFtl(fileFullPath, getThreadLocalPageParamMap());
    }
    
    /**
     * 渲染默认位置的页面，并且指定一个参数表
     * @author nan.li
     * @param resourcePath
     * @param paramMap
     */
    public void okPage(String resourcePath, Map<String, Object> extraParamMap)
    {
        okFtl(resourcePath, extraParamMap);
    }
    
    /**
     * 渲染默认位置的页面，并且指定一个参数表
     * @author nan.li
     * @param resourcePath
     * @param paramMap
     */
    public void okFtl(String resourcePath, Map<String, Object> extraParamMap)
    {
        HttpServer httpServer = getIOInfo().getHttpServer();
        if (!httpServer.isInitFreemarkerRoot())
        {
            Logs.e("Freemarker root目录尚未初始化！请确保服务器配置过： server.addFreemarkerPageDirRoute(docBasePath, resourcePath) ！");
            okHtml("页面无法渲染，因为Freemarker root 尚未被初始化！请确保服务器配置过： server.addFreemarkerPageDirRoute(docBasePath, resourcePath) ！");
            return;
        }
        
        //一个参数表，用于替换Freemarker文件中对应的EL表达式内容
        Map<String, Object> resultAllParamMap = new HashMap<>();
        //该接口请求的url参数表、POST参数表等等
        resultAllParamMap.putAll(getParamMap());
        //指定的额外的参数表
        resultAllParamMap.putAll(extraParamMap);
        
        RenderUtils.renderFtl(getIOInfo(), HttpResponseCode.OK, httpServer.getFkBasePath(), httpServer.getFkResourcePath(), resourcePath, resultAllParamMap);
    }
    
    /**
     * 输出某个字符流的内容，一般是用作页面渲染（支持jar包内置内容输出）<br>
     * @author nan.li
     * @param inputStream
     */
    public void okCharStream(InputStream inputStream)
    {
        try
        {
            String content = IOUtils.toString(inputStream, CharEncoding.UTF_8);
            okHtml(content);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            StreamUtils.close(inputStream);
        }
    }
    
    /**
     * 输出某个字节流的内容，一般是用作文件下载（支持jar包内置内容输出），输出的内容是Octet-Stream<br>
     * @author nan.li
     * @param inputStream
     */
    public void okByteStream(InputStream inputStream)
    {
        RenderUtils.renderStream(getIOInfo(), HttpResponseCode.OK, inputStream, "bin");
    }
    
    /**
     * 输出字节数组，例如输出验证码图像数据，采用默认的bin的扩展名
     * @author nan.li
     * @param bytes
     */
    public void okBytes(byte[] bytes)
    {
        RenderUtils.renderBytes(getIOInfo(), HttpResponseCode.OK, bytes, "bin");
    }
    
    /**
     * 输出字节数组，例如输出验证码图像数据，可以自定义输出文件的扩展名信息
     * @author nan.li
     * @param bytes
     * @param extension
     */
    public void okBytes(byte[] bytes, String extension)
    {
        RenderUtils.renderBytes(getIOInfo(), HttpResponseCode.OK, bytes, extension);
    }
    
    /**
     * 原封不动输出某个文本页面的内容，一般是用作页面渲染（支持jar包内置内容输出）<br>
     * @author nan.li
     * @param resourcePath 资源路径
     */
    public void okCharStream(String resourcePath)
    {
        okCharStream(getClass().getClassLoader().getResourceAsStream(resourcePath));
    }
    
    /**
     * 原封不动输出某个文件的内容，一般是用作文件下载（支持jar包内置内容输出），输出的内容是Octet-Stream<br>
     * @author nan.li
     * @param resourcePath
     */
    public void okByteStream(String resourcePath)
    {
        okByteStream(getClass().getClassLoader().getResourceAsStream(resourcePath));
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
    
    /**
     * 失败后输出的对象，允许定义错误码
     * @author nan.li
     * @param obj
     * @return
     */
    protected FrontObj fail(int errcode)
    {
        return fail(errcode, null);
    }
    
    /**
     * 失败后输出的对象，允许定义错误码和错误消息
     * @author nan.li
     * @param obj
     * @return
     */
    protected FrontObj fail(int errcode, String errmsg)
    {
        return fail(null, errcode, errmsg);
    }
    
    /**
     * 失败后输出的对象，指定错误码和错误消息
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
