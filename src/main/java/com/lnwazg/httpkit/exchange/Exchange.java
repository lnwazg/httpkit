package com.lnwazg.httpkit.exchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 交换信息的对象<br>
 * 所谓交换信息的对象，就是两个流：一个输入流、一个输出流
 * 可以自动关闭<br>
 * @author Ian Caffey
 * @since 1.0
 */
public class Exchange implements AutoCloseable
{
    /**
     * 输入流
     */
    public final InputStream in;
    
    /**
     * 输出流
     */
    public final OutputStream out;
    
    /**
     * 当前的这个socket对象
     */
    public final Socket socket;
    
    /**
     * 构造函数 
     * @param in 参数输入流
     * @param out 参数输出流
     * @param socket 
     */
    public Exchange(InputStream in, OutputStream out, Socket socket)
    {
        this.in = in;
        this.out = out;
        this.socket = socket;
    }
    
    @Override
    public void close()
        throws IOException
    {
        in.close();
        out.close();
    }
}
