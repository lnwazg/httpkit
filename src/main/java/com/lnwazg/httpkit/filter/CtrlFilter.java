package com.lnwazg.httpkit.filter;

/**
 * 过滤器
 * @author lnwazg@126.com
 * @version 2017年3月15日
 */
public interface CtrlFilter
{
    /**
     * 进行过滤
     * @author lnwazg@126.com
     * @param filterChain
     */
    void doFilter(CtrlFilterChain filterChain);
}
