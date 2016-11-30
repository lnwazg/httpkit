package com.lnwazg.httpkit.exchange;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SocketExchangeFactory implements ExchangeFactory
{
    private final ServerSocket serverSocket;
    
    public SocketExchangeFactory(ServerSocket serverSocket)
    {
        if (serverSocket == null)
        {
            throw new IllegalArgumentException();
        }
        this.serverSocket = serverSocket;
    }
    
    @Override
    public Exchange create()
        throws IOException
    {
        //while循环在阻塞在这里，直到有一个新的连接到达了，即进行新的处理！
        Socket socket = serverSocket.accept();
        //返回这个新连接的输入流与输出流对象
        return new Exchange(socket.getInputStream(), socket.getOutputStream())
        {
            @Override
            public void close()
                throws IOException
            {
                super.close();
                socket.close();
            }
        };
    }
    
    @Override
    public boolean isClosed()
        throws IOException
    {
        return serverSocket.isClosed();
    }
    
    @Override
    public void close()
        throws Exception
    {
        serverSocket.close();
    }
}
