package com.lnwazg.httpkit.handler;

import com.lnwazg.httpkit.io.HttpReader;
import com.lnwazg.httpkit.io.HttpWriter;

/**
 * HttpHandler
 *
 * @author Ian Caffey
 * @since 1.0
 */
public interface HttpHandler
{
    public void accept(HttpReader reader, HttpWriter writer);
}
