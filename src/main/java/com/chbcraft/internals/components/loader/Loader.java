package com.chbcraft.internals.components.loader;

import com.chbcraft.internals.components.Listener;
import com.chbcraft.internals.components.Routers;
import com.chbcraft.internals.components.entries.PluginEntry;
import com.chbcraft.internals.components.listen.RegisteredListener;
import com.chbcraft.internals.components.listen.RegisteredRouter;
import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Loader {
    /**
     * 注册一个路由类进入服务器,并且初始化所有的路由方法,返回注册后的路由方法
     * @param plugin 注册的插件
     * @param routers 路由类
     * @return 返回注册路由类所有的路由方法的路由表方法
     */
    Set<RegisteredRouter> createRegisteredRouter(Plugin plugin, Routers routers);
    /**
     * 创建注册监听器类的所有的监听器对象
     * @param plugin 注册监听器的插件
     * @param listener 注册监听器的类
     * @return 返回创建后的监听器列表
     */
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

    /**
     * 获得所有插件名称的列表
     * @return 返回插件名称列表集合
     */
    List<String> getPluginList();


}
