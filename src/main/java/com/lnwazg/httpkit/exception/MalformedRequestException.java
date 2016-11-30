package com.lnwazg.httpkit.exception;

/**
 * 非法请求异常<br>
 * 请求格式不合法
 * @author lnwazg@126.com
 * @version 2016年11月26日
 */
public class MalformedRequestException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public MalformedRequestException()
    {
    }
    
    public MalformedRequestException(String message)
    {
        super(message);
    }
    
    public MalformedRequestException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public MalformedRequestException(Throwable cause)
    {
        super(cause);
    }
    
    public MalformedRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
