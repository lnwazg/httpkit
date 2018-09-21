package com.lnwazg.httpkit.handler.route;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;

import com.lnwazg.httpkit.CommonResponse;
import com.lnwazg.httpkit.ControllerPathMethodMapper;
import com.lnwazg.httpkit.HttpResponseCode;
import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.httpkit.exception.RoutingException;
import com.lnwazg.httpkit.handler.HttpHandler;
import com.lnwazg.httpkit.io.HttpReader;
import com.lnwazg.httpkit.io.IOInfo;
import com.lnwazg.httpkit.page.RenderPage;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.httpkit.util.RenderUtils;
import com.lnwazg.kit.anno.Anno;
import com.lnwazg.kit.controllerpattern.Controller;
import com.lnwazg.kit.controllerpattern.RequestMapping;
import com.lnwazg.kit.executor.ExecMgr;
import com.lnwazg.kit.gson.GsonKit;
import com.lnwazg.kit.http.url.URIEncoderDecoder;
import com.lnwazg.kit.http.url.UriParamUtils;
import com.lnwazg.kit.log.Logs;
import com.lnwazg.kit.map.Maps;
import com.lnwazg.kit.singleton.B;
import com.lnwazg.kit.str.UrlKit;

/**
 * 路由对象<br>
 * 路由的优先级从高到低顺序排列如下：<br>
 * 1. 自定义的Controller<br>
 * 2. freeMarker ftl文件解析<br>
 * 3. 文件浏览器解析<br>
 */
public class Router implements HttpHandler
{
    /**
     * 控制器的路由表<br>
     * key为控制器类的方法名+前缀的contextPath<br>
     * value为对应的处理器类以及相应的方法的包装—->Route对象
     */
    private final Map<String, ControllerPathMethodMapper> controllerRoutesMap = new HashMap<>();
    
    /**
     * 正则匹配的路由附表<br>
     * key:     ^/card/users/\w+/topics/\w+.do$ <br>
     * value:   /card/users/{userId}/topics/{topicId}.do     ^/card/users/(\w+)/topics/(\w+).do$       [userId,topicId]
     */
    private final Map<String, RegexMapDetail> regexUrlToDetailMap = new HashMap<>();
    
    /**
     * 文档文件夹目录路由表<br>
     * key为起始路径，用作key匹配的时候用，例如：/root/maven/<br>
     * value为对应的File对象，例如：new File("D:\\maven")
     */
    private final Map<String, File> docDirectoryRoutesMap = new HashMap<>();
    
    /**
     * Freemarker的路由表<br>
     * key为起始路径，用作key匹配的时候用，例如：/root/maven/<br>
     * value为具体的资源目录，例如：static/<br>
     * 为何此处value一定是一个相对路径？因为要兼容当资源全部打包成jar包之后，依然可以可靠服务的情况！<br>
     */
    private final Map<String, String> freemarkerRoutesMap = new HashMap<>();
    
    /**
     * Controller类和对象的一对一对应表
     */
    private Map<Class<? extends BaseController>, BaseController> controllerClassObjectMap = new HashMap<>();
    
    private HttpServer httpServer;
    
    public void setHttpServer(HttpServer httpServer)
    {
        this.httpServer = httpServer;
    }
    
    @Override
    public void accept(IOInfo ioInfo)
    {
        HttpReader reader = ioInfo.getReader();
        try
        {
            String uri = reader.getUri();
            //找到路径      /root/base/index?fff=4343&bbb=6666
            if (StringUtils.isNotEmpty(uri))
            {
                //多斜杠兼容
                //例如将： http://127.0.0.1:8080/////__info__ 自动翻译成  http://127.0.0.1:8080/__info__
                uri = UrlKit.cleanUri(uri);
                
                //此处的处理含有几个优先级，因此可能会有些许性能的损失！但是无妨，因为性能损失换来了系统服务弹性的提升！
                //1.优先从routesMap中查找     
                //找到那个路由处理器对象，即可获得要调用的类对象以及方法，还有调用参数
                uri = UriParamUtils.removeParams(uri);//首先先做去除参数的操作
                ImmutablePair<ControllerPathMethodMapper, Map<String, String>> pair = findFromControllerMap(uri);
                if (pair != null)
                {
                    //2.routesMap中找到数据了，此处开始核心的业务调用
                    ControllerPathMethodMapper controllerPathMethodMapper = pair.getLeft();
                    Map<String, String> extraMap = pair.getRight();
                    
                    //额外的参数，是从url中的正则匹配所提取到的
                    //类似如下的url:   /card/users/{userId}/topics/{topicId}.do     可以提取到: userId=155,topicId=200
                    if (Maps.isNotEmpty(extraMap))
                    {
                        //将这些额外的url正则参数也放入到参数表中
                        ioInfo.appendExtraRequestParamMap(extraMap);
                    }
                    controllerPathMethodMapper.invokeControllerMethod(ioInfo);
                }
                else
                {
                    //3.查找不到，则从docRoutesMap中查找  例如     /root/games/1.doc?aaa=123
                    //key:   /root/games   value: File
                    //key:   /root/list
                    
                    //4.如果是list请求，列出根目录的文件服务器列表
                    if (matchDirRootListDrives(uri, ioInfo))
                    {
                        listDirRootDrives(ioInfo);
                    }
                    //如果是__info__请求，那么列出当前网站的元数据信息页
                    else if (matchMetaInfoRequestPath(uri, ioInfo))
                    {
                        describeWebsiteMetadata(ioInfo);
                    }
                    else
                    {
                        //5.首先尝试从ftl映射目录中查找资源
                        //key:   /root/web/page/index.ftl
                        ImmutableTriple<String, String, String> baseAndSubPath = findResourcePathFromFreemarkerRouteMap(uri);
                        if (baseAndSubPath != null)
                        {
                            //6.ftl网站映射处理
                            processFtl(ioInfo, baseAndSubPath.getLeft(), baseAndSubPath.getMiddle(), baseAndSubPath.getRight(), uri);
                        }
                        else
                        {
                            //7.从文件服务器中（文档目录映射表中）查找
                            //key:   /root/games  
                            //value: File
                            File file = findFileFromDocRouteMap(uri);
                            if (file != null && file.exists())
                            {
                                //7.开启一个线程，处理这个文件请求
                                processFile(ioInfo, file, uri);
                            }
                            else
                            {
                                //8.最终啥都没匹配到
                                throw new RoutingException("Unable to find route, uri: " + uri);
                            }
                        }
                    }
                }
            }
        }
        catch (InvocationTargetException | IllegalAccessException e)
        {
            throw new RuntimeException("Unable to invoke route action.", e);
        }
    }
    
    /**
     * 描述网站元信息
     * @author nan.li
     * @param ioInfo
     */
    private void describeWebsiteMetadata(IOInfo ioInfo)
    {
        ExecMgr.cachedExec.execute(() -> {
            try
            {
                RenderUtils.renderMsg(ioInfo, HttpResponseCode.OK, GsonKit.gson.toJson(ioInfo.getHttpServer().getHttpServiceSummary()), "json");
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 若符合元数据描述页面<br>
     * 这个页面一般是秘密的、私有的、不对外开放的，但是便于内部开发使用的
     * @author nan.li
     * @param uri
     * @param ioInfo
     * @return
     */
    private boolean matchMetaInfoRequestPath(String uri, IOInfo ioInfo)
    {
        if (uri.equals(String.format("%s/__info__", ioInfo.getHttpServer().getBasePath())))
        {
            return true;
        }
        return false;
    }
    
    private void listDirRootDrives(IOInfo ioInfo)
    {
        ExecMgr.cachedExec.execute(() -> {
            try
            {
                RenderPage.showDirectory(ioInfo, docDirectoryRoutesMap);
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
    private void processFile(IOInfo ioInfo, File f, String uri)
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
                        RenderUtils.sendRedirect(ioInfo, uri + "/");
                    }
                    else
                    {
                        RenderPage.showDirectory(ioInfo, f, uri);
                    }
                }
                else
                {
                    RenderUtils.renderFile(ioInfo, HttpResponseCode.OK, f);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 渲染ftl文件
     * @author nan.li
     * @param reader
     * @param writer
     * @param basePath        /root/web/
     * @param resourcePath    static/
     * @param subPath         page/index.ftl
     * @param uri
     */
    private void processFtl(IOInfo ioInfo, String basePath, String resourcePath, String subPath, String uri)
    {
        ExecMgr.cachedExec.execute(() -> {
            //判断资源是否存在
            if (existResource(resourcePath, subPath))
            {
                String fileName = getFileNameFromSubPath(subPath);
                if (StringUtils.isNotEmpty(fileName) && FilenameUtils.getExtension(fileName).toLowerCase().equals("ftl"))
                {
                    RenderUtils.renderFtl(ioInfo, HttpResponseCode.OK, basePath, resourcePath, subPath);
                }
                else
                {
                    RenderUtils.renderResource(ioInfo, HttpResponseCode.OK, resourcePath, subPath, fileName);
                }
            }
            else
            {
                CommonResponse.notFound().accept(ioInfo);
            }
        });
    }
    
    /**
     * 获取文件名
     * @author nan.li
     * @param subPath  page/index.ftl
     * @return
     */
    private String getFileNameFromSubPath(String subPath)
    {
        int index = subPath.lastIndexOf("/");
        if (index != -1)
        {
            return subPath.substring(index + 1);
        }
        return null;
    }
    
    /**
     * 判断某个资源是否存在
     * @author nan.li
     * @param resourcePath  static/
     * @param subPath  page/index.ftl
     * @return
     */
    private boolean existResource(String resourcePath, String subPath)
    {
        String path = String.format("%s%s", resourcePath, subPath);
        return Router.class.getClassLoader().getResourceAsStream(path) != null;
    }
    
    /**
     * 符合列出驱动器列表的uri<br>
     * 例如如下：<br>
     * http://127.0.0.1/list<br>
     * http://127.0.0.1/index
     * @author nan.li
     * @param uri
     * @param ioInfo 
     * @return
     */
    private boolean matchDirRootListDrives(String uri, IOInfo ioInfo)
    {
        if (uri.equals(String.format("%s/list", ioInfo.getHttpServer().getBasePath()))
            || uri.equals(String.format("%s/index", ioInfo.getHttpServer().getBasePath())))
        {
            return true;
        }
        return false;
    }
    
    private ImmutableTriple<String, String, String> findResourcePathFromFreemarkerRouteMap(String uri)
    {
        //2.查找不到，则从docRoutesMap中查找  例如
        //uri:       /root/web/page/index.ftl
        //key:       /root/web
        //value:     static/
        for (String key : freemarkerRoutesMap.keySet())
        {
            if (uri.startsWith(key + "/"))
            {
                //uri:       /root/web/page/index.ftl
                //key:       /root/web
                //value:     static/
                
                //key:       /root/web/
                String subPath = StringUtils.removeStart(uri, key + "/");
                //subPath:   page/index.ftl
                //uri解码，即可完美支持中文
                subPath = URIEncoderDecoder.decode(subPath);
                return new ImmutableTriple<String, String, String>(key + "/", freemarkerRoutesMap.get(key), subPath);
            }
        }
        return null;
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
        for (String key : docDirectoryRoutesMap.keySet())
        {
            //uri:       /root/games
            //uri:       /root/games/
            if (uri.equals(key) || uri.equals(key + "/"))
            {
                //刚好相等，那么直接将那个文件夹返回即可
                return docDirectoryRoutesMap.get(key);
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
                return new File(docDirectoryRoutesMap.get(key), subPath);
            }
        }
        return null;
    }
    
    /**
     * 将方法放入到路由表中
     * @author nan.li
     * @param path
     * @param controllerPathMethodMapper
     * @param controllerMethodAnno 
     */
    private void putControllerRoutesMap(String path, ControllerPathMethodMapper controllerPathMethodMapper, String controllerMethodAnno, HttpServer httpServer)
    {
        Logs.i(String.format("Adding route -> %s", controllerPathMethodMapper));
        httpServer.getHttpServiceSummary().trackServicePath(controllerPathMethodMapper.getRoutingPath(), controllerMethodAnno);
        if (controllerRoutesMap.containsKey(path))
        {
            System.err.println("警告：存在重复的path: " + path + ", 将会仅保留最后的那个映射方法！");
        }
        controllerRoutesMap.put(path, controllerPathMethodMapper);
    }
    
    /**
     * 正则匹配的路由附表
     * @author nan.li
     * @param urlRegex
     * @param regexMapDetail
     * @param controllerMethodAnno 
     */
    private void putToRegexMap(String urlRegex, RegexMapDetail regexMapDetail, String controllerMethodAnno)
    {
        Logs.i(String.format("Adding REGEX route %s -> %s", urlRegex, regexMapDetail));
        httpServer.getHttpServiceSummary().trackServicePath(regexMapDetail.getMethodMapping(), controllerMethodAnno);
        if (regexUrlToDetailMap.containsKey(urlRegex))
        {
            System.err.println("警告：存在重复的urlRegex: " + urlRegex + ", 将会仅保留最后的那个映射！");
        }
        regexUrlToDetailMap.put(urlRegex, regexMapDetail);
    }
    
    /**
     * 查找指定uri所能匹配到的Route对象
     * @author nan.li
     * @param uri
     * @return
     */
    public ImmutablePair<ControllerPathMethodMapper, Map<String, String>> findFromControllerMap(String uri)
    {
        //        Logs.d("findFromControllerMap()  uri: " + uri);
        //uri:  /card/cache.do 
        
        //        Pattern pattern = Pattern.compile("^\\/\\w+\\/\\w+$");
        
        //      /card/users/{userId}/topics/{topicId}.do
        //uri:  /card/users/155/topics/200.do
        //uri:  ^/card/users/\\w+/topics/\\w+.do$
        //      ^/card/users/(\\w+)/topics/(\\w+).do$
        ControllerPathMethodMapper controllerPathMethodMapper = controllerRoutesMap.get(uri);
        if (controllerPathMethodMapper != null)
        {
            //直接就匹配到了
            return new ImmutablePair<ControllerPathMethodMapper, Map<String, String>>(controllerPathMethodMapper, null);
        }
        else
        {
            //还要尝试用正则附表去匹配
            for (String regexStr : regexUrlToDetailMap.keySet())
            {
                //尝试正则匹配
                //regexStr      ^/card/users/\w+/topics/\w+.do$
                //uri            /card/users/155/topics/200.do
                if (Pattern.compile(regexStr).matcher(uri).matches())
                {
                    //匹配成功！
                    //                    Logs.d("uri: " + uri + " 匹配成功！");
                    RegexMapDetail regexMapDetail = regexUrlToDetailMap.get(regexStr);
                    //接下来就是提取参数，构建paramMap
                    List<String> paramValueList = new ArrayList<>();
                    Pattern pat = Pattern.compile(regexMapDetail.getExtractRegex());//^/card/users/(\w+)/topics/(\w+).do$
                    Matcher mat = pat.matcher(uri);
                    while (mat.find())
                    {
                        for (int i = 1; i <= mat.groupCount(); i++)
                        {
                            String find = mat.group(i);
                            paramValueList.add(find);//[155,200]
                        }
                    }
                    //额外的参数表
                    //这些额外参数是通过url中的正则匹配出来的
                    Map<String, String> paramMap = new HashMap<>();
                    for (int i = 0; i < regexMapDetail.getParamNameList().size(); i++)
                    {
                        //userId     155
                        //topicId    200
                        paramMap.put(regexMapDetail.getParamNameList().get(i), paramValueList.get(i));
                    }
                    //   /card/users/{userId}/topics/{topicId}.do
                    controllerPathMethodMapper = controllerRoutesMap.get(regexMapDetail.getMethodMapping());
                    return new ImmutablePair<ControllerPathMethodMapper, Map<String, String>>(controllerPathMethodMapper, paramMap);
                }
            }
        }
        return null;
    }
    
    public boolean matchPath2(String key)
    {
        Pattern pattern = Pattern.compile("^/card/users/\\w+/topics/\\w+.do$");
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
    
    public boolean matchPath(String key)
    {
        //        Pattern pattern = Pattern.compile("^\\/\\w+\\/\\w+$");
        Pattern pattern = Pattern.compile("^/\\w+/\\w+$");
        Matcher matcher = pattern.matcher(key);
        return matcher.matches();
    }
    
    public MutablePair<String, String> resolvePath(String key)
    {
        MutablePair<String, String> retPair = new MutablePair<>();
        List<String> ret = new ArrayList<>();
        Pattern pat = Pattern.compile("^/(\\w+)/(\\w+)$");
        Matcher mat = pat.matcher(key);
        while (mat.find())
        {
            for (int i = 1; i <= mat.groupCount(); i++)
            {
                String find = mat.group(i);
                ret.add(find);
            }
        }
        if (ret.size() == 2)
        {
            retPair.setLeft(ret.get(0));
            retPair.setRight(ret.get(1));
            return retPair;
        }
        return null;
    }
    
    /**
     * 文档路由表
     * @author lnwazg@126.com
     * @param basePath
     * @param file
     */
    private void putDocumentRootMap(String basePath, File file)
    {
        Logs.i(String.format("Adding DOCUMENT route -> %s --> %s", basePath, file.getPath()));
        httpServer.getHttpServiceSummary().trackServicePath(basePath, "http文件服务器");
        if (docDirectoryRoutesMap.containsKey(basePath))
        {
            System.err.println("警告：存在重复的document root path: " + basePath + ", 将会仅保留最后的那个映射方法！");
        }
        docDirectoryRoutesMap.put(basePath, file);
    }
    
    private void putFreemarkerRoot(String basePath, String resourcePath)
    {
        Logs.i(String.format("Adding FreeMarker route -> %s --> %s", basePath, resourcePath));
        httpServer.getHttpServiceSummary().trackServicePath(basePath, "freemarker页面");
        if (freemarkerRoutesMap.containsKey(basePath))
        {
            System.err.println("警告：存在重复的freemarker root path: " + basePath + ", 将会仅保留最后的那个映射方法！");
        }
        freemarkerRoutesMap.put(basePath, resourcePath);
    }
    
    /**
     * 将控制器加入路由表
     * @author nan.li
     * @param c
     * @param router
     */
    public void addControllerRoutes(Class<BaseController> c, Router router)
    {
        if (!controllerClassObjectMap.containsKey(c))
        {
            //            try
            //            {
            //                controllerClassObjectMap.put(c, c.newInstance());
            //            }
            //            catch (InstantiationException e)
            //            {
            //                e.printStackTrace();
            //            }
            //            catch (IllegalAccessException e)
            //            {
            //                e.printStackTrace();
            //            }
            //此处的newInstance换成动态代理生成的对象，从单例表中取出那个动态代理对象
            
            controllerClassObjectMap.put(c, B.g(c));
        }
        addControllerRoutes(c, controllerClassObjectMap.get(c), router);
    }
    
    /**
     * 将控制器加入路由表
     * @author nan.li
     * @param controllerClazz  
     * @param controllerProxy 其实是生成的动态代理类
     * @param router
     */
    public void addControllerRoutes(Class<BaseController> controllerClazz, BaseController controllerProxy, Router router)
    {
        if (controllerProxy == null || router == null)
        {
            throw new IllegalArgumentException();
        }
        //        Class<?> c = controller.getClass();
        Class<?> c = (Class<?>)controllerClazz;
        String basePath = httpServer.getBasePath();
        //基础路径
        if (c.isAnnotationPresent(Controller.class))
        {
            Controller bp = c.getAnnotation(Controller.class);
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
                String path = "";
                
                //取出Controller方法上面的注释，便于元数据页面进行描述
                String controllerMethodAnno = null;
                if (method.isAnnotationPresent(Anno.class))
                {
                    controllerMethodAnno = method.getAnnotation(Anno.class).value();
                }
                
                //方法映射
                String methodMapping = method.getName();
                //如果上面有注解，那么就映射到具体的注解所指定的路径上去
                if (method.isAnnotationPresent(RequestMapping.class))
                {
                    String annoValue = method.getAnnotation(RequestMapping.class).value();
                    if (StringUtils.isNotEmpty(annoValue))
                    {
                        // /cache
                        if (annoValue.startsWith("/"))
                        {
                            //@RequestMapping(value = "/cache")
                            //截取到斜杠后面的内容
                            annoValue = annoValue.substring(1);// cache
                        }
                        if (StringUtils.isNotEmpty(annoValue))
                        {
                            methodMapping = annoValue;
                        }
                    }
                }
                //    /users/{userId}/topics/{topicId}
                //    /cache
                //    可能是走RESTFUL模糊匹配uri参数的，也可能不走
                //是否是正则匹配的url
                boolean regexMapping = methodMapping.indexOf("{") != -1;
                
                //controller的结尾是否需要以.do结尾
                if (StringUtils.isNotEmpty(httpServer.getControllerSuffix()))
                {
                    //有后缀，则拼接上后缀
                    path = String.format("%s/%s.%s", finalBasePath, methodMapping, httpServer.getControllerSuffix()).trim();
                }
                else
                {
                    //无后缀，则直接是裸方法名
                    path = String.format("%s/%s", finalBasePath, methodMapping).trim();
                }
                
                //如何是正则表达式的话，那么还要借用另一个map多做一层关联
                if (regexMapping)
                {
                    //path = /card/users/{userId}/topics/{topicId}.do
                    
                    //将正则的内容放入到另一个表里面
                    //原有的methodMapping         /card/users/{userId}/topics/{topicId}.do
                    //期望匹配到的实际链接                       /card/users/155/topics/200.do
                    
                    //参数表：                                                     [userId, topicId]
                    
                    //匹配正则                                                      ^/card/users/\\w+/topics/\\w+.do$            用于进行url参数数据匹配
                    //提取正则                                                      ^/card/users/(\\w+)/topics/(\\w+).do$        用户进行url参数提取，获得到 155 200这两个参数，对应于  [userId, topicId] 参数名列表
                    
                    //                    Logs.d("regexMapping path: " + path);
                    //用于提取出参数表
                    Pattern pat = Pattern.compile("\\{(\\w+)\\}");
                    Matcher mat = pat.matcher(path);
                    List<String> paramNameList = new ArrayList<>();//[userId,topicId]
                    while (mat.find())
                    {
                        for (int i = 1; i <= mat.groupCount(); i++)
                        {
                            String paramName = mat.group(i);
                            paramNameList.add(paramName);
                        }
                    }
                    //                    Logs.d("paramNameList: " + paramNameList);
                    
                    //path = /card/users/{userId}/topics/{topicId}.do
                    String urlRegex = String.format("^%s$", path.replaceAll("\\{\\w+\\}", "\\\\w+"));//  ^/card/users/\w+/topics/\w+.do$ 
                    String extractRegex = String.format("^%s$", path.replaceAll("\\{\\w+\\}", "(\\\\w+)"));  //^/card/users/(\w+)/topics/(\w+).do$ 
                    //                                        Logs.d("urlRegex: " + urlRegex);
                    //                                        Logs.d("extractRegex: " + extractRegex);
                    router.putToRegexMap(urlRegex, new RegexMapDetail(path, extractRegex, paramNameList), controllerMethodAnno);
                }
                else
                {
                    //构建路由对象
                    ControllerPathMethodMapper controllerPathMethodMapper = new ControllerPathMethodMapper(path, method, controllerProxy);
                    //将其放入路由表
                    router.putControllerRoutesMap(path, controllerPathMethodMapper, controllerMethodAnno, httpServer);
                }
            });
        } while ((c = c.getSuperclass()) != BaseController.class);
    }
    
    public static void main(String[] args)
    {
        System.out.println("/card/users/{userId}/topics/{topicId}.do".replaceAll("\\{\\w+\\}", ""));
        //card/users//topics/.do
        Pattern pat = Pattern.compile("\\{(\\w+)\\}");
        Matcher mat = pat.matcher("/card/users/{userId}/topics/{topicId}.do");
        while (mat.find())
        {
            for (int i = 0; i <= mat.groupCount(); i++)
            {
                String find = mat.group(i);
                System.out.println(find);
            }
        }
        //        {userId}
        //        userId
        //        {topicId}
        //        topicId
    }
    
    /**
     * 往路由表中增加某个文档根目录的路由
     * @author lnwazg@126.com
     * @param basePath
     * @param file
     * @param router
     */
    public void addDocumentRootRoutes(String docBasePath, File file, Router router)
    {
        String basePath = httpServer.getBasePath();
        if (!docBasePath.startsWith("/"))
        {
            docBasePath = String.format("/%s", docBasePath);
        }
        basePath = String.format("%s%s", basePath, docBasePath);
        router.putDocumentRootMap(basePath, file);
    }
    
    /**
     * 增加freemarker的服务根目录映射
     * @author nan.li
     * @param docBasePath
     * @param resourcePath
     * @param router
     */
    public void addFreemarkerRootRoutes(String docBasePath, String resourcePath, Router router)
    {
        String basePath = httpServer.getBasePath();
        if (!docBasePath.startsWith("/"))
        {
            docBasePath = String.format("/%s", docBasePath);
        }
        if (!resourcePath.endsWith("/"))
        {
            resourcePath = String.format("%s/", resourcePath);
        }
        basePath = String.format("%s%s", basePath, docBasePath);
        router.putFreemarkerRoot(basePath, resourcePath);
        if (!httpServer.isInitFreemarkerRoot())
        {
            httpServer.setFkBasePath(basePath + "/");
            httpServer.setFkResourcePath(resourcePath);
            httpServer.setInitFreemarkerRoot(true);
        }
    }
    
}
