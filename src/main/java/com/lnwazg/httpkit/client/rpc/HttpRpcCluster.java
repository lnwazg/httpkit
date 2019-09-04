package com.lnwazg.httpkit.client.rpc;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;

public class HttpRpcCluster
{
    List<HttpRpc> list = new ArrayList<>();
    
    /**
     * 将节点添加到集群中
     * @author nan.li
     * @param httpRpc
     */
    public void addHttpRpc(HttpRpc httpRpc)
    {
        list.add(httpRpc);
    }
    
    /**
     * 从集群中随机挑选一个节点进行引用访问
     * @author nan.li
     * @param interfaceClazz
     * @return
     */
    public <T> T reference(Class<T> interfaceClazz)
    {
        return list.get(RandomUtils.nextInt(0, list.size())).reference(interfaceClazz);
    }
}
