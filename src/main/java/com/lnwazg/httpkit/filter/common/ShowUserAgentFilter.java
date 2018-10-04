package com.lnwazg.httpkit.filter.common;

import com.lnwazg.httpkit.filter.adapter.business.LogicChainAdapter;
import com.lnwazg.kit.log.Logs;

/**
 * 显示UA的过滤器
 * @author linan
 */
public class ShowUserAgentFilter extends LogicChainAdapter
{
    
    @Override
    public void before()
    {
        Logs.i("user-agent is: " + getHeader("User-Agent"));
    }
    
    @Override
    public void after()
    {
        
    }
    
}
