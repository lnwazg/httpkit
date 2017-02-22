# httpkit
一个精巧的java版http服务器。可替代tomcat、jetty，80ms极速启动，颠覆你对java服务器的认知！  
注意，该项目需要依赖kit子项目！   
kit项目地址： https://github.com/lnwazg/kit

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

###运行控制台输出：
![运行截图](screenshots/1.png)

###内嵌的资源浏览器
![内嵌的资源浏览器](screenshots/2.png)

###执行一个controller，输出一个json对象
![执行一个controller](screenshots/3.png)
