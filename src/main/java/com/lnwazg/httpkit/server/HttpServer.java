package com.lnwazg.httpkit.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import com.lnwazg.httpkit.controller.Controller;
import com.lnwazg.httpkit.exchange.ExchangeFactory;
import com.lnwazg.httpkit.exchange.SocketExchangeFactory;
import com.lnwazg.httpkit.exchange.exchangehandler.HttpExchangeHandler;
import com.lnwazg.httpkit.handler.route.Router;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.reflect.ClassKit;

/**
 * HttpServer对象<br>
 * 提供了一批静态方法，用于提供http的基础设施
 */
public class HttpServer extends Server
{
    StopWatch stopWatch = new StopWatch();
    
    /**
     * 默认的http版本号
     */
    public static final String VERSION = "HTTP/1.1";
    
    /**
     * 服务器版本号
     */
    public static final String SERVER_NAME = "LiNan/1.0.2";
    
    /**
     * 基础路径
     */
    private static String BASE_PATH = "";
    
    /**
     * 路由对象
     */
    private final Router router;
    
    private int port;
    
    /**
     * 搜索的磁盘表
     */
    String[] searchDisks = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    
    /**
     * 是否压缩输出，默认true
     */
    public static boolean gzipOutput = true;
    
    public static HttpServer bind(int port)
        throws IOException
    {
        return new HttpServer(new SocketExchangeFactory(new ServerSocket(port)), new Router(), port);
    }
    
    private HttpServer(ExchangeFactory factory, Router router, int port)
    {
        super(factory, new HttpExchangeHandler(router));
        stopWatch.start();
        this.router = router;
        this.port = port;
        Logs.i("Start routing...");
    }
    
    /**
     * 设置上下文路径
     * @author nan.li
     * @param contextPath
     */
    public void setContextPath(String contextPath)
    {
        setBasePath(contextPath);
    }
    
    /**
     * 设置上下文路径
     * @author lnwazg@126.com
     * @param basePath
     */
    public static void setBasePath(String basePath)
    {
        if (StringUtils.isNotEmpty(basePath))
        {
            if (!basePath.startsWith("/"))
            {
                basePath = String.format("/%s", basePath);
            }
            BASE_PATH = basePath;
        }
    }
    
    public static String getBasePath()
    {
        return BASE_PATH;
    }
    
    /**
     * 根据类型增加路由器
     * @author lnwazg@126.com
     * @param c
     * @return
     */
    public void addControllerRoute(Class<? extends Controller> c)
    {
        Router.addRoutes(c, router);
    }
    
    /**
     * 根据对象增加路由器
     * @author lnwazg@126.com
     * @param controller
     * @return
     */
    public void addControllerRoute(Controller controller)
    {
        Router.addRoutes(controller, router);
    }
    
    public void addWatchResourceDirRoute(String docBasePath, File file)
    {
        Router.addDocumentRootRoutes(docBasePath, file, router);
    }
    
    /**
     * 自动搜索并添加搜索目录
     * @author nan.li
     */
    public void autoSearchThenAddWatchResourceDirRoute()
    {
        for (String disk : searchDisks)
        {
            disk = disk.toLowerCase();
            File f = new File(String.format("%s:\\", disk));
            if (f.exists())
            {
                addWatchResourceDirRoute(disk, f);
            }
        }
    }
    
    /**
     * 搜素某个包下面的所有类，然后依次根据具体的类增加路由器
     * @author nan.li
     * @param packageName
     */
    @SuppressWarnings("unchecked")
    public HttpServer packageSearchAndInit(String packageName)
    {
        List<Class<?>> cList = ClassKit.getClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                if (Controller.class.isAssignableFrom(clazz))
                {
                    //如果Controller是clazz的父类的话
                    addControllerRoute((Class<Controller>)clazz);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return this;
    }
    
    @Override
    public void listen()
    {
        super.listen();
        Logs.i("Server started OK at port " + port + ", which cost " + stopWatch.getTime() + " ms!\n");
    }
    
    public void shutdown()
    {
        try
        {
            close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
