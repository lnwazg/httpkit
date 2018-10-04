package com.lnwazg.httpkit.filter.adapter.business;

import com.lnwazg.httpkit.filter.adapter.CommonCtrlFilterAdapter;

/**
 * 逻辑链模板<br>
 * 需要override两个方法：before()和after()，分别代表过滤前和过滤后要做的额外逻辑
 * @author linan
 */
public abstract class LogicChainAdapter extends CommonCtrlFilterAdapter
{
    public abstract void before();
    
    public abstract void after();
    
    @Override
    public boolean shouldPass()
    {
        return true;
    }
}
