package com.lnwazg.httpkit.io;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.CharEncoding;

import com.lnwazg.httpkit.HttpResponseCode;

public class HttpWriter extends BufferedWriter
{
    public static final String CONTENT_LENGTH = "Content-Length";
    
    public static final String CONTENT_TYPE = "Content-Type";
    
    public static final String CONTENT_ENCODING = "Content-Encoding";
    
    public static final String DATE = "Date";
    
    public static final String EXPIRES = "Expires";
    
    public static final String LAST_MODIFIED = "Last-modified";
    
    public static final String SERVER = "Server";
    
    /**
     * 输出流对象
     */
    public final OutputStream out;
    
    public HttpWriter(OutputStream out)
        throws UnsupportedEncodingException
    {
        super(new OutputStreamWriter(out, CharEncoding.UTF_8));
        this.out = out;
    }
    
    public void writeContentLength(long length)
        throws IOException
    {
        writeHeader(HttpWriter.CONTENT_LENGTH, length);
    }
    
    public void writeContentType(String type)
        throws IOException
    {
        writeHeader(HttpWriter.CONTENT_TYPE, type);
    }
    
    public void writeContentEncoding(String encoding)
        throws IOException
    {
        writeHeader(HttpWriter.CONTENT_ENCODING, encoding);
    }
    
    public void writeDate(Instant instant)
        throws IOException
    {
        writeHeader(HttpWriter.DATE, instant == null ? null : DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneId.of("GMT"))));
    }
    
    public void writeExpiration(Instant instant)
        throws IOException
    {
        writeHeader(HttpWriter.EXPIRES,
            instant == null ? "Never" : DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneId.of("GMT"))));
    }
    
    public void writeLastModified(Instant instant)
        throws IOException
    {
        writeHeader(HttpWriter.LAST_MODIFIED,
            instant == null ? "Never" : DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.ofInstant(instant, ZoneId.of("GMT"))));
    }
    
    public void writeServer(String server)
        throws IOException
    {
        writeHeader(HttpWriter.SERVER, server);
    }
    
    /**
     * 输出返回的结果码
     * @author lnwazg@126.com
     * @param version
     * @param code
     * @throws IOException
     */
    public void writeResponseCode(String version, HttpResponseCode code)
        throws IOException
    {
        writeHeader(String.format("%s %s %s", version, code.value(), code.message()));
    }
    
    public void writeHeader(String key, Object value)
        throws IOException
    {
        writeHeader(key, String.valueOf(value));
    }
    
    public void writeHeader(String key, String value)
        throws IOException
    {
        writeHeader(String.format("%s:%s", key, value));
    }
    
    public void writeHeader(String header)
        throws IOException
    {
        write(String.format("%s\r\n", header));
    }
    
    public void endHeader()
        throws IOException
    {
        write("\r\n");
    }
    
    /**
     * 额外的响应头信息表
     */
    Map<String, String> extraResponseHeaders = new HashMap<>();
    
    /**
     * 预输出消息体
     * @author nan.li
     * @param key
     * @param value
     */
    public void addHeaderPre(String key, String value)
    {
        this.extraResponseHeaders.put(key, value);
    }
    
    public Map<String, String> getExtraResponseHeaders()
    {
        return extraResponseHeaders;
    }
}
