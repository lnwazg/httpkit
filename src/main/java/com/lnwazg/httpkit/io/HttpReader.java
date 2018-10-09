package com.lnwazg.httpkit.io;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.httpkit.exception.MalformedRequestException;
import com.lnwazg.kit.http.url.URIEncoderDecoder;
import com.lnwazg.kit.http.url.UriParamUtils;
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
    
    public static final String HEAD = "HEAD";
    
    public static final String PUT = "PUT";
    
    public static final String DELETE = "DELETE";
    
    /**
     * 输入流
     */
    public final InputStream in;
    
    /**
     * 包装后的输入流
     */
    public final BufferedReader reader;
    
    /**
     * POST GET HEAD等
     */
    private String requestType;
    
    /**
     * /card/viewCardByUserId.do?userId=2
     */
    private String uri;
    
    /**
     * HTTP/1.1   HTTP/1.0
     */
    private String version;
    
    /**
     * 请求头表
     */
    private Map<String, String> headerMap = new HashMap<>();
    
    /**
     * cookie表
     */
    private Map<String, String> cookieMap = new HashMap<>();
    
    /**
     * 参数表
     */
    private Map<String, String> paramMap = new HashMap<>();
    
    /**
     * 增设参数表
     * @author nan.li
     * @param extraMap
     */
    public void appendExtraRequestParamMap(Map<String, String> extraMap)
    {
        //因为是增设的参数，并且还是url路径中的正则参数，那么这些参数的优先级自然是最低的，因此只能原有参数覆盖它们，而不是它们覆盖原有参数
        //覆盖参数
        extraMap.putAll(paramMap);
        //覆盖完毕后，再回设给paramMap
        paramMap = extraMap;
    }
    
    /**
     * 消息体字节码
     */
    private byte[] body;
    
    /**
     * 消息体字符串，跟body字节码一一对应
     */
    private String payloadBody;
    
    /**
     * 客户端是否支持gzip压缩输出<br>
     * 默认不支持<br>
     * 支持gzip的意思是说，服务端多了一种输出的方式：既可以gzip输出，也可以普通输出。<br>
     * 若不支持gzip输出，那么只可以普通输出。
     */
    private boolean supportGzipOutput;
    
    public HttpReader(InputStream in)
        throws UnsupportedEncodingException
    {
        this.in = in;
        this.reader = new BufferedReader(new InputStreamReader(in, CharEncoding.UTF_8));
        //依次从头读到尾
        //1.读消息签名
        if (readSignatureFully())
        {
            //2.读消息头
            if (readHeadersFully())
            {
                //3.读消息体
                if (readBodyFully())
                {
                    //解析其他的额外信息 
                    resolveExtraInfo();
                }
            }
        }
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
    
    public byte[] getBody()
    {
        return body;
    }
    
    public String getPayloadBody()
    {
        return payloadBody;
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
    
    public String getBodyAsString()
    {
        return readBodyAsString();
    }
    
    public String getRequestType()
    {
        return requestType;
    }
    
    public String getUri()
    {
        return uri;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public String readHeader(String key)
    {
        return headerMap.get(key);
    }
    
    public String getHeader(String key)
    {
        return headerMap.get(key);
    }
    
    public Map<String, String> readHeaders()
    {
        return headerMap;
    }
    
    public Map<String, String> getHeaders()
    {
        return headerMap;
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
    private boolean readSignatureFully()
    {
        // POST /path/to/call.do?param=value HTTP/1.1
        try
        {
            String line = reader.readLine();
            Logs.i(String.format("Receive request: %s", line));
            if (StringUtils.isEmpty(line))
            {
                return false;
            }
            //这样的写法实测下来性能竟然是最高的！
            int firstBlankIndex = line.indexOf(' ');
            int secondBlankIndex = line.indexOf(' ', firstBlankIndex + 1);
            this.requestType = line.substring(0, firstBlankIndex).trim();
            this.uri = line.substring(firstBlankIndex + 1, secondBlankIndex).trim();
            this.version = line.substring(secondBlankIndex + 1).trim();
            return true;
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
    private boolean readHeadersFully()
    {
        //完整的请求头格式如下：
        //[I] Host: 127.0.0.1
        //[I] Connection: keep-alive
        //[I] Pragma: no-cache
        //[I] Cache-Control: no-cache
        //[I] Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8
        //[I] Upgrade-Insecure-Requests: 1
        //[I] User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36
        //[I] Accept-Encoding: gzip, deflate, sdch
        //[I] Accept-Language: zh-CN,zh;q=0.8,en;q=0.6,ja;q=0.4
        try
        {
            String header;
            //逐行读取
            while ((header = reader.readLine()) != null)
            {
                if (StringUtils.isEmpty(header))
                {
                    break;
                }
                int colon = header.indexOf(':');
                if (colon == -1)
                {
                    throw new IllegalStateException("Unable to handle header: " + header);
                }
                headerMap.put(header.substring(0, colon).trim(), header.substring(colon + 1).trim());
            }
            return true;
        }
        catch (Exception e)
        {
            throw new MalformedRequestException("Unable to parse incoming HTTP request.", e);
        }
    }
    
    /**
     * 对消息头进一步分析
     * @author nan.li
     */
    private boolean resolveExtraInfo()
    {
        //1.根据reader判断是否需要gzip输出
        String acceptEncoding = getHeader("Accept-Encoding");
        if (StringUtils.isNotEmpty(acceptEncoding) && acceptEncoding.indexOf("gzip") != -1)
        {
            supportGzipOutput = true;
        }
        //2.解析cookie
        String cookies = getHeader("Cookie");
        // aaa=yyyy; bbb=zzz
        if (StringUtils.isNotEmpty(cookies))
        {
            cookies = cookies.trim();
            String[] pairs = cookies.split(";");
            if (ArrayUtils.isNotEmpty(pairs))
            {
                for (String pair : pairs)
                {
                    pair = pair.trim();
                    //aaa=yyyy
                    if (StringUtils.isNotEmpty(pair) && pair.indexOf("=") != -1)
                    {
                        String[] keyvalue = pair.split("=");
                        if (ArrayUtils.isNotEmpty(keyvalue) && keyvalue.length == 2)
                        {
                            cookieMap.put(keyvalue[0].trim(), keyvalue[1].trim());
                        }
                    }
                }
            }
        }
        
        //3.解析参数表
        String uri = getUri();
        if (StringUtils.isNotEmpty(uri))
        {
            //一个合法的请求，至少uri必须要是非空的
            //对uri参数先进行解码
            uri = URIEncoderDecoder.decode(uri);
            //解析出uri的参数表
            Map<String, String> ret = UriParamUtils.resolveUrlParamMap(uri);
            //默认就采用url里面的参数表
            paramMap = ret;
            if (HttpReader.POST.equals(getRequestType()))
            {
                String contentType = getHeader("Content-Type");
                //如果是post请求，那么这个参数表的内容会得到扩充
                if (StringUtils.isNotEmpty(contentType))
                {
                    //关于不同类型的请求的参数解析，这里有方案：http://blog.csdn.net/ye1992/article/details/49998511
                    //1.针对application/x-www-form-urlencoded这种编码的处理
                    if (contentType.indexOf("application/x-www-form-urlencoded") != -1)
                    {
                        String payloadBody = getPayloadBody();
                        //userId=2&last_name=Doe&action=Submit
                        //post  payload body
                        //post方式的话，会用实际数据进行覆盖参数操作
                        Map<String, String> newKv = UriParamUtils.resolveUrlParamMap("aaa.jsp?" + payloadBody);
                        //将uri里面的参数覆盖掉body里面的参数，因为uri的参数的优先级更高
                        //事实证明，当uri和body里面同时拥有某个参数的时候，uri里面的参数的优先级更高！ 2017-3-14
                        //url参数比form参数优先级更高，这一点已经通过springboot做过试验了！
                        newKv.putAll(ret);
                        //覆盖完成之后，直接返回掉
                        paramMap = newKv;
                    }
                    else
                    {
                        //2.其他形式的Content-Type参数编码，需要用对应的解析算法进行处理
                        //Content-Type参数类型总结如下：
                        //multipart/form-data  多个行  多个键值对的解析  比较复杂
                        //x-www-form-urlencoded  一行，多个键值对以&相连接
                        //raw           可以上传任意格式的文本，可以上传text、json、xml、html等，这种一般是无法明确解析的。必须指定明确的格式之后才可以正常解析
                        //Content-Type:application/octet-stream   只可以上传二进制数据，通常用来上传文件，由于没有键值，所以，一次只能上传一个文件
                        
                        //multipart/form-data与x-www-form-urlencoded区别：
                        //multipart/form-data：既可以上传文件等二进制数据，也可以上传表单键值对，只是最后会转化为一条信息；
                        //x-www-form-urlencoded：只能上传键值对，并且键值对都是间隔分开的。
                    }
                }
            }
        }
        return true;
    }
    
    public Map<String, String> getCookieMap()
    {
        return cookieMap;
    }
    
    public String getCookie(String key)
    {
        return cookieMap.get(key);
    }
    
    public Map<String, String> getParamMap()
    {
        return paramMap;
    }
    
    public String getParam(String key)
    {
        return paramMap.get(key);
    }
    
    /**
     * 完整地读消息体
     * @author lnwazg@126.com
     */
    private boolean readBodyFully()
    {
        //典型的一个post请求格式如下：
        //POST /card/viewCardByUserId.do HTTP/1.1
        //Host: 127.0.0.1
        //Connection: keep-alive
        //Content-Length: 36
        //Cache-Control: no-cache
        //Origin: chrome-extension://fhbjgbiflinjbdggehcddcbncdddomop
        //Content-Type: application/x-www-form-urlencoded
        //User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36
        //Postman-Token: 6a247e02-ce0f-c71c-6eb8-19d702d84fec
        //Accept: */*
        //Accept-Encoding: gzip, deflate
        //Accept-Language: zh-CN,zh;q=0.8,en;q=0.6,ja;q=0.4
        
        //userId=2&last_name=Doe&action=Submit
        
        //仅POST请求才要解析消息体
        //参考资料： http://stackoverflow.com/questions/3033755/reading-post-data-from-html-form-sent-to-serversocket
        //此处必须逐字符读取，不可以逐行读取（因为到了消息体传输的时候，根本没有换行符作为结尾了！）
        if (POST.equals(requestType) || PUT.equals(requestType))
        {
            //解析出Content-Type
            String contentType = readHeader("Content-Type");
            if (StringUtils.isNotEmpty(contentType))
            {
                //之所以用indexOf进行匹配，是为了能兼容“application/x-www-form-urlencoded;charset=UTF-8”类似这样的增加了编码方式的写法
                //匹配到form提交的表单的格式
                //form提交的消息体将会被以‘%’字符进行编码
                if (contentType.indexOf("application/x-www-form-urlencoded") != -1)
                {
                    //如果是最经典的form提交等方式的话，那么将payload解析成字符串是很正确的选择
                    StringBuilder payload = new StringBuilder();
                    try
                    {
                        while (reader.ready())
                        {
                            //逐字符读取
                            payload.append((char)reader.read());
                        }
                        payloadBody = payload.toString();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
                //else 针对contentType为application/json等非标准形式的参数解析，可以留待后续扩充。
                //当然也可以同样解析为字符串，然后留待后续应用层进行解析
                //else multipart  文件上传的格式解析。可能要解析成字节流数组。
                //以后再解析
            }
        }
        return true;
    }
    
    @Override
    public void close()
        throws IOException
    {
        StreamUtils.close(reader, in);
    }
}
