package com.lnwazg.httpkit.exception;

/**
 * 路由异常
 * @author lnwazg@126.com
 * @version 2016年11月26日
 */
public class RoutingException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public RoutingException()
    {
    }
    
    public RoutingException(String message)
    {
        super(message);
    }
    
    public RoutingException(String message, Throwable cause)
    {
        super(message, cause);
    }
    
    public RoutingException(Throwable cause)
    {
        super(cause);
    }
    
    public RoutingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
