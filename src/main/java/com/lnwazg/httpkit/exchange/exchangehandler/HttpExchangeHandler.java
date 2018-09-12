package com.lnwazg.httpkit.exchange.exchangehandler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.lnwazg.httpkit.CommonResponse;
import com.lnwazg.httpkit.exception.MalformedRequestException;
import com.lnwazg.httpkit.exception.RoutingException;
import com.lnwazg.httpkit.exchange.Exchange;
import com.lnwazg.httpkit.handler.HttpHandler;
import com.lnwazg.httpkit.io.HttpReader;
import com.lnwazg.httpkit.io.HttpWriter;
import com.lnwazg.httpkit.io.IOInfo;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.kit.executor.ExecMgr;

/**
 * http数据交换的处理器
 *
 * @author Ian Caffey
 * @since 1.0
 */
public class HttpExchangeHandler implements ExchangeHandler
{
    private final HttpHandler handler;
    
    public HttpExchangeHandler(HttpHandler handler)
    {
        this.handler = handler;
    }
    
    @Override
    public void accept(Exchange exchange, HttpServer httpServer)
        throws IOException
    {
        //不控制流量，照单全收型，有可能会导致服务器撑爆了
        //                ExecMgr.cachedExec.execute(() -> {
        
        //流量控制的问题，现在也彻底解决！
        //风控系统，有效防止瞬间积压的请求过多的情况！过多的就直接抛弃！
        
        //流量防火墙控制器
        //可完美应付1w人在线压测！
        ExecMgr.trafficCtrlExec.execute(() -> {
            IOInfo ioInfo = null;
            try
            {
                HttpReader reader = new HttpReader(exchange.in);
                HttpWriter writer = new HttpWriter(exchange.out);
                ioInfo = new IOInfo(reader, writer, exchange.socket, httpServer);
                handler.accept(ioInfo);
            }
            catch (MalformedRequestException e)
            {
                CommonResponse.badRequest().accept(ioInfo);
            }
            catch (RoutingException e)
            {
                CommonResponse.notFound().accept(ioInfo);
            }
            catch (UnsupportedEncodingException e)
            {
                //不支持的编码，那么ioInfo还未来得及初始化，那么就没必要再处理了
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                CommonResponse.internalServerError().accept(ioInfo);
            }
        });
    }
}
