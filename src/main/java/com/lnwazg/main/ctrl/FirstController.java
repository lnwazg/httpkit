package com.lnwazg.main.ctrl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.CharEncoding;

import com.lnwazg.httpkit.anno.BasePath;
import com.lnwazg.httpkit.controller.Controller;
import com.lnwazg.httpkit.mime.MimeMappingMap;

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
        
        ok("You are great!");
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
        okFile(new File("c:\\1.jpg"));
    }
    
    void index3()
    {
        ok("index3");
    }
    
    void index4()
    {
        okJson(MimeMappingMap.mimeMap);
    }
    
    void index5()
    {
        okJson(CharEncoding.UTF_8);
    }
    
    public static class Person
    {
        String name;
        
        String age;
    }
}
