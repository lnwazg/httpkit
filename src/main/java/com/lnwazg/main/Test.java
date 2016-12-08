package com.lnwazg.main;

import java.io.File;
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
        //        http://127.0.0.1:7777/root/base/index2
        
        //        server.addWatchResourceDirRoute("games", new File("J:\\games"));
        //        http://127.0.0.1:7777/root/games/
        
        server.addWatchResourceDirRoute("maven", new File("D:\\maven"));
        //        http://127.0.0.1:7777/root/maven/
        
        server.autoSearchThenAddWatchResourceDirRoute();
        //        http://127.0.0.1:7777/root/list
        
        server.addWatchResourceDirRoute("webStatic", new File("O:\\2012\\mavenPrj\\httpkit\\src\\main\\resources\\static"));
        //        O:\2012\mavenPrj\httpkit\src\main\resources\static
        
        server.addFreemarkerPageDirRoute("web", HttpServer.DEFAULT_WEB_RESOURCE_BASE_PATH);
        server.addFreemarkerPageDirRoute("web1", "static2/");
        server.addFreemarkerPageDirRoute("web2", "static2/");
        server.addFreemarkerPageDirRoute("web3", HttpServer.DEFAULT_WEB_RESOURCE_BASE_PATH);
        
        //监听在这个端口处
        server.listen();
    }
}
