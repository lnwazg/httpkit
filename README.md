# httpkit
一个精巧的java版http服务器，可替代tomcat、jetty，80ms极速启动。

- 精巧、独立，占用资源少，80ms极速启动,可替代tomcat
- 内置风控处理，防瞬时并发量过大崩溃
- 可以独立jar包方式运行，轻巧快速
- 可与nginx完美配合
- 支持内置的简洁方便的http文件服务器
- 支持内置自适应扫描的全盘服务器
- Controller指定包之后自动扫描，也可以自定义包扫描目录
- 支持path name param，便捷RESTUL模式开发
- freemarker模板支持
- 强大的过滤器支持
- 默认启用了自动跨域处理（属于系统默认的内置过滤器）
- 支持多端口、多服务实例方式启动   
- 支持服务器元信息显示，日志启动后即可查看
- Controller支持类级和方法级注解：@JsonResponse和@XmlResponse，用于将返回的对象自动转换成Json或Xml格式（2018-10-6）
- TODO RESTFUL模式增强，可以仅支持指定的http method
- TODO 强化文档，降低入门难度
- TODO 现有框架为BIO模式，规划整合NIO模式

### 使用方法

```java
//起一个server实例
int port = 7777;
HttpServer server = HttpServer.bind(port);

//设置上下文路径
server.setContextPath("root");

//设置controller类的扫描包
server.packageSearchAndInit("com.lnwazg.main.ctrl");

//设置自动磁盘扫描
server.autoSearchThenAddWatchResourceDirRoute();

//监听在这个端口处
server.listen();
```

然后访问文件列表：
http://127.0.0.1:7777/root/list

访问具体的控制器
http://127.0.0.1:7777/root/base/index

### Controller类的写法
```java
package com.lnwazg.main.ctrl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.lnwazg.httpkit.anno.BasePath;
import com.lnwazg.httpkit.controller.Controller;

/**
 * 示例controller<br>
 * 注解指定这个类的path
 * @author nan.li
 * @version 2016年11月30日
 */
@BasePath("/base")
public class FirstController extends Controller
{
    void index()
    {
        //以json形式输出一个对象
        okJson(Maps.asMap("name","LiNan","greetings","Hello World!","remark","This is a test controller!"));
    }

    void index2()
    {
        //输出一个文件
        okFile(new File("c:\\1.jpg"));
    }

    public static class Person
    {
        String name;

        String age;
    }
}

```

### 运行控制台输出：
![运行截图](screenshots/1.png)

### 内嵌的资源浏览器
![内嵌的资源浏览器](screenshots/2.png)

### 执行一个controller，输出一个json对象
![执行一个controller](screenshots/3.png)
