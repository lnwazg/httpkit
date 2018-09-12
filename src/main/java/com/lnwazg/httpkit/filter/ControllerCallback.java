package com.lnwazg.httpkit.filter;

/**
 * 最终调用servlet（controller）的回调函数
 * @author lnwazg@126.com
 * @version 2017年3月15日
 */
public interface ControllerCallback
{
    void call();
}
