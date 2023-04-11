package com.chbcraft.internals.components;

import com.chbcraft.internals.base.LibrariesClassLoader;
import com.chbcraft.internals.components.entries.EntryCreator;
import com.chbcraft.internals.components.entries.config.PluginConfiguration;
import com.chbcraft.internals.components.entries.PluginEntry;
import com.chbcraft.internals.components.entries.config.PropertiesConfiguration;
import com.chbcraft.internals.components.enums.ConfigType;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.utils.JarInitialUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

public class FloatSphere {
    /**
     * config文件夹下的config.properties文件的对象
     */
    private volatile static Configuration properties = null;
    private volatile static TopologyMachine topologyMachine = null;
    private static String ROOT_PATH = null;
    private static EntryCreator entryCreator = null;
    private static volatile PluginManager pluginManager;
    private static LibrariesClassLoader libLoader = null;

    /**
     * 获得当前的根目录
     * @return 返回根目录
     */
    public static String getRootPath(){
        synchronized (FloatSphere.class){
            if(ROOT_PATH==null){
                File file = null;
                try {
                    file = new File(FloatSphere.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                assert file != null;
                if(file.isFile())//判断当前是jar还是开发模式下
                    ROOT_PATH = file.getParentFile().getAbsolutePath().endsWith("\\")?file.getParentFile().getAbsolutePath():file.getParentFile().getAbsolutePath()+"\\";
                else {
                    try {
                        ROOT_PATH = FloatSphere.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                    } catch (URISyntaxException e) {
                        String t = FloatSphere.class.getProtectionDomain().getCodeSource().getLocation().getPath();
                        System.out.println(StandardCharsets.UTF_8.name());
                        try{
                            ROOT_PATH = URLDecoder.decode(t, StandardCharsets.UTF_8.name());
                        } catch (UnsupportedEncodingException unsupportedEncodingException) {
                            try{
                                ROOT_PATH = URLDecoder.decode(t,StandardCharsets.ISO_8859_1.name());
                            } catch (UnsupportedEncodingException encodingException) {
                                encodingException.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        return ROOT_PATH;
    }

    /**
     * 获得自己的绝对位置
     * @return 返回自己的绝对为值
     */
    public static File getSelfFile(){
        try {
            return new File(FloatSphere.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 创建配置文件对象,如果已经创建过了,就不再创建
     * @param map 从磁盘中存储的配置文件键值对
     * @param type 创建的类型
     * @return 返回配置文件对象
     */
    public static Configuration createConfig(Object map, ConfigType type){
        Configuration config = null;
        if(map instanceof Map){
            switch (type){
                case TYPE_CONFIG:{
                    config = new YamlConfiguration();
                    break;
                }
                case TYPE_PLUGIN:{
                    config = new PluginConfiguration();
                    break;
                }
                default:{
                }
            }
            addValueToConfig(config,map);
        }
        return config;
    }
    private static void addValueToConfig(Configuration config,Object map){
        Set<Map.Entry<String,Object>> objectSet = ConfigurationUtil.castToMap(map).entrySet();
        for(Map.Entry<String,Object> section : objectSet){
            config.addValueByKey(section.getKey(), section.getValue());
        }
    }

    /**
     * 使用创造器创建Entry
     * @param file Jar文件
     * @return 返回插件集合
     */
    public static PluginEntry createPluginEntry(File file) throws FileNotFoundException {
        if(entryCreator == null)
            entryCreator = new EntryCreator();
        if(file.getName().endsWith(".jar"))
        {
            return entryCreator.createPluginEntry(file);
        }
        return null;
    }

    /**
     * 创建一个config.properties配置文件对象
     * @return 返回这个对象
     */
    synchronized static Configuration createProperties(){
        if(properties == null){
            if(properties==null){
                properties = getProperties();
            }
        }
        return properties;
    }
    /**
     * 获得配置文件Configuration内容对象
     * @return 返回Configuration
     */
    static Configuration getProperties(){
        File file = new File(getRootPath()+"\\config\\config.properties");
        boolean flag;
        Configuration properties = null;
        if(file.exists()){
            Properties prop = new Properties();
            try (BufferedInputStream input = new BufferedInputStream(new FileInputStream(file))) {
                prop.load(input);
                properties = new PropertiesConfiguration(prop);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else{
            flag = file.getParentFile().mkdirs();
            try {
                if(flag||file.getParentFile().exists()||file.createNewFile()){
                    System.out.println(Objects.requireNonNull(FloatSphere.getSelfFile()).getAbsolutePath());
                    JarInitialUtils.readDirectoryFromJarToCd(Objects.requireNonNull(FloatSphere.getSelfFile()),"config",file.getParentFile(),true);
                    if(file.exists())
                        properties = getProperties();
                    else
                        System.out.println("Can not find a default config.properties in your jar!");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    /**
     * 创建topo排序工具
     * @return 返回工具
     */
    static TopologyMachine createTopologyMachine(){
        if(topologyMachine==null)
            topologyMachine = new TopologyMachine();
        return topologyMachine;
    }

    /**
     * 获得插件管理类
     * @return 返回管理类单例
     */
    public static PluginManager getPluginManager(){
        synchronized (FloatSphere.class){
            if(pluginManager==null){
                if (pluginManager==null){
                    pluginManager = new FloatPluginManager("plugins");
                }
            }
        }
        return pluginManager;
    }
}
