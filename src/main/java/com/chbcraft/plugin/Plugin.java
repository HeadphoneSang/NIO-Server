package com.chbcraft.plugin;

import com.chbcraft.internals.components.entries.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface Plugin {
    /**
     * 关闭插件
     */
    public void disable();
    /**
     * 检查插件是否开启
     * @return 返回是否开启
     */
    public boolean isEnable();
    /**
     * 当插件启动时启动的方法体
     */
    public void onEnable();

    /**
     * 当插件取消的时候启动的方法
     */
    public void onDisable();

    /**
     * 获得插件在plugins中对应的文件夹
     * @return 饭
     */
    public File getDataFolder();

    /**
     * 获得Jar包中的config文件的输入流
     * @param fileName 文件的名字
     * @return 返回输入流
     * @throws IOException IO异常
     */
    public InputStream getDefaultResource(String fileName) throws IOException;

    /**
     * 获得插件的名字
     * @return 返回名字
     */
    public String getName();

    /**
     * 获得磁盘中的config文件,初始化操作对应的内存对象
     */
    public void loadConfig();

    /**
     * 获得对应的config.yml对象
     * @return 返回对象
     */
    public Configuration getConfig();

    /**
     * 将内存中的config对象存储到磁盘中
     */
    public void saveConfig();

    /**
     * 重新从磁盘读config内存对象
     */
    public void reloadConfig();
}
