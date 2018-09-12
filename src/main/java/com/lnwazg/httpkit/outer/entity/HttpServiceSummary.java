package com.lnwazg.httpkit.outer.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * http服务的描述信息
 * @author nan.li
 * @version 2017年12月10日
 */
public class HttpServiceSummary
{
    /**
     * ip地址。默认为本地地址
     */
    private String ip = "127.0.0.1";
    
    /**
     * 端口号
     */
    private int port;
    
    /**
     * 是否是http协议。默认是http协议
     */
    private boolean protocolHttp = true;
    
    /**
     * 功能列表
     */
    private List<PathRemark> list;
    
    public String getIp()
    {
        return ip;
    }
    
    public HttpServiceSummary setIp(String ip)
    {
        this.ip = ip;
        return this;
    }
    
    public int getPort()
    {
        return port;
    }
    
    public HttpServiceSummary setPort(int port)
    {
        this.port = port;
        return this;
    }
    
    public boolean isProtocolHttp()
    {
        return protocolHttp;
    }
    
    public HttpServiceSummary setProtocolHttp(boolean protocolHttp)
    {
        this.protocolHttp = protocolHttp;
        return this;
    }
    
    public List<PathRemark> getList()
    {
        return list;
    }
    
    public HttpServiceSummary setList(List<PathRemark> list)
    {
        this.list = list;
        return this;
    }
    
    
    /**
     * 记录服务路径
     * @author nan.li
     * @param routingPath
     */
    public void trackServicePath(String routingPath)
    {
        List<PathRemark> pathRemarks = getList();
        if (pathRemarks == null)
        {
            pathRemarks = new ArrayList<>();
        }
        pathRemarks.add(new PathRemark().setPath(routingPath));
        setList(pathRemarks);
    }
    
    /**
     * 记录服务路径和说明信息
     * @author nan.li
     * @param routingPath
     * @param remark
     */
    public void trackServicePath(String routingPath, String remark)
    {
        List<PathRemark> pathRemarks = getList();
        if (pathRemarks == null)
        {
            pathRemarks = new ArrayList<>();
        }
        pathRemarks.add(new PathRemark().setPath(routingPath).setRemark(remark));
        setList(pathRemarks);
    }
}
