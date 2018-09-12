package com.lnwazg.httpkit.filter.common;

import org.apache.commons.lang3.StringUtils;

import com.lnwazg.httpkit.controller.BaseController;
import com.lnwazg.httpkit.filter.CtrlFilter;
import com.lnwazg.httpkit.filter.CtrlFilterChain;

/**
 * 跨域过滤器
 * @author nan.li
 * @version 2017年4月9日
 */
public class CORSFilter extends BaseController implements CtrlFilter
{
    public void doFilter(CtrlFilterChain filterChain)
    {
        //            server.addExtraResponseHeaders(Maps.asStrMap("Access-Control-Allow-Origin", "*"));
        
        //例如： 当前所处的页面
        //            http://insurance.cekid.com/order/form/132/1273
        //想要访问的json请求的地址
        //            Request URL:http://baoxian.cekid.com/front_insurance/orderInte/queryRecognizeeList.do?skuId=1273
        
        //请求头中：
        //            Origin:http://insurance.cekid.com
        
        //响应头中要做的配置：
        //            Access-Control-Allow-Credentials:true
        //            Access-Control-Allow-Origin:http://insurance.cekid.com
        
        addHeaderPre("Access-Control-Allow-Credentials", true);
        String origin = getHeader("Origin");
        if (StringUtils.isNotEmpty(origin))
        {
            addHeaderPre("Access-Control-Allow-Origin", origin);
        }
        filterChain.moveToNext(this);
    }
}