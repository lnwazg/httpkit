package com.lnwazg.httpkit.exchange.exchangehandler;

import java.io.IOException;

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
        ExecMgr.cachedExec.execute(() -> {
            HttpReader reader = new HttpReader(exchange.in);
            HttpWriter writer = new HttpWriter(exchange.out);
            IOInfo ioInfo = new IOInfo(reader, writer, exchange.socket, httpServer);
            try
            {
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
            catch (Exception e)
            {
                e.printStackTrace();
                CommonResponse.internalServerError().accept(ioInfo);
            }
        });
    }
}
