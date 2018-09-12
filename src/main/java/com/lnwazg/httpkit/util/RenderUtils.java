package com.lnwazg.httpkit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.httpkit.CommonResponse;
import com.lnwazg.httpkit.Constants;
import com.lnwazg.httpkit.HttpResponseCode;
import com.lnwazg.httpkit.handler.route.Router;
import com.lnwazg.httpkit.io.HttpReader;
import com.lnwazg.httpkit.io.HttpWriter;
import com.lnwazg.httpkit.io.IOInfo;
import com.lnwazg.kit.compress.GzipBytesUtils;
import com.lnwazg.kit.freemarker.FreeMkKit;
import com.lnwazg.kit.http.DownloadKit;
import com.lnwazg.kit.io.StreamUtils;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.mime.MimeMappingMap;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 工具类
 * @author lnwazg@126.com
 * @version 2016年11月26日
 */
public class RenderUtils
{
    /**
     * 判断该类型是否是需要直接渲染的类型
     * @author nan.li
     * @param contentType
     * @return
     */
    private static boolean isDirectContentType(String contentType)
    {
        for (String ct : MimeMappingMap.directContentTypes)
        {
            if (contentType.indexOf(ct) != -1)
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 统一消息处理<br>
     * 指定消息的扩展名<br>
     * 文本形式作统一压缩处理
     * @author lnwazg@126.com
     * @param writer
     * @param code
     * @param msg
     * @param extension
     */
    public static void renderMsg(IOInfo ioInfo, HttpResponseCode code, String msg, String extension)
    {
        HttpReader reader = ioInfo.getReader();
        HttpWriter writer = ioInfo.getWriter();
        boolean gzipOutput = reader.isSupportGzipOutput();
        try
        {
            writer.writeResponseCode(Constants.VERSION, code);
            String contentType = "text/html;charset=utf-8";
            if (StringUtils.isNotEmpty(extension))
            {
                contentType = String.format("%s;charset=utf-8", MimeMappingMap.mimeMap.get(extension.toLowerCase()));
            }
            writer.writeContentType(contentType);
            if (gzipOutput)
            {
                writer.writeContentEncoding("gzip");
            }
            writer.writeServer(Constants.SERVER_NAME);
            
            //根据启动配置输出额外的响应头
            Map<String, String> extraResponseHeaders = ioInfo.getHttpServer().getExtraResponseHeaders();
            if (MapUtils.isNotEmpty(extraResponseHeaders))
            {
                for (String key : extraResponseHeaders.keySet())
                {
                    writer.writeHeader(key, extraResponseHeaders.get(key));
                }
            }
            //将writer中预输出的内容输出
            extraResponseHeaders = writer.getExtraResponseHeaders();
            if (MapUtils.isNotEmpty(extraResponseHeaders))
            {
                for (String key : extraResponseHeaders.keySet())
                {
                    writer.writeHeader(key, extraResponseHeaders.get(key));
                }
            }
            
            //            writer.writeHeader("Connection", "keep-alive");
            writer.endHeader();
            writer.flush();
            
            if (msg != null)
            {
                if (gzipOutput)
                {
                    //采用gzip格式压缩输出
                    byte[] bs = msg.getBytes(CharEncoding.UTF_8);
                    bs = GzipBytesUtils.zip(bs);//将字节压缩输出
                    IOUtils.write(bs, writer.out);
                }
                else
                {
                    //非压缩输出的方式
                    writer.write(msg);
                }
            }
        }
        catch (SocketException e)
        {
            //Software caused connection abort: socket write error
            //忽略该类异常
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            //在此处关闭了连接，那么长连接就无法保持了
            //也就是说，请求头里面的 Connection: keep-alive 就被无视了
            StreamUtils.close(writer, reader);
            //实践证明，只有在此处关闭连接，才能正常地向客户端输出响应。也就是说，本架构目前是不支持连接的keep-alive的 
        }
    }
    
    /**
     * 渲染输出流
     * @author nan.li
     * @param ioInfo
     * @param code
     * @param inputStream
     * @param extension
     */
    public static void renderStream(IOInfo ioInfo, HttpResponseCode code, InputStream inputStream, String extension)
    {
        byte[] bs;
        try
        {
            //先转为字节码数组，再输出
            bs = IOUtils.toByteArray(inputStream);
            renderBytes(ioInfo, code, bs, extension);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 输出字节码
     * @author nan.li
     * @param ioInfo
     * @param code
     * @param bytes
     * @param extension
     */
    public static void renderBytes(IOInfo ioInfo, HttpResponseCode code, byte[] bytes, String extension)
    {
        HttpReader reader = ioInfo.getReader();
        HttpWriter writer = ioInfo.getWriter();
        boolean gzipOutput = reader.isSupportGzipOutput();
        try
        {
            writer.writeResponseCode(Constants.VERSION, code);
            String contentType = "application/octet-stream";//默认是bin类型的输出MIME
            if (StringUtils.isNotEmpty(extension))
            {
                contentType = String.format("%s;charset=utf-8", MimeMappingMap.mimeMap.get(extension.toLowerCase()));
            }
            writer.writeContentType(contentType);
            if (gzipOutput)
            {
                writer.writeContentEncoding("gzip");
            }
            writer.writeServer(Constants.SERVER_NAME);
            //根据启动配置输出额外的响应头
            Map<String, String> extraResponseHeaders = ioInfo.getHttpServer().getExtraResponseHeaders();
            if (MapUtils.isNotEmpty(extraResponseHeaders))
            {
                for (String key : extraResponseHeaders.keySet())
                {
                    writer.writeHeader(key, extraResponseHeaders.get(key));
                }
            }
            //将writer中预输出的内容输出
            extraResponseHeaders = writer.getExtraResponseHeaders();
            if (MapUtils.isNotEmpty(extraResponseHeaders))
            {
                for (String key : extraResponseHeaders.keySet())
                {
                    writer.writeHeader(key, extraResponseHeaders.get(key));
                }
            }
            //            writer.writeHeader("Connection", "keep-alive");
            writer.endHeader();
            writer.flush();
            byte[] bs = bytes;
            if (gzipOutput)
            {
                //采用gzip格式压缩输出
                bs = GzipBytesUtils.zip(bs);//将字节压缩输出
                IOUtils.write(bs, writer.out);
            }
            else
            {
                IOUtils.write(bs, writer.out);
            }
        }
        catch (SocketException e)
        {
            //Software caused connection abort: socket write error
            //忽略该类异常
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            //在此处关闭了连接，那么长连接就无法保持了
            //也就是说，请求头里面的 Connection: keep-alive 就被无视了
            StreamUtils.close(writer, reader);
            //实践证明，只有在此处关闭连接，才能正常地向客户端输出响应。也就是说，本架构目前是不支持连接的keep-alive的 
        }
    }
    
    /**
     * 统一消息处理<br>
     * 采用默认的扩展名
     * @author lnwazg@126.com
     * @param reader
     * @param writer
     * @param code
     * @param msg
     */
    public static void renderHtml(IOInfo ioInfo, HttpResponseCode code, String msg)
    {
        renderMsg(ioInfo, code, msg, "html");
    }
    
    /**
     * 消息处理
     * @author lnwazg@126.com
     * @param reader
     * @param writer
     * @param code
     * @param file
     */
    public static void renderFile(IOInfo ioInfo, HttpResponseCode code, File file)
    {
        if (file == null || !file.exists())
        {
            CommonResponse.notFound().accept(ioInfo);
            return;
        }
        HttpReader reader = ioInfo.getReader();
        HttpWriter writer = ioInfo.getWriter();
        boolean gzipOutput = reader.isSupportGzipOutput();
        //尝试获取待下载文件的扩展名
        String fileName = file.getName();
        String extension = FilenameUtils.getExtension(fileName);
        FileInputStream fileInputStream = null;
        GzipCompressorOutputStream gzipCompressorOutputStream = null;
        try
        {
            writer.writeResponseCode(Constants.VERSION, code);
            
            //默认情况下，将其作为文本类型返回（匹配不到MIME信息的时候）
            //对于chrome浏览器，其会正确判断实际类型的！所以，text/plain是最合适的类型！
            String contentType = "text/plain;charset=utf-8";
            if (StringUtils.isNotEmpty(extension))
            {
                //有扩展名，则尝试按照扩展名去查找
                String mime = MimeMappingMap.mimeMap.get(extension.toLowerCase());
                if (StringUtils.isNotEmpty(mime))
                {
                    //找到了，就按照实际的扩展名进行返回
                    contentType = String.format("%s", mime);
                }
            }
            writer.writeContentType(contentType);
            
            //如果是图片或视频，则直接在线预览，不要下载
            if (isDirectContentType(contentType))
            {
                //preview directly  直接在线预览
            }
            else
            {
                //一旦加上了下载标识符，那么就无法在线预览了，只能作为附件进行下载了！
                String userAgent = reader.readHeader("User-Agent");
                //给响应头输出下载文件的信息，方便浏览器客户端识别保存
                String contentDisposition = DownloadKit.getContentDispositionByNameAndUserAgent(fileName, userAgent);
                if (StringUtils.isNotEmpty(contentDisposition))
                {
                    writer.writeHeader("content-disposition", contentDisposition);
                }
            }
            
            if (gzipOutput)
            {
                writer.writeContentEncoding("gzip");
            }
            writer.writeServer(Constants.SERVER_NAME);
            writer.endHeader();
            writer.flush();
            
            fileInputStream = new FileInputStream(file);
            if (gzipOutput)
            {
                //字节输出的形式需要占用巨量内存，因此不推荐！
                //                byte[] bs = IOUtils.toByteArray(fileInputStream);
                //                bs = GzipBytesUtils.zip(bs);//将字节压缩输出
                //                IOUtils.write(bs, writer.out);
                gzipCompressorOutputStream = new GzipCompressorOutputStream(writer.out);
                IOUtils.copy(fileInputStream, gzipCompressorOutputStream);
            }
            else
            {
                IOUtils.copy(fileInputStream, writer.out);
            }
        }
        catch (SocketException e)
        {
            //ignore
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            StreamUtils.close(fileInputStream, gzipCompressorOutputStream, writer, reader);
        }
    }
    
    /**
     * 渲染具体的某个资源
     * @author nan.li
     * @param reader
     * @param writer
     * @param code
     * @param resourcePath  static/
     * @param subPath       page/index.ftl
     * @param fileName      index.ftl
     */
    public static void renderResource(IOInfo ioInfo, HttpResponseCode code, String resourcePath, String subPath, String fileName)
    {
        HttpReader reader = ioInfo.getReader();
        HttpWriter writer = ioInfo.getWriter();
        boolean gzipOutput = reader.isSupportGzipOutput();
        //尝试获取待下载文件的扩展名
        String extension = FilenameUtils.getExtension(fileName);
        InputStream inputStream = null;
        GzipCompressorOutputStream gzipCompressorOutputStream = null;
        try
        {
            writer.writeResponseCode(Constants.VERSION, code);
            
            //add by linan for CORS
            writer.writeHeader("Access-Control-Allow-Credentials", true);
            String origin = reader.readHeader("Origin");
            if (StringUtils.isNotEmpty(origin))
            {
                writer.writeHeader("Access-Control-Allow-Origin", origin);
            }
            
            //默认情况下，将其作为文本类型返回（匹配不到MIME信息的时候）
            //对于chrome浏览器，其会正确判断实际类型的！所以，text/plain是最合适的类型！
            String contentType = "text/plain;charset=utf-8";
            if (StringUtils.isNotEmpty(extension))
            {
                //有扩展名，则尝试按照扩展名去查找
                String mime = MimeMappingMap.mimeMap.get(extension.toLowerCase());
                if (StringUtils.isNotEmpty(mime))
                {
                    //找到了，就按照实际的扩展名进行返回
                    contentType = String.format("%s", mime);
                }
            }
            writer.writeContentType(contentType);
            //如果是图片或视频，则直接在线预览，不要下载
            if (isDirectContentType(contentType))
            {
                //preview directly  直接在线预览
            }
            else
            {
                //一旦加上了下载标识符，那么就无法在线预览了，只能作为附件进行下载了！
                String userAgent = reader.readHeader("User-Agent");
                //给响应头输出下载文件的信息，方便浏览器客户端识别保存
                String contentDisposition = DownloadKit.getContentDispositionByNameAndUserAgent(fileName, userAgent);
                if (StringUtils.isNotEmpty(contentDisposition))
                {
                    writer.writeHeader("content-disposition", contentDisposition);
                }
            }
            if (gzipOutput)
            {
                writer.writeContentEncoding("gzip");
            }
            writer.writeServer(Constants.SERVER_NAME);
            writer.endHeader();
            writer.flush();
            
            String path = String.format("%s%s", resourcePath, subPath);
            inputStream = Router.class.getClassLoader().getResourceAsStream(path);
            
            if (gzipOutput)
            {
                gzipCompressorOutputStream = new GzipCompressorOutputStream(writer.out);
                IOUtils.copy(inputStream, gzipCompressorOutputStream);
            }
            else
            {
                IOUtils.copy(inputStream, writer.out);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            StreamUtils.close(inputStream, gzipCompressorOutputStream, writer, reader);
        }
    }
    
    /**
     * freemarker配置信息表
     */
    static Map<String, Configuration> configureMap = new HashMap<>();
    
    //            System.out.println("============");
    //            System.out.println(ioInfo.getSocket());
    //            System.out.println(ioInfo.getSocket().getInetAddress());
    //            System.out.println(ioInfo.getSocket().getLocalAddress().getHostName());
    //            System.out.println(ioInfo.getSocket().getLocalAddress().getHostAddress());
    //            System.out.println(ioInfo.getSocket().getPort());
    //            System.out.println(ioInfo.getSocket().getLocalPort());
    //            System.out.println(ioInfo.getSocket().getLocalSocketAddress());
    //            System.out.println(ioInfo.getSocket().getRemoteSocketAddress());
    //            System.out.println("************\n\n");
    
    /**
     * 渲染ftl文件
     * @author nan.li
     * @param reader
     * @param writer
     * @param code
     * @param basePath      /root/web/
     * @param resourcePath  static/
     * @param subPath       page/index.ftl
     * @throws TemplateException 
     */
    public static void renderFtl(IOInfo ioInfo, HttpResponseCode code, String basePath, String resourcePath, String subPath)
    {
        renderFtl(ioInfo, code, basePath, resourcePath, subPath, null);
    }
    
    /**
     * 渲染ftl文件
     * @author nan.li
     * @param ioInfo
     * @param code
     * @param basePath      /root/web/
     * @param resourcePath  static/
     * @param subPath       page/index.ftl
     * @param extraParamMap 额外的参数表
     */
    public static void renderFtl(IOInfo ioInfo, HttpResponseCode code, String basePath, String resourcePath, String subPath, Map<String, Object> extraParamMap)
    {
        //获取Freemarker配置对象
        String key = basePath;
        if (!configureMap.containsKey(key))
        {
            configureMap.put(key, FreeMkKit.getConfigurationByClassLoader(RenderUtils.class.getClassLoader(), "/" + resourcePath));
        }
        Configuration configuration = configureMap.get(key);
        //根据名称去获取相应的模板对象（这个自身就有了缓存了，因此无需更改）
        Template template;
        try
        {
            template = configuration.getTemplate(subPath, CharEncoding.UTF_8);
            //公共参数
            //这个参数不能从缓存中获取，因为ioInfo.getSocket().getLocalAddress().getHostName()是动态变化的！
            //            Map<String, Object> map = Maps.asMap("base",
            //                String.format("http://%s:%d%s", ioInfo.getSocket().getLocalAddress().getHostName(), ioInfo.getHttpServer().getPort(), basePath));
            //            Map<String, Object> map = Maps.asMap("base",
            //                String.format("http://%s:%d%s", ioInfo.getSocket().getInetAddress().getHostAddress(), ioInfo.getHttpServer().getPort(), basePath));
            
            //从请求头Host参数中获取浏览器访问的真实地址
            Map<String, Object> map = Maps.asMap("base",
                String.format("http://%s%s", ioInfo.getReader().getHeader("Host"), basePath));
                
            //如果额外参数表非空，则加上额外参数
            if (Maps.isNotEmpty(extraParamMap))
            {
                map.putAll(extraParamMap);
            }
            
            //进行参数转换
            String msg = FreeMkKit.format(template, map);
            if (StringUtils.isNotEmpty(template.toString()))
            {
                //如果模板为空
                if (StringUtils.isEmpty(msg))
                {
                    //转换异常，则通知内部服务器异常
                    CommonResponse.internalServerError().accept(ioInfo);
                }
                else
                {
                    //转换正常
                    renderHtml(ioInfo, code, msg);
                }
            }
            else
            {
                //如果模板为空
                renderHtml(ioInfo, code, "");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 重定向到某个地址
     * @author lnwazg@126.com
     * @param reader
     * @param writer
     * @param location
     */
    public static void sendRedirect(IOInfo ioInfo, String location)
    {
        //        HttpReader reader = ioInfo.getReader();
        HttpWriter writer = ioInfo.getWriter();
        try
        {
            writer.writeResponseCode(Constants.VERSION, HttpResponseCode.MOVED_PERMANENTLY);
            writer.writeHeader("Location", location);
            writer.writeServer(Constants.SERVER_NAME);
            writer.endHeader();
            writer.flush();
        }
        catch (SocketException e)
        {
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            StreamUtils.close(writer);
        }
    }
    
}
