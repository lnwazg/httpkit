package com.lnwazg.httpkit.exchange.exchangehandler;

import java.io.IOException;

import com.lnwazg.httpkit.exchange.Exchange;
import com.lnwazg.httpkit.server.HttpServer;

/**
 * 数据交换的处理器
 *
 * @author Ian Caffey
 * @since 1.0
 */
public interface ExchangeHandler
{
    /**
     * 接收一个交换器对象，并进行处理
     * @author nan.li
     * @param exchange
     * @param httpServer 
     * @throws IOException
     */
    public void accept(Exchange exchange, HttpServer httpServer)
        throws IOException;
}
