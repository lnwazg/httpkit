package com.lnwazg.httpkit.filter.common;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.httpkit.filter.CtrlFilter;
import com.lnwazg.httpkit.filter.CtrlFilterChain;

/**
 * 跨域过滤器
 * @author nan.li
 * @version 2017年4月9日
 */
public class CORSFilter extends BaseController implements CtrlFilter
{
    public void doFilter(CtrlFilterChain filterChain)
    {
        //之前的逻辑
        addHeaderPre("Access-Control-Allow-Credentials", true);
        String origin = getHeader("Origin");
        if (StringUtils.isNotEmpty(origin))
        {
            addHeaderPre("Access-Control-Allow-Origin", origin);
        }
        
        //过滤器链是否应该继续移动
        filterChain.moveToNext(this);
        
        //之后的逻辑
        //...
    }
}