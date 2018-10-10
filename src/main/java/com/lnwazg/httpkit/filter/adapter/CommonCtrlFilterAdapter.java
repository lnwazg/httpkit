package com.lnwazg.httpkit.filter.adapter;

import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.httpkit.filter.CtrlFilter;
import com.lnwazg.httpkit.filter.CtrlFilterChain;
import com.lnwazg.kit.log.Logs;

/**
 * 通用的过滤器模板<br>
 * 使用该模板，将比直接实现CtrlFilter接口更加简单方便
 * @author linan
 */
public abstract class CommonCtrlFilterAdapter extends BaseController implements CtrlFilter
{
    
    @Override
    public void doFilter(CtrlFilterChain filterChain)
    {
        before();
        
        if (shouldPass())
        {
            filterChain.moveToNext(this);
        }
        else
        {
            Logs.i("shouldPass()返回false，过滤器链提前结束！");
        }
        
        after();
    }
    
    /**
     * 过滤前要做的事情
     */
    public abstract void before();
    
    /**
     * 过滤后要做的事情
     */
    public abstract void after();
    
    /**
     * 是否允许过滤器主链条继续往下走<br>
     * 若你要在过滤器中做逻辑校验，并根据校验结果决定真正的业务逻辑是否参与处理，那么应该这么做：若要拦截，则返回false；否则，返回false<br>
     * 对于普通的逻辑链应用场景，则该方法应该始终返回true，而真正的链式逻辑应该重写before()和after()两个方法
     * @return true 过滤器链会继续往下走; false 过滤器链不会继续往下走，主方法也将被拦截
     */
    public abstract boolean shouldPass();
    
}
