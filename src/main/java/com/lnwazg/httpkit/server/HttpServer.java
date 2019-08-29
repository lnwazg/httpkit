package com.lnwazg.httpkit.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.time.StopWatch;

import com.lnwazg.httpkit.Constants;
import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.httpkit.exchange.ExchangeFactory;
import com.lnwazg.httpkit.exchange.SocketExchangeFactory;
import com.lnwazg.httpkit.exchange.exchangehandler.HttpExchangeHandler;
import com.lnwazg.httpkit.filter.CtrlFilter;
import com.lnwazg.httpkit.filter.CtrlFilterChain;
import com.lnwazg.httpkit.handler.route.Router;
import com.lnwazg.httpkit.outer.entity.HttpServiceSummary;
import com.lnwazg.httpkit.outer.entity.PathRemark;
import com.lnwazg.httpkit.proxy.ControllerProxy;
import com.lnwazg.kit.http.net.IpHostUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.reflect.ClassKit;
import com.lnwazg.kit.security.SecurityUtils;
import com.lnwazg.kit.singleton.B;
//import com.lnwazg.myzoo.framework.MyZooClient;

/**
 * HttpServer对象<br>
 * 支持多个服务器实例一起运行<br>
 * 全面去除HttpServer里面的static方法，才是幸福保障！<br>
 * 为何要去除static？因为要确保一个HttpServer的实例里的所有数据的生命周期都要和HttpServer保持一致，否则会造成HttpServer重启时引入脏数据！<br>
 * 需要严格区分static和非static的边界，才能编写出最高效健壮的系统代码！<br>
 * static是可共享的、只有一份，因此适用于工具类，但并不适合需要多实例的场景！
 */
public class HttpServer extends Server
{
    /**
     * 服务器启动计时器
     */
    private StopWatch stopWatch = new StopWatch();
    
    /**
     * controller的结尾，一般是xxx.do结尾，这个值一般是do
     */
    private String controllerSuffix;
    
    /**
     * 基础路径
     */
    private String basePath = "";
    
    /**
     * 路由器对象
     */
    private Router router;
    
    /**
     * 端口号
     */
    private int port;
    
    /**
     * 搜索的磁盘表
     */
    private String[] searchDisks =
        {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
    
    /**
     * 是否初始化过FreeMarker的root目录<br>
     * 仅需初始化一次即可
     */
    private boolean initFreemarkerRoot;
    
    private HttpServiceSummary httpServiceSummary;
    
    /**
     * 绑定端口号，并获得一个HttpServer的实例
     * @author nan.li
     * @param port
     * @return
     * @throws IOException
     */
    public static HttpServer bind(int port)
        throws IOException
    {
        return new HttpServer(new SocketExchangeFactory(new ServerSocket(port)), new Router(), port);
    }
    
    /**
     * 构造函数 
     * @param factory
     * @param router
     * @param port
     */
    private HttpServer(ExchangeFactory factory, Router router, int port)
    {
        super(factory, new HttpExchangeHandler(router));
        stopWatch.start();
        this.router = router;
        this.router.setHttpServer(this);
        this.port = port;
        this.httpServiceSummary = new HttpServiceSummary();//初始化http服务总结对象
        this.httpServiceSummary.setPort(port);
        Logs.i("HttpServer start generating route table...");
    }
    
    /**
     * 获取当前的端口号
     * @author nan.li
     * @return
     */
    public int getPort()
    {
        return port;
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
    public void setBasePath(String basePath)
    {
        if (StringUtils.isNotEmpty(basePath))
        {
            if (!basePath.startsWith("/"))
            {
                basePath = String.format("/%s", basePath);
            }
            this.basePath = basePath;
        }
    }
    
    public String getBasePath()
    {
        return basePath;
    }
    
    /**
     * 我的zooKeeper是否初始化成功了
     */
    public static boolean myZooInitSuccess = false;
    
    //跟MyZoo相关的参数
    private String groupName = "http";//组名称
    
    private String nodeBaseName = "http";//节点基础名称
    
    private String server;//当前的服务器地址
    
    private String nodeName;//当前的节点名称
    
    public void listen()
    {
        super.listen(this);
        //根据配置文件是否存在，决定是否注册到myzoo里面
        try
        {
            Class.forName("com.lnwazg.myzoo.framework.MyZooClient");
            Logs.i("检测到myzoo-api依赖库，开始检测加载myzoo配置文件...");
            Object myZooClient = ClassKit.newInstance("com.lnwazg.myzoo.framework.MyZooClient");
            myZooInitSuccess = (boolean)ClassKit.invokeMethod(myZooClient, "initDefaultConfig");//myZooInitSuccess = MyZooClient.initDefaultConfig();
            if (myZooInitSuccess)
            {
                //启动的时候，取出一些节点的基本信息
                server = IpHostUtils.getLocalHostIP();
                nodeName = String.format("%s-%s", nodeBaseName, SecurityUtils.md5Encode(String.format("%s_%s", server, port)));
                //向myzookeeper注册mq服务
                Map<String, String> map = Maps.asStrMap("node", nodeName, "group", groupName, "server", server, "port", port + "");
                //追加额外的信息
                appendHttpServiceList(map);
                //                MyZooClient.registerService(map);
                ClassKit.invokeMethod(myZooClient, "registerService", new Class[] {Map.class}, map);
            }
        }
        catch (ClassNotFoundException e)
        {
            Logs.i("未发现myzoo-api依赖库，因此忽略注册到myzoo注册中心。");
        }
        Logs.i("Http服务器启动完毕！端口号：" + port);
        Logs.i("HttpServer start OK! Cost " + stopWatch.getTime() + " ms! Please visit http://127.0.0.1:" + port + getBasePath()
            + "/__info__\n");
    }
    
    public void shutdown()
    {
        try
        {
            close();
            if (myZooInitSuccess)
            {
                //启动的时候，取出一些节点的基本信息
                server = IpHostUtils.getLocalHostIP();
                nodeName = String.format("%s-%s", nodeBaseName, SecurityUtils.md5Encode(String.format("%s_%s", server, port)));
                //向myzookeeper注册mq服务
                Map<String, String> map = Maps.asStrMap("node", nodeName, "group", groupName, "server", server, "port", port + "");
                //追加额外的信息
                //                MyZooClient.unregisterService(map);
                Object myZooClient = ClassKit.newInstance("com.lnwazg.myzoo.framework.MyZooClient");
                ClassKit.invokeMethod(myZooClient, "unregisterService", new Class[] {Map.class}, map);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 获取http服务列表
     * @author nan.li
     * @return
     */
    private void appendHttpServiceList(Map<String, String> map)
    {
        List<PathRemark> list = httpServiceSummary.getList();
        int i = 1;
        for (PathRemark pathRemark : list)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%s://%s%s%s%s",
                (httpServiceSummary.isProtocolHttp() ? "http" : "https"),
                server,
                ((httpServiceSummary.getPort() == 80) ? "" : (":" + httpServiceSummary.getPort())),
                pathRemark.getPath(),
                (StringUtils.isEmpty(pathRemark.getRemark()) ? "" : ("  " + pathRemark.getRemark()))));
            map.put("S" + (i++), sb.toString());
        }
    }
    
    /**
     * 自定义http服务所在的组名称
     * @author nan.li
     * @param groupName
     */
    public void setMyZooGroupName(String groupName)
    {
        this.groupName = String.format("http-%s", groupName);
        this.nodeBaseName = String.format("http-%s", groupName);
    }
    
    //============================================================================================================================================================
    //以下是具体的增加路由的一些机制
    
    /**
     * 映射具体的Controller class<br>
     * 根据类型增加路由器
     * @author lnwazg@126.com
     * @param c
     * @return
     */
    public void addControllerRoute(Class<BaseController> c)
    {
        router.addControllerRoutes(c, router);
    }
    
    //    /**
    //     * 映射具体的Controller object<br>
    //     * 根据对象增加路由器
    //     * @author lnwazg@126.com
    //     * @param controller
    //     * @return
    //     */
    //    public void addControllerRoute(Controller controller)
    //    {
    //        Router.addControllerRoutes(controller, router);
    //    }
    
    /**
     * 增加资源映射器<br>
     * 将资源文件夹映射到指定的docBasePath位置
     * @author lnwazg@126.com
     * @param docBasePath
     * @param file
     */
    public void addWatchResourceDirRoute(String docBasePath, File file)
    {
        router.addDocumentRootRoutes(docBasePath, file, router);
    }
    
    /**
     * 将资源文件夹resourcePath映射到docBasePath<br>
     * 即使打成jar包，依然可以无障碍访问里面的页面以及资源！
     * @author lnwazg@126.com
     * @param docBasePath
     * @param resourcePath
     */
    public void addFreemarkerPageDirRoute(String docBasePath, String resourcePath)
    {
        router.addFreemarkerRootRoutes(docBasePath, resourcePath, router);
    }
    
    /**
     * 自动搜索并添加搜索目录
     * @author nan.li
     */
    public void autoSearchThenAddWatchResourceDirRoute()
    {
        Logs.i("开启自适应全盘扫描文件服务器...");
        if (SystemUtils.IS_OS_WINDOWS)
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
        else
        {
            //linux，添加根目录即可
            addWatchResourceDirRoute("rootdir", new File("/"));
        }
    }
    
    /**
     * 搜索某个包下面的所有类，然后依次根据具体的类增加路由器
     * @author nan.li
     * @param packageName
     * @return
     */
    public HttpServer packageSearchAndInit(String packageName)
    {
        return packageSearchAndInit(packageName, null);
    }
    
    /**
     * 使用默认的过滤器配置，去初始化controller
     * @author nan.li
     * @param packageName
     * @return
     */
    public HttpServer packageSearchAndInitUseDefaultFilterConfigs(String packageName)
    {
        return packageSearchAndInit(packageName, initFilterConfigs());
    }
    
    /**
     * 搜索某个包下面的所有类，然后依次根据具体的类增加路由器
     * @author nan.li
     * @param packageName
     * @param ctrlFilterChain 
     */
    @SuppressWarnings("unchecked")
    public HttpServer packageSearchAndInit(String packageName, CtrlFilterChain ctrlFilterChain)
    {
        List<Class<?>> cList = ClassKit.getPackageAllClasses(packageName.trim());
        try
        {
            for (Class<?> clazz : cList)
            {
                if (BaseController.class.isAssignableFrom(clazz))
                {
                    //如果Controller是clazz的父类的话
                    //首先，先为这个类生成动态代理，然后将该代理类注入到单例表中
                    BaseController controllerProxy = ControllerProxy.proxyControllerWithFilterChain((Class<BaseController>)clazz, ctrlFilterChain);//根据接口生成动态代理类
                    B.s(clazz, controllerProxy);
                    addControllerRoute((Class<BaseController>)clazz);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return this;
    }
    
    /**
     * 自适应加载过滤器配置类列表<br>
     * 首先尝试从配置文件：filters.cfg中加载，若无配置文件，则使用系统内置的默认的过滤器列表
     * @return
     */
    public CtrlFilterChain initFilterConfigs()
    {
        try
        {
            List<String> classNameList = null;
            if (getClass().getClassLoader().getResource(Constants.FILTERS_CFG_PATH) != null)
            {
                Logs.i("开始加载Http Filter配置文件:" + Constants.FILTERS_CFG_PATH);
                classNameList = IOUtils.readLines(getClass().getClassLoader().getResourceAsStream(Constants.FILTERS_CFG_PATH), CharEncoding.UTF_8);
            }
            else
            {
                Logs.i("未发现Http Filter配置文件，将采用默认的过滤器配置！");
                classNameList = new ArrayList<>();
                //以下是内置的默认过滤器列表
                
                //跨域过滤器
                classNameList.add("com.lnwazg.httpkit.filter.common.CORSFilter");
            }
            return initFilterConfigs(classNameList);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 根据指定的类名列表，初始化过滤器配置<br>
     * 从上到下有序加载
     * @param classNameList
     * @return
     */
    public CtrlFilterChain initFilterConfigs(List<String> classNameList)
    {
        try
        {
            //过滤器类列表
            List<Class<CtrlFilter>> filterClassList = new ArrayList<>();
            if (classNameList != null && classNameList.size() > 0)
            {
                for (String classpath : classNameList)
                {
                    String trimed = StringUtils.trimToEmpty(classpath);
                    if (StringUtils.isNotBlank(trimed))
                    {
                        if (trimed.startsWith("#") || trimed.startsWith("//"))
                        {
                            //配置文件支持注释
                            continue;
                        }
                        // 尝试加载这个类
                        @SuppressWarnings("unchecked")
                        Class<CtrlFilter> filterClass = (Class<CtrlFilter>)Class.forName(trimed);
                        if (filterClass != null)
                        {
                            filterClassList.add(filterClass);
                        }
                        else
                        {
                            System.err.println(String.format("无法加载类: %s, 请检查类路径是否写错？", trimed));
                            continue;
                        }
                    }
                }
            }
            //过滤器类加载完毕了，接下来要挨个初始化了
            //先初始化过滤器链条
            CtrlFilterChain ctrlFilterChain = new CtrlFilterChain();
            //然后依次初始化各个过滤器
            if (filterClassList.size() > 0)
            {
                Logs.i("加载到的filterClassList为:" + filterClassList);
                for (Class<CtrlFilter> filterClazz : filterClassList)
                {
                    //单例实例化过滤器
                    CtrlFilter ctrlFilter = B.g(filterClazz);
                    //将实例入链
                    ctrlFilterChain.addToChain(ctrlFilter);
                }
            }
            return ctrlFilterChain;
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * 额外的响应头信息表
     */
    Map<String, String> extraResponseHeaders = new HashMap<>();
    
    /**
     * 添加额外的响应头表
     * @author nan.li
     * @param extraResponseHeaders
     */
    public void addExtraResponseHeaders(Map<String, String> extraResponseHeaders)
    {
        this.extraResponseHeaders.putAll(extraResponseHeaders);
    }
    
    public Map<String, String> getExtraResponseHeaders()
    {
        return extraResponseHeaders;
    }
    
    public String getControllerSuffix()
    {
        return controllerSuffix;
    }
    
    public void setControllerSuffix(String controllerSuffix)
    {
        this.controllerSuffix = controllerSuffix;
    }
    
    public boolean isInitFreemarkerRoot()
    {
        return initFreemarkerRoot;
    }
    
    public void setInitFreemarkerRoot(boolean initFreemarkerRoot)
    {
        this.initFreemarkerRoot = initFreemarkerRoot;
    }
    
    /**
     * /web/
     */
    private String fkBasePath;
    
    /**
     * static/<br>
     * HttpServer.DEFAULT_WEB_RESOURCE_BASE_PATH<br>
     */
    private String fkResourcePath;
    
    public void setFkBasePath(String fkBasePath)
    {
        this.fkBasePath = fkBasePath;
    }
    
    public String getFkBasePath()
    {
        return fkBasePath;
    }
    
    public void setFkResourcePath(String fkResourcePath)
    {
        this.fkResourcePath = fkResourcePath;
    }
    
    public String getFkResourcePath()
    {
        return fkResourcePath;
    }
    
    public HttpServiceSummary getHttpServiceSummary()
    {
        return httpServiceSummary;
    }
    
    public void setHttpServiceSummary(HttpServiceSummary httpServiceSummary)
    {
        this.httpServiceSummary = httpServiceSummary;
    }
    
}
