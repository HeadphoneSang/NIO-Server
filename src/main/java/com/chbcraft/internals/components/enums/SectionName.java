package com.chbcraft.internals.components.enums;

public enum SectionName {
    /**
     * 最大插件限制数量
     */
    MAX_PLUGIN_LIMIT("max_plugin_limit"),
    PLUGIN_LOADING("plugin_loading_message"),
    ENABLE_SECURITY("enable_security_IO"),
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
    MAIN_CLASS("main"),
    PLUGIN_NAME("name"),
    PLUGIN_DEPENDS("after");
    private final String section;
    private SectionName(String section){
        this.section = section;
    }
    public String value(){
        return this.section;
    }
}
