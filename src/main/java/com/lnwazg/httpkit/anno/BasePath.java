package com.lnwazg.httpkit.anno;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 设置处理器类的基础路径
 * @author nan.li
 * @version 2016年11月25日
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface BasePath
{
    public abstract String value();
}
