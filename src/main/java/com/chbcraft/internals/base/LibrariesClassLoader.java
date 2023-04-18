package com.chbcraft.internals.base;

import com.chbcraft.exception.LibInitializedException;
import com.chbcraft.internals.components.CustomPlugin;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import com.chbcraft.internals.components.utils.JarInitialUtils;
import com.chbcraft.plugin.Plugin;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LibrariesClassLoader extends BaseComponentLoader {
    private final File libFile;
    private final ConcurrentHashMap<String, Plugin> allLibs = new ConcurrentHashMap<>();
    public LibrariesClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
        if(urls.length==1){
            URL fileUrl = urls[0];
            if(fileUrl==null)
                throw new LibInitializedException("Your libs directory is Wrong!");
            File tempFile = new File(fileUrl.getPath());
            boolean flag = true;
            if(!tempFile.exists()){
                JarInitialUtils.readDirectoryFromJarToCd(Objects.requireNonNull(FloatSphere.getSelfFile()),"libs",tempFile,true);
                if(!tempFile.exists())
                    flag = tempFile.mkdirs();
            }
            if(!tempFile.isDirectory()||!flag)
                throw new LibInitializedException("You should choose one Directory!");
            this.libFile = tempFile;
        }
        else
            throw new LibInitializedException("You should choose one Directory!");
        File[] libsFile = this.libFile.listFiles(s -> s.getName().endsWith(".jar"));
        if(libsFile!=null){
            Iterator<File> files = Arrays.stream(libsFile).iterator();
            out:while(files.hasNext()){
                File jarFile;
                Configuration pluginYml;
                do{
                    if(!files.hasNext())
                        break out;
                    jarFile = files.next();
                    pluginYml = ConfigurationUtil.getPluginConfigYML(jarFile);
                }while(pluginYml==null);
                try {
                    this.addURL(jarFile.toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                String name = pluginYml.getString("name");
                String mainClass = pluginYml.getString("main");
                Class<? extends CustomPlugin> clazz = null;
                try {
                    clazz = Class.forName(mainClass,true,this).asSubclass(CustomPlugin.class);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                if(clazz!=null){
                    try {
                        Plugin main = clazz.newInstance();
                        initialedPlugin(main,pluginYml);
                        this.allLibs.put(name,main);
                        main.onEnable();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    /**
     * 反向注入属性
     * @param plugin 注入的插件
     */
    synchronized public void initialedPlugin(Plugin plugin,Configuration pluginDescription) throws NoClassDefFoundError{
        if(plugin != null&&pluginDescription!=null){
            File dataFolder = new File(this.libFile,pluginDescription.getString("name"));
            MessageBox logger = new MessageBox(pluginDescription.getString("name"));
            if(plugin instanceof CustomPlugin){
                CustomPlugin customPlugin = (CustomPlugin)plugin;
                customPlugin.init(pluginDescription, dataFolder,logger);
            }
        }else
            MessageBox.getLogger().broadcastPluginWarn("插件出错!");
    }

    @Override
    public void initialedPlugin(Plugin plugin) {

    }

    public Collection<String> getLibsName(){
        Collection<String> libsName = new HashSet<>();
        if(!allLibs.isEmpty()){
            Enumeration<String> names = allLibs.keys();
            while(names.hasMoreElements()){
                libsName.add(names.nextElement());
            }
        }
        return libsName;
    }

    /**
     * 删除指定名称的lib
     * @param plugin 插件
     */
    public void disableLib(Plugin plugin){
        if(plugin==null){
            MessageBox.getLogger().warnTips("The plugin is not exist!");
            return;
        }
        if(plugin.isEnable()){
            plugin.disable();
        }
        FloatSphere.getPluginManager().unregisterEventListener(plugin);
        FloatSphere.getPluginManager().unregisterRouter(plugin);
        this.allLibs.remove(plugin.getName());
    }

    /**
     * 注销掉所有的依赖
     */
    public void disableAllPlugin(){
        Collection<Plugin> plugins = allLibs.values();
        for (Plugin plugin : plugins) {
            disableLib(plugin);
        }
    }
}
