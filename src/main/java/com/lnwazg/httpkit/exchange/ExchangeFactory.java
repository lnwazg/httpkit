package com.lnwazg.httpkit.exchange;

import java.io.IOException;

/**
 * 交换信息对象的工厂类接口
 */
public interface ExchangeFactory extends AutoCloseable
{
    /**
     * 创建交换对象
     * @author nan.li
     * @return
     * @throws IOException
     */
    Exchange create()
        throws IOException;
        
    /**
     * 检测该工厂是否关闭了
     * @author nan.li
     * @return
     * @throws IOException
     */
    boolean isClosed()
        throws IOException;
}
