package com.lnwazg.httpkit.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.httpkit.exception.MalformedRequestException;
import com.lnwazg.kit.io.StreamUtils;
import com.lnwazg.kit.log.Logs;

/**
 * 请求解析类
 * @author lnwazg@126.com
 * @version 2016年11月27日
 */
public class HttpReader implements Closeable
{
    public static final String GET = "GET";
    
    public static final String POST = "POST";
    
    public static final String DELETE = "DELETE";
    
    private final InputStream in;
    
    private final BufferedReader reader;
    
    /**
     * 读取的状态
     */
    private ReadState state;
    
    private String requestType;
    
    private String uri;
    
    private String version;
    
    private Map<String, String> headers = new HashMap<>();
    
    private byte[] body;
    
    /**
     * 客户端是否支持gzip压缩输出<br>
     * 默认不支持
     */
    private boolean supportGzipOutput;
    
    public HttpReader(InputStream in)
    {
        this.in = in;
        this.reader = new BufferedReader(new InputStreamReader(in));
        this.state = ReadState.BEGIN;
        //从头读到尾
        //读标签
        readSignatureFully();
        //读消息头
        readHeadersFully();
        //读取消息体
        readBodyFully();
    }
    
    /**
     * 读取请求的消息体<br>
     * 消息体是裸字节，可以是流的形式，也可能是字符数组
     * @author nan.li
     * @return
     */
    public byte[] readBody()
    {
        return body;
    }
    
    /**
     * 以字符串的形式去读取请求的消息体
     * @author nan.li
     * @return
     */
    public String readBodyAsString()
    {
        try
        {
            return new String(body, CharEncoding.UTF_8);
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public String readRequestType()
    {
        return requestType;
    }
    
    public String readUri()
    {
        return uri;
    }
    
    public String readVersion()
    {
        return version;
    }
    
    public String readHeader(String key)
    {
        return headers.get(key);
    }
    
    public Map<String, String> readHeaders()
    {
        return headers;
    }
    
    public boolean isSupportGzipOutput()
    {
        return supportGzipOutput;
    }
    
    /**
     * 完整地读取签名区<br>
     * 包括：requestType uri version信息
     * @author lnwazg@126.com
     */
    private void readSignatureFully()
    {
        if (state != ReadState.BEGIN)
        {
            return;
        }
        try
        {
            String first = reader.readLine();
            Logs.i(String.format("Receive request: %s", first));
            if (StringUtils.isEmpty(first))
            {
                return;
            }
            int firstIndex = first.indexOf(' ');
            int secondIndex = first.indexOf(' ', firstIndex + 1);
            this.requestType = first.substring(0, firstIndex).trim();
            this.uri = first.substring(firstIndex + 1, secondIndex).trim();
            this.version = first.substring(secondIndex + 1).trim();
            state = ReadState.HEADERS;
        }
        catch (Exception e)
        {
            throw new MalformedRequestException("Unable to parse incoming HTTP request.", e);
        }
    }
    
    /**
     * 完整地读消息头
     * @author lnwazg@126.com
     */
    private void readHeadersFully()
    {
        if (state != ReadState.HEADERS)
        {
            return;
        }
        try
        {
            String header;
            while ((header = reader.readLine()) != null)
            {
                //                [I] Host: 127.0.0.1
                //                [I] Connection: keep-alive
                //                [I] Pragma: no-cache
                //                [I] Cache-Control: no-cache
                //                [I] Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
                //                [I] Upgrade-Insecure-Requests: 1
                //                [I] User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36
                //                [I] Accept-Encoding: gzip, deflate, sdch
                //                [I] Accept-Language: zh-CN,zh;q=0.8,en;q=0.6,ja;q=0.4
                if (StringUtils.isEmpty(header))
                {
                    break;
                }
                int colon = header.indexOf(':');
                if (colon == -1)
                {
                    throw new IllegalStateException("Unable to handle header: " + header);
                }
                headers.put(header.substring(0, colon).trim(), header.substring(colon + 1).trim());
            }
            resolveExtra();
            state = ReadState.BODY;
        }
        catch (Exception e)
        {
            throw new MalformedRequestException("Unable to parse incoming HTTP request.", e);
        }
    }
    
    /**
     * 解析其他的信息
     * @author nan.li
     */
    private void resolveExtra()
    {
        //根据reader判断是否需要gzip输出
        String acceptEncoding = readHeader("Accept-Encoding");
        if (StringUtils.isNotEmpty(acceptEncoding) && acceptEncoding.indexOf("gzip") != -1)
        {
            supportGzipOutput = true;
        }
    }
    
    //    /**
    //     * 根据reader判断是否需要gzip输出
    //     * @author nan.li
    //     * @param reader
    //     * @return
    //     */
    //    private static boolean isGzipOut(HttpReader reader)
    //    {
    //        String acceptEncoding = reader.readHeader("Accept-Encoding");
    //        if (StringUtils.isNotEmpty(acceptEncoding) && acceptEncoding.indexOf("gzip") != -1)
    //        {
    //            return true;
    //        }
    //        return false;
    //    }
    
    /**
     * 完整地读消息体
     * @author lnwazg@126.com
     */
    private void readBodyFully()
    {
        if (state != ReadState.BODY)
        {
            return;
        }
        
        //        try
        //        {
        //            this.body = IOUtils.toByteArray(in);
        //        }
        //        catch (IOException e)
        //        {
        //            e.printStackTrace();
        //        }
        
        this.state = ReadState.END;
    }
    
    /**
     * 阅读状态
     * @author lnwazg@126.com
     * @version 2016年11月26日
     */
    private static enum ReadState
    {
        /**
         * 开始读取
         */
        BEGIN,
        /**
        * 消息头
        */
        HEADERS,
        /**
         * 消息体
         */
        BODY,
        /**
         * 读取完毕
         */
        END
    }
    
    @Override
    public void close()
        throws IOException
    {
        StreamUtils.close(reader, in);
    }
    
}
