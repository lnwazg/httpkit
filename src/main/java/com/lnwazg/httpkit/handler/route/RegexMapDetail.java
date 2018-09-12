package com.lnwazg.httpkit.handler.route;

import java.util.List;

/**
 * 正则匹配URL映射
 * @author nan.li
 * @version 2017年4月21日
 */
public class RegexMapDetail
{
    /**
     * 方法映射的路径<br>
     * /card/users/{userId}/topics/{topicId}.do
     */
    private String methodMapping;
    
    
    /**
     * 提取参数用到的正则<br>
     * ^/card/users/(\w+)/topics/(\w+).do$
     */
    private String extractRegex;
    
    /**
     * 参数表<br>
     * [userId,topicId]
     */
    private List<String> paramNameList;
    
    public RegexMapDetail(String methodMapping, String extractRegex, List<String> paramNameList)
    {
        this.methodMapping = methodMapping;
        this.extractRegex = extractRegex;
        this.paramNameList = paramNameList;
    }
    
    public String getMethodMapping()
    {
        return methodMapping;
    }
    
    public RegexMapDetail setMethodMapping(String methodMapping)
    {
        this.methodMapping = methodMapping;
        return this;
    }
    
    public String getExtractRegex()
    {
        return extractRegex;
    }
    
    public RegexMapDetail setExtractRegex(String extractRegex)
    {
        this.extractRegex = extractRegex;
        return this;
    }
    
    public List<String> getParamNameList()
    {
        return paramNameList;
    }
    
    public RegexMapDetail setParamNameList(List<String> paramNameList)
    {
        this.paramNameList = paramNameList;
        return this;
    }
    
    @Override
    public String toString()
    {
        return "RegexMapDetail [methodMapping=" + methodMapping + ", extractRegex=" + extractRegex + ", paramNameList=" + paramNameList + "]";
    }
}
