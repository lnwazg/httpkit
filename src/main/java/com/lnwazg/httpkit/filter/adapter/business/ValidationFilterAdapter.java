package com.lnwazg.httpkit.filter.adapter.business;

import com.lnwazg.httpkit.filter.adapter.CommonCtrlFilterAdapter;

/**
 * 通用的业务逻辑校验模板<br>
 * 仅需override shouldPass()方法，若校验通过，返回true；否则，返回false
 * @author linan
 */
public abstract class ValidationFilterAdapter extends CommonCtrlFilterAdapter
{
    @Override
    public void before()
    {
        //do nothing
    }
    
    @Override
    public void after()
    {
        //do nothing
    }
    
    public abstract boolean shouldPass();
}
