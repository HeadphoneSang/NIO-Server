package com.chbcraft.net.handlers.router;

import java.util.HashMap;
import java.util.Map;

public class RouterAdaptor {
    private final Map<String,RequestSorter> sorterMap = new HashMap<>();
    private final RequestSorter defaultSorter = new UndefinedRequestSorter();
    public RouterAdaptor addSorter(String method,RequestSorter sorter){
        if(sorterMap.containsKey(method)){
            System.err.println("conflict method sorter: "+method);
        }
        else
            sorterMap.put(method,sorter);
        return this;
    }

    /**
     * 获得对应的分拣请求的处理器
     * @param method 请求类型
     * @return 返回请求分拣器或者是null
     */
    public RequestSorter getSorter(String method){
        return sorterMap.getOrDefault(method,defaultSorter);
    }
}
