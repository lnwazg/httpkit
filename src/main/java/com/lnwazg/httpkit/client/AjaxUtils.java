package com.lnwazg.httpkit.client;

import java.io.IOException;
import java.util.Map;

import com.lnwazg.kit.http.ext.HttpUtils;

/**
 * 客户端访问的工具
 * @author nan.li
 * @version 2016年12月7日
 */
public class AjaxUtils
{
    /**
     * 获取ajax交互的结果内容
     * @author nan.li
     * @param url
     * @param params
     * @return
     */
    public static String getContent(String url, Map<String, String> params)
    {
        try
        {
            String content = HttpUtils.doGet(url, params);
            return content;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
    
    public static void main(String[] args)
    {
        System.out.println(getContent("http://127.0.0.1:7777/root/base/index", null));
    }
}
