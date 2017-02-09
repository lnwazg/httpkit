# httpkit
精巧的java版http服务器。可替代tomcat、jetty，80ms极速启动，颠覆你对java服务器的认知！

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
        Person person = new Person();
        person.age = "18";
        person.name = "hudson";
        List<Person> list = new ArrayList<>();
        for (int i = 0; i < 5; i++)
        {
            list.add(person);
        }
        person = new Person();
        person.name = getParam("fff");
        list.add(person);
        
        //输出abc。
        ok("abc");
        
        //输出一个json对象。
        //okJson(list);

        //        okJson(GsonHelper.gson.toJson(list));
        //        okJson(list);
        //        okJson(new FrontObj().success().setData(list));
        //        okJson(success(list));
        //        okJson(fail(list));
        //        okJson(fail(list,10002,"转换错误"));
        //        okJson(fail(10002, "转换错误"));
        //        okJson(fail(10003));
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

运行控制台输出：
![运行截图](1.png "运行示例")

内嵌的资源浏览器
!(2.png)
