package com.lnwazg.httpkit.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Json格式的响应，声明了之后整个Controller都是Json格式的输出<br>
 * 自动包裹异常响应
 * @author linan
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface JsonResponse
{
}
