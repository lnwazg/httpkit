package com.lnwazg.httpkit.page;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.lnwazg.httpkit.HttpResponseCode;
import com.lnwazg.httpkit.io.IOInfo;
import com.lnwazg.httpkit.server.HttpServer;
import com.lnwazg.httpkit.util.RenderUtils;

/**
 * 页面渲染的工具类
 * @author lnwazg@126.com
 * @version 2016年11月27日
 */
public class RenderPage
{
    static String templateStr = null;
    
    static
    {
        try
        {
            templateStr = IOUtils.toString(RenderPage.class.getResourceAsStream("2.html"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 渲染一个文件件
     * @author lnwazg@126.com
     * @param reader
     * @param writer
     * @param f
     * @param uri 
     */
    public static void showDirectory(IOInfo ioInfo, File f, String uri)
    {
        if (f == null || !f.exists() || !f.isDirectory())
        {
            return;
        }
        String title = f.getName();
        String body = getBody(f, uri);
        String result = StringUtils.replace(templateStr, "${title}", title);
        result = StringUtils.replace(result, "${body}", body);
        RenderUtils.renderHtml(ioInfo, HttpResponseCode.OK, result);
    }
    
    public static void showDirectory(IOInfo ioInfo, Map<String, File> docRoutesMap)
    {
        String title = "资源目录列表";
        String body = getDocRoutesRootBody(docRoutesMap);
        String result = StringUtils.replace(templateStr, "${title}", title);
        result = StringUtils.replace(result, "${body}", body);
        RenderUtils.renderHtml(ioInfo, HttpResponseCode.OK, result);
    }
    
    static Comparator<File> c = new Comparator<File>()
    {
        @Override
        public int compare(File o1, File o2)
        {
            //忽略大小写的排序比较
            //默认采用的升序排序的方式
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };
    
    static Comparator<String> c2 = new Comparator<String>()
    {
        @Override
        public int compare(String o1, String o2)
        {
            return o1.compareToIgnoreCase(o2);
        }
    };
    
    private static String getDocRoutesRootBody(Map<String, File> docRoutesMap)
    {
        StringBuilder sb = new StringBuilder();
        Set<String> dirs = docRoutesMap.keySet();
        List<String> list = new ArrayList<>();
        list.addAll(dirs);
        Collections.sort(list, c2);
        for (String dir : list)
        {
            sb.append(String.format("<a href=\"%s/\"><b><i>%s/</i></b></a><br>\r\n", dir, StringUtils.removeStart(dir, HttpServer.getBasePath() + "/")));
        }
        return sb.toString();
    }
    
    /**
     * 符合这种模式的 /root/d/
     * @author nan.li
     * @param uri
     * @return
     */
    private static boolean matchRoot(String uri)
    {
        Pattern pattern = Pattern.compile("^" + HttpServer.getBasePath() + "/\\w+/$");
        //        System.out.println(pattern);
        Matcher matcher = pattern.matcher(uri);
        return matcher.matches();
    }
    
    //    public static void main(String[] args)
    //    {
    //        HttpServer.setBasePath("root");
    //        System.out.println(matchRoot("/root/*dd/"));
    //    }
    //    
    
    /**
     * 拼接文件列表的内容
     * @author lnwazg@126.com
     * @param f
     * @param uri 
     * @return
     */
    private static String getBody(File f, String uri)
    {
        //        <a href="StarCraft/">StarCraft/</a>
        //        <br>
        //        <a href="1.doc">1.doc</a>
        //        <br>
        //uri:      /root/d/
        StringBuilder sb = new StringBuilder();
        String toUpper = "..";
        String toUpperUrl = "../";
        if (matchRoot(uri))
        {
            toUpperUrl = String.format("%s/list", HttpServer.getBasePath());
        }
        sb.append(String.format("<a href=\"%s\"><b><i>%s</i></b></a><br>\r\n", toUpperUrl, toUpper));
        
        File[] files = f.listFiles();
        List<File> dirs = new ArrayList<>();
        List<File> fs = new ArrayList<>();
        
        if (ArrayUtils.isNotEmpty(files))
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    dirs.add(file);
                }
                else
                {
                    fs.add(file);
                }
            }
        }
        
        Collections.sort(dirs, c);
        Collections.sort(fs, c);
        
        for (File file : dirs)
        {
            sb.append(String.format("<a href=\"%s/\"><b><i>%s/</i></b></a><br>\r\n", file.getName(), file.getName()));
        }
        for (File file : fs)
        {
            sb.append(String.format("<a href=\"%s\">%s</a><br>\r\n", file.getName(), file.getName()));
        }
        
        return sb.toString();
    }
    
}
