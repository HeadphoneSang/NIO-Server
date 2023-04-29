package com.chbcraft.internals.components.enums;

public enum SectionName {
    /**
     * Websocket请求升级的url
     */
    WS_URL("ws-url"),
    /**
     * 请求超时时间
     */
    TIME_OUT("timeout"),
    /**
     * 最大插件限制数量
     */
    MAX_PLUGIN_LIMIT("max_plugin_limit"),
    /**
     * 插件加载提示
     */
    PLUGIN_LOADING("plugin_loading_message"),
    /**
     * 开启安全跨域
     */
    ENABLE_SECURITY("enable_security_IO"),
    /**
     * 服务器监听端口号
     */
    SERVER_PORT("server-port"),
    /**
     * 是否允许从,非依赖插件中加载类
     */
    ENABLE_CROSS_DOMAIN("enable_cross_domain"),
    /**
     * 是否允许插件之间互相依赖访问
     */
    ENABLE_DEPEND_OTHER("enable_depends"),
    /**
     * 是否替换系统提示
     */
    ENABLE_SYSTEM_OUT("replace_system_out"),
    /**
     * 主类
     */
    MAIN_CLASS("main"),
    /**
     * 插件名称
     */
    PLUGIN_NAME("name"),
    /**
     * 解码编码格式
     */
    DECODE_CHARSET("decode-charset"),
    /**
     * 依赖
     */
    PLUGIN_DEPENDS("after");
    private final String section;
    private SectionName(String section){
        this.section = section;
    }
    public String value(){
        return this.section;
    }
}
