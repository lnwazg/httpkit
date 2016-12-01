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
        
        //输出abc
        ok("abc");
        
        //输出一个json对象
        //        okJson(list);
        
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
