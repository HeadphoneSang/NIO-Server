package com.chbcraft.internals.components.loader;

import com.chbcraft.internals.components.Listener;
import com.chbcraft.internals.components.entries.PluginEntry;
import com.chbcraft.internals.components.listen.RegisteredListener;
import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Loader {
    Map<Class<? extends Event>, Set<RegisteredListener>> createRegistered(Plugin plugin, Listener listener);
    /**
     * 获取当前插件总量
     * @return 返回插件总数
     */
    int getPluginNumber();

    /**
     * 从过全限定名获得类加载器
     * @param name 类名
     * @return 返回加载器
     */
    ClassLoader getClassLoaderByPluginName(String name);
    /**
     * 添加Class对象到全局中
     * @param name class的全限定名
     * @param clazz Class对象
     */
    void addClazz(String name, Class<?> clazz);

    /**
     * 从文件加载插件
     * @param pluginEntry 插件对应的文件
     */
    void loadPlugin(PluginEntry pluginEntry,ClassLoader parentLoader) throws Exception;

    /**
     * 取消插件加载
     * @param plugin 插件
     */
    void disablePlugin(Plugin plugin);

    List<String> getPluginList();
}
