package com.lnwazg.httpkit.outer.entity;

/**
 * 服务路径说明信息
 * @author nan.li
 * @version 2017年12月10日
 */
public class PathRemark
{
    /**
     * 服务路径
     */
    String path;
    
    /**
     * 功能说明
     */
    String remark;
    
    public String getPath()
    {
        return path;
    }
    
    public PathRemark setPath(String path)
    {
        this.path = path;
        return this;
    }
    
    public String getRemark()
    {
        return remark;
    }
    
    public PathRemark setRemark(String remark)
    {
        this.remark = remark;
        return this;
    }
}
