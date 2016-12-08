package com.lnwazg.httpkit.server;

import java.io.IOException;

import com.lnwazg.httpkit.exchange.ExchangeFactory;
import com.lnwazg.httpkit.exchange.exchangehandler.ExchangeHandler;
import com.lnwazg.kit.log.Logs;

/**
 * Server的基础类<br>
 * 该server提供了自动关闭的接口
 */
public class Server implements AutoCloseable
{
    /**
     * 交换对象的工厂
     */
    private final ExchangeFactory factory;
    
    /**
     * 交换对象处理器
     */
    private final ExchangeHandler handler;
    
    public Server(ExchangeFactory factory, ExchangeHandler handler)
    {
        this.factory = factory;
        this.handler = handler;
    }
    
    //    /**
    //     * 等待处理完毕<br>
    //     * 核心就是由工厂生产一个处理器，然后由处理器去立刻处理这个生产好的对象
    //     * @author nan.li
    //     * @throws IOException
    //     */
    //    public void await()
    //        throws IOException
    //    {
    //        handler.accept(factory.create());
    //    }
    
    /**
     * 监听器<br>
     * 主要工厂还没关闭，处理器就要一直循环去处理所有的请求
     * @author nan.li
     * @param httpServer 
     * @throws IOException
     */
    public void listen(HttpServer httpServer)
    {
        Logs.i("Routing over!");
        new Thread(() -> {
            try
            {
                while (!isClosed())
                {
                    handler.accept(factory.create(), httpServer);
                    //对于现在的广泛普及的宽带连接来说，Keep-Alive也许并不像以前一样有用。web服务器会保持连接若干秒(Apache中默认15秒)，这与提高的性能相比也许会影响性能。
                    //对于单个文件被不断请求的服务(例如图片存放网站)，Keep-Alive可能会极大的影响性能，因为它在文件被请求之后还保持了不必要的连接很长时间。
                }
            }
            catch (Exception e)
            {
                //                e.printStackTrace();
                System.out.println("Server stopped.");
            }
        }).start();
    }
    
    public boolean isClosed()
        throws IOException
    {
        return factory.isClosed();
    }
    
    @Override
    public void close()
        throws Exception
    {
        factory.close();
    }
}
