package com.chbcraft.net.handlers.inbound;

import com.chbcraft.internals.components.listen.RegisteredRouter;
import java.util.List;
import java.util.Map;

public interface HttpRequestImp {
    /**
     * 获得请求的完整URL
     * @return 返回URL字符串
     */
    String url();

    /**
     * 获得请求的类型
     * @return 返回完整的请求类型
     */
    RegisteredRouter.RouteMethod method();

    /**
     * 获得请求体内容字符串
     * @return 返回字符串
     */
    String getContentText();

    /**
     * 覆盖掉原来的请求体数据
     * @param text 新的请求体数据
     */
    void setContentText(String text);

    /**
     * 获得请求头键名对应的值
     * @param key 键
     * @return 值
     */
    String getHeader(String key);

    /**
     * 获得所有的请求体
     * @return 返回一个请求集合,只读,更改不会起作用
     */
    List<Map.Entry<String,String>> getHeaders();

    /**
     * 获得版本号
     * @return 返回版本号
     */
    String protocolVersion();
}
