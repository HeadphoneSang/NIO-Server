package com.chbcraft.internals.components.entries;
import com.chbcraft.internals.components.entries.config.Configuration;

import java.io.File;
import java.util.List;

public class PluginEntry {

    private final File file;

    /**
     * 获得载入时间
     * @return 返回载入时间
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * 设置在载入时间
     * @param startTime 时间
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    private long startTime = 0;
    private final String pluginName;
    private List<String> beforePlugins = null;
    private List<String> afterPlugins = null;
    private final Configuration pluginYml;

    PluginEntry(String pluginName, Configuration pluginYml, File file, List<String> beforePlugins, List<String> afterPlugins){
        this.beforePlugins = beforePlugins;
        this.afterPlugins = afterPlugins;
        this.pluginName = pluginName;
        this.file = file;
        this.pluginYml = pluginYml;
    }
    /**
     * 获得所有依赖他的插件
     * @return 返回插件集合
     */
    public List<String> getBeforePlugins() {
        return beforePlugins;
    }

    /**
     * 获得依赖的所有插件名称
     * @return 返回插件集合
     */
    public List<String> getAfterPlugins() {
        return afterPlugins;
    }

    /**
     * 获得这个插件的名字
     * @return 返回名称
     */
    public String getPluginName(){
        return (String) this.pluginName;
    }

    /**
     * 获得这个插件的源文件
     * @return 源文件或null
     */
    public File getFile(){
        return this.file;
    }

    /**
     * 获得这个插件的Plugin.yml文件
     * @return 返回这个文件或者null
     */
    public Configuration getPluginYml() {
        return pluginYml;
    }
}
