package com.lnwazg.httpkit.handler;

import com.lnwazg.httpkit.io.IOInfo;

/**
 * HttpHandler
 *
 * @author Ian Caffey
 * @since 1.0
 */
public interface HttpHandler
{
    public void accept(IOInfo ioInfo);
}
