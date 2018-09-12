package com.lnwazg.main.ctrl;

import java.io.File;

import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.kit.controllerpattern.Controller;
import com.lnwazg.kit.map.Maps;

/**
 * 示例controller<br>
 * 注解指定这个类的path
 * @author nan.li
 * @version 2016年11月30日
 */
@Controller("/base")
public class FirstController extends BaseController
{
    void index()
    {
//        Person person = new Person();
//        person.age = "18";
//        person.name = "hudson";
//        List<Person> list = new ArrayList<>();
//        for (int i = 0; i < 2; i++)
//        {
//            list.add(person);
//        }
//        person = new Person();
//        person.name = getParam("name");
//        list.add(person);
//        //输出abc
//        //        ok("abc");
//        //输出一个json对象
//        okJson(list);
        
        okJson(Maps.asMap("name","LiNan","greetings","Hello World!","remark","This is a test controller!"));
        
        //        ok(GsonHelper.gson.toJson(list));
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
