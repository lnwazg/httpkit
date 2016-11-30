package com.lnwazg.main;

import java.io.IOException;

import com.lnwazg.httpkit.server.HttpServer;

/**
 * 测试我的最精简的http服务器
 * @author nan.li
 * @version 2016年11月25日
 */
public class Test
{
    public static void main(String[] args)
        throws IOException
    {
        //起一个server实例
        int port = 7777;
        HttpServer server = HttpServer.bind(port);
        //            server.setBasePath("root");
        server.setContextPath("root");
        //            server.accept(FirstController.class);
        server.packageSearchAndInit("com.lnwazg.main.ctrl");
        //            server.addWatchResourceDirRoute("games", new File("J:\\games"));
        //            server.addWatchResourceDirRoute("maven", new File("D:\\maven"));
        server.autoSearchThenAddWatchResourceDirRoute();
        
        //监听在这个端口处
        server.listen();
    }
}
