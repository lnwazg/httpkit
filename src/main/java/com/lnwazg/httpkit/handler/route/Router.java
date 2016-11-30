package com.lnwazg.httpkit.handler.route;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.httpkit.HttpResponseCode;
import com.lnwazg.httpkit.anno.BasePath;
import com.lnwazg.httpkit.controller.Controller;
import com.lnwazg.httpkit.exception.RoutingException;
import com.lnwazg.httpkit.handler.HttpHandler;
import com.lnwazg.httpkit.io.HttpReader;
import com.lnwazg.httpkit.io.HttpWriter;
import com.lnwazg.httpkit.page.RenderPage;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.httpkit.util.Utils;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.url.URIEncoderDecoder;
import com.lnwazg.kit.url.UriUtils;

/**
 * 路由对象
 */
public class Router implements HttpHandler
{
    /**
     * 路由表
     */
    private final Map<String, Route> routesMap = new HashMap<>();
    
    /**
     * 文档目录路由表
     */
    private final Map<String, File> docRoutesMap = new HashMap<>();
    
    @Override
    public void accept(HttpReader reader, HttpWriter writer)
    {
        try
        {
            String uri = reader.readUri();
            //找到路径      /root/base/index?fff=4343&bbb=6666
            if (StringUtils.isNotEmpty(uri))
            {
                //1.优先从routesMap中查找     
                //找到那个路由处理器对象，即可获得要调用的类对象以及方法，还有调用参数
                uri = UriUtils.removeParams(uri);//首先先做去除参数的操作
                Route route = find(uri);
                if (route == null)
                {
                    //2.查找不到，则从docRoutesMap中查找  例如     /root/games/1.doc?aaa=123
                    //key:   /root/games   value: File
                    if (matchListDrives(uri))
                    {
                        listDrives(reader, writer);
                    }
                    else
                    {
                        File f = findFileFromDocRouteMap(uri);
                        if (f != null && f.exists())
                        {
                            //开启一个线程，处理这个文件请求
                            processFile(reader, writer, f, uri);
                        }
                        else
                        {
                            //啥都没匹配到
                            throw new RoutingException("Unable to find route, uri: " + uri);
                        }
                    }
                }
                else
                {
                    //核心的业务调用
                    route.invoke(reader, writer);
                }
            }
        }
        catch (InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException("Unable to invoke route action.", e);
        }
    }
    
    private void listDrives(HttpReader reader, HttpWriter writer)
    {
        ExecMgr.cachedExec.execute(() -> {
            try
            {
                RenderPage.showDirectory(reader, writer, docRoutesMap);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    /** 
     * 如果是文件，就以文件的方式进行处理；否则，按照文件夹的方式进行处理
     * @author lnwazg@126.com
     * @param reader
     * @param writer
     * @param f
     * @param uri 
     */
    private void processFile(HttpReader reader, HttpWriter writer, File f, String uri)
    {
        ExecMgr.cachedExec.execute(() -> {
            try
            {
                if (f.isDirectory())
                {
                    //如果是文件夹的话，那么一定要将结尾加上斜杠
                    if (!uri.endsWith("/"))
                    {
                        //发送重定向
                        Utils.sendRedirect(reader, writer, uri + "/");
                    }
                    else
                    {
                        RenderPage.showDirectory(reader, writer, f, uri);
                    }
                }
                else
                {
                    Utils.handleMsg(reader, writer, HttpResponseCode.OK, f);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 符合列出驱动器列表的uri
     * @author nan.li
     * @param uri
     * @return
     */
    private boolean matchListDrives(String uri)
    {
        //uri:       /root/list?aaa=123
        //        uri = UriUtils.removeParams(uri);
        //uri:       /root/list
        if (uri.equals(String.format("%s/list", HttpServer.getBasePath())) || uri.equals(String.format("%s/index", HttpServer.getBasePath())))
        {
            return true;
        }
        return false;
    }
    
    /**
     * 从文档路由表中查找匹配的路由器
     * @author lnwazg@126.com
     * @param uri
     * @return
     */
    private File findFileFromDocRouteMap(String uri)
    {
        //2.查找不到，则从docRoutesMap中查找  例如
        //uri:       /root/games/1.doc?aaa=123
        //key:       /root/games
        //value:      File
        for (String key : docRoutesMap.keySet())
        {
            //uri:       /root/games
            //uri:       /root/games/
            if (uri.equals(key) || uri.equals(key + "/"))
            {
                //刚好相等，那么直接将那个文件夹返回即可
                return docRoutesMap.get(key);
            }
            //uri:       /root/games/1.doc?aaa=123
            else if (uri.startsWith(key + "/"))
            {
                //先去除参数
                //                uri = UriUtils.removeParams(uri);
                //uri:       /root/games/1.doc
                //key:       /root/games
                String subPath = StringUtils.removeStart(uri, key);
                //subPath:   /1.doc
                //uri解码，即可完美支持中文
                subPath = URIEncoderDecoder.decode(subPath);
                return new File(docRoutesMap.get(key), subPath);
            }
        }
        return null;
    }
    
    /**
     * 将方法放入到路由表中
     * @author nan.li
     * @param path
     * @param route
     */
    private void put(String path, Route route)
    {
        Logs.i(String.format("Adding route -> %s", route));
        if (routesMap.containsKey(path))
        {
            System.err.println("警告：存在重复的path: " + path + ", 将会仅保留最后的那个映射方法！");
        }
        routesMap.put(path, route);
    }
    
    /**
     * 文档路由表
     * @author lnwazg@126.com
     * @param basePath
     * @param file
     */
    private void putDocumentRoot(String basePath, File file)
    {
        Logs.i(String.format("Adding DOCUMENT route -> %s --> %s", basePath, file.getPath()));
        if (docRoutesMap.containsKey(basePath))
        {
            System.err.println("警告：存在重复的document root path: " + basePath + ", 将会仅保留最后的那个映射方法！");
        }
        docRoutesMap.put(basePath, file);
    }
    
    /**
     * 查找指定uri所能匹配到的Route对象
     * @author nan.li
     * @param uri
     * @return
     */
    public Route find(String uri)
    {
        return routesMap.get(uri);
    }
    
    /**
     * Controller类和对象的一对一对应表
     */
    private static Map<Class<? extends Controller>, Controller> ctrlClzObjMap = new HashMap<>();
    
    /**
     * 将控制器加入路由表
     * @author nan.li
     * @param c
     * @param router
     */
    public static void addRoutes(Class<? extends Controller> c, Router router)
    {
        if (!ctrlClzObjMap.containsKey(c))
        {
            try
            {
                ctrlClzObjMap.put(c, c.newInstance());
            }
            catch (InstantiationException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
        addRoutes(ctrlClzObjMap.get(c), router);
    }
    
    /**
     * 将控制器加入路由表
     * @author nan.li
     * @param controller
     * @param router
     */
    public static void addRoutes(Controller controller, Router router)
    {
        if (controller == null || router == null)
        {
            throw new IllegalArgumentException();
        }
        Class<?> c = controller.getClass();
        String basePath = HttpServer.getBasePath();
        //基础路径
        if (c.isAnnotationPresent(BasePath.class))
        {
            BasePath bp = c.getAnnotation(BasePath.class);
            String bpValue = bp.value();
            if (StringUtils.isNotEmpty(bpValue) && !bpValue.equals("/"))
            {
                basePath = basePath + bpValue;
            }
        }
        String finalBasePath = basePath;
        do
        {
            //对所有的声明的方法都进行映射
            Arrays.stream(c.getDeclaredMethods()).forEach(method -> {
                //方法名拼接上斜杠，就是路径
                String path = String.format("%s/%s", finalBasePath, method.getName()).trim();
                Route route = new Route(path);
                //组装调用参数
                //储存要调用的类以及方法
                //参数在实际调用的时候再传入
                route.setParam(method, controller);
                //将其放入路由表
                router.put(path, route);
            });
        } while ((c = c.getSuperclass()) != Controller.class);
    }
    
    /**
     * 往路由表中增加某个文档根目录的路由
     * @author lnwazg@126.com
     * @param basePath
     * @param file
     * @param router
     */
    public static void addDocumentRootRoutes(String docBasePath, File file, Router router)
    {
        String basePath = HttpServer.getBasePath();
        if (!docBasePath.startsWith("/"))
        {
            docBasePath = String.format("/%s", docBasePath);
        }
        basePath = String.format("%s%s", basePath, docBasePath);
        router.putDocumentRoot(basePath, file);
    }
}
