package com.lnwazg.httpkit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketException;

import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.httpkit.CommonResponse;
import com.lnwazg.httpkit.HttpResponseCode;
import com.lnwazg.httpkit.io.HttpReader;
import com.lnwazg.httpkit.io.HttpWriter;
import com.lnwazg.httpkit.mime.MimeMappingMap;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.kit.compress.GzipBytesUtils;
import com.lnwazg.kit.http.DownloadUtils;
import com.lnwazg.kit.io.StreamUtils;

/**
 * 工具类
 * @author lnwazg@126.com
 * @version 2016年11月26日
 */
public class Utils
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
     * 采用默认的扩展名
     * @author lnwazg@126.com
     * @param reader
     * @param writer
     * @param code
     * @param msg
     */
    public static void handleMsg(HttpReader reader, HttpWriter writer, HttpResponseCode code, String msg)
    {
        handleMsg(reader, writer, code, msg, "html");
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
    public static void handleMsg(HttpReader reader, HttpWriter writer, HttpResponseCode code, String msg, String extension)
    {
        try
        {
            writer.writeResponseCode(HttpServer.VERSION, code);
            String contentType = "text/html;charset=utf-8";
            if (StringUtils.isNotEmpty(extension))
            {
                contentType = String.format("%s;charset=utf-8", MimeMappingMap.mimeMap.get(extension.toLowerCase()));
            }
            writer.writeContentType(contentType);
            if (HttpServer.gzipOutput)
            {
                writer.writeContentEncoding("gzip");
            }
            writer.writeServer(HttpServer.SERVER_NAME);
            //            writer.writeHeader("Connection", "keep-alive");
            writer.endHeader();
            writer.flush();
            
            if (msg != null)
            {
                if (HttpServer.gzipOutput)
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
            StreamUtils.close(writer, reader);
        }
    }
    
    /**
     * 重定向到某个地址
     * @author lnwazg@126.com
     * @param reader
     * @param writer
     * @param location
     */
    public static void sendRedirect(HttpReader reader, HttpWriter writer, String location)
    {
        try
        {
            writer.writeResponseCode(HttpServer.VERSION, HttpResponseCode.MOVED_PERMANENTLY);
            writer.writeHeader("Location", location);
            writer.writeServer(HttpServer.SERVER_NAME);
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
    
    /**
     * 消息处理
     * @author lnwazg@126.com
     * @param reader
     * @param writer
     * @param code
     * @param file
     */
    public static void handleMsg(HttpReader reader, HttpWriter writer, HttpResponseCode code, File file)
    {
        if (file == null || !file.exists())
        {
            CommonResponse.notFound().accept(reader, writer);
            return;
        }
        
        //尝试获取待下载文件的扩展名
        String fileName = file.getName();
        String extension = FilenameUtils.getExtension(fileName);
        FileInputStream fileInputStream = null;
        GzipCompressorOutputStream gzipCompressorOutputStream = null;
        try
        {
            writer.writeResponseCode(HttpServer.VERSION, code);
            
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
                String contentDisposition = DownloadUtils.getContentDispositionByNameAndUserAgent(fileName, userAgent);
                if (StringUtils.isNotEmpty(contentDisposition))
                {
                    writer.writeHeader("content-disposition", contentDisposition);
                }
            }
            
            if (HttpServer.gzipOutput)
            {
                writer.writeContentEncoding("gzip");
            }
            writer.writeServer(HttpServer.SERVER_NAME);
            writer.endHeader();
            writer.flush();
            
            fileInputStream = new FileInputStream(file);
            if (HttpServer.gzipOutput)
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
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            StreamUtils.close(fileInputStream, gzipCompressorOutputStream, writer, reader);
        }
    }
    
}
