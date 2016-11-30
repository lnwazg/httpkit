# kit
java版http服务器

### 使用方法

        //起一个server实例
        int port = 7777;
        HttpServer server = HttpServer.bind(port);

	//设置上下文路径
        server.setContextPath("root");

	//设置controller类
        server.packageSearchAndInit("com.lnwazg.main.ctrl");

	//设置自动磁盘扫描
        server.autoSearchThenAddWatchResourceDirRoute();
        
        //监听在这个端口处
        server.listen();


然后访问文件列表：
http://127.0.0.1:7777/root/list

访问具体的控制器
http://127.0.0.1:7777/root/base/index