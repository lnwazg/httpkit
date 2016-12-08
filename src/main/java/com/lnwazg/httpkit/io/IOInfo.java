package com.lnwazg.httpkit.io;

import java.net.Socket;

import com.lnwazg.httpkit.server.HttpServer;

public class IOInfo
{
    private HttpReader reader;
    
    private HttpWriter writer;
    
    private Socket socket;
    
    private HttpServer httpServer;
    
    public IOInfo(HttpReader reader, HttpWriter writer, Socket socket, HttpServer httpServer)
    {
        this.reader = reader;
        this.writer = writer;
        this.socket = socket;
        this.httpServer = httpServer;
    }
    
    public HttpReader getReader()
    {
        return reader;
    }
    
    public void setReader(HttpReader reader)
    {
        this.reader = reader;
    }
    
    public HttpWriter getWriter()
    {
        return writer;
    }
    
    public void setWriter(HttpWriter writer)
    {
        this.writer = writer;
    }
    
    public Socket getSocket()
    {
        return socket;
    }
    
    public void setSocket(Socket socket)
    {
        this.socket = socket;
    }
    
    public HttpServer getHttpServer()
    {
        return httpServer;
    }
    
    public void setHttpServer(HttpServer httpServer)
    {
        this.httpServer = httpServer;
    }
}
