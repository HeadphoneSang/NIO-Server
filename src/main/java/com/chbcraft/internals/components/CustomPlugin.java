package com.chbcraft.internals.components;

import com.chbcraft.internals.base.BaseComponentLoader;
import com.chbcraft.internals.base.LibrariesClassLoader;
import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.enums.ConfigType;
import com.chbcraft.internals.components.utils.JarInitialUtils;
import com.chbcraft.plugin.BasePlugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public abstract class CustomPlugin extends BasePlugin {
    /**
     *  其类加载器
     */
    private BaseComponentLoader loader;
    /**
     * 插件的jar中的plugin.yml配置文件对象
     */
    private Configuration pluginDescriptions;
    /**
     * 插件对应的配置文件内存对象config.yml
     */
    private Configuration pluginConfig;
    /**
     * 插件对应的文件夹
     */
    private File dataFolder;
    /**
     * 文件夹中的config.yml文件
     */
    private File config;
    /**
     * 消息输出盒子
     */
    private MessageBox logger;

    private String jarName;
    public abstract void onEnable();

    public CustomPlugin(){
        ClassLoader loader = this.getClass().getClassLoader();
        if(loader instanceof PluginClassLoader){
            this.loader = (PluginClassLoader)loader;
            this.loader.initialedPlugin(this);
        }else if (loader instanceof LibrariesClassLoader){
            this.loader = (LibrariesClassLoader)loader;
        }
    }
    public void init(Configuration pluginInfoConfig, File dataFolder, MessageBox logger){
        this.dataFolder = dataFolder;
        this.pluginDescriptions = pluginInfoConfig;
        config = new File(dataFolder,"config.yml");
        this.logger = logger;
    }
    public void initLib(Configuration pluginInfoConfig, File dataFolder, MessageBox logger,String jarName){
        this.dataFolder = dataFolder;
        this.jarName = jarName;
        this.pluginDescriptions = pluginInfoConfig;
        config = new File(dataFolder,"config.yml");
        this.logger = logger;
    }
    public MessageBox getLogger(){
        if(logger==null)
            logger = MessageBox.getLogger();
        return logger;
    }
    @Override
    public void onDisable() {

    }

    @Override
    public File getDataFolder() {
        return dataFolder;
    }

    /**
     * 从Jar包中获得默认的配置文件
     * @param fileName 配置文件名称
     * @return 返回输入流
     */
    @Override
    public InputStream getDefaultResource(String fileName) throws IOException {
        InputStream inputStream = null;
        File tar = null;
        if(loader instanceof LibrariesClassLoader){
            tar = new File(dataFolder.getParent(),jarName);
        }else{
            tar = new File(jarName);
        }
        JarFile file = new JarFile(tar);
        ZipEntry entry = file.getEntry(fileName);
        inputStream = file.getInputStream(entry);


        return inputStream;
    }

    @Override
    public String getName() {
        if(pluginDescriptions!=null)
            return String.valueOf(pluginDescriptions.getValueByKey("name"));
        return null;
    }
    public void loadConfig(){
        Configuration temp = loadConfig(false);
        if(temp!=null)
            this.pluginConfig = temp;
        else
            getLogger().warnTips("config.yml is not found!or had loaded");
    }
    /**
     * 从磁盘的配置文件中读取并转化为pluginConfig
     */
    private Configuration loadConfig(boolean isReload){
        if(!isReload)
            if(pluginConfig!=null)
                return null;
        if(!dataFolder.exists())
            initDataFolder();
        if(!config.exists()){
            initDefaultFJar();
        }
        Configuration tempConfig = null;
        try(BufferedInputStream buff = new BufferedInputStream(new FileInputStream(config))){
            Configuration configuration = FloatSphere.createConfig(getFileMap(buff), ConfigType.TYPE_CONFIG);
            if(configuration instanceof YamlConfiguration){
                ((YamlConfiguration) configuration).setFile(config);
            }
            if(configuration!=null)
                tempConfig = configuration;
        }catch (IOException e){
            e.printStackTrace();
        }
        return tempConfig;
    }

    /**
     * 把config从磁盘中创建,并从Jar包中寻找默认配置文件加载到磁盘中
     */
    private boolean initDefaultFJar(){
        try (InputStream inputStream = getDefaultResource(config.getName())){
            if(inputStream!=null&&config.createNewFile()){
                File outputFile = config;
                try {
                    if( outputFile.createNewFile()||outputFile.exists()) {
                        try (BufferedOutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile))) {
                            byte[] buff = new byte[1024];
                            int num = 0;
                            while ((num = inputStream.read(buff)) != -1) {
                                output.write(buff, 0, num);
                            }
                            output.flush();
                        }
                        catch (Exception e){
                            getLogger().error("have something wrong when create default file ->"+config.getName());
                            return false;
                        }
                    }
                    else{
                        getLogger().error("have something wrong when create default file ->"+config.getName());
                        return false;
                    }
                } catch (IOException e) {
                    getLogger().error("have something wrong when create default file ->"+config.getName());
                    return false;
                }
            }else{
                getLogger().error("The Plugin don't have a Default Config!");
                return false;
            }
        } catch (IOException e) {
            getLogger().error("The Plugin don't have a Default Config!");
            return false;
        }
        return true;
    }
    private void initDataFolder(){
        dataFolder.mkdirs();
    }
    @Override
    public Configuration getConfig() {
        if(pluginConfig==null){
            loadConfig();
        }
        return this.pluginConfig;
    }
    @Override
    public void saveConfig() {
        if(pluginConfig==null){
            getLogger().warnTips(config.getName()+" has not find in your Directory!we will create a default file");
            initDefaultFJar();
            return;
        }
        if(!config.exists()){
            if(!initDefaultFJar()){
                try {
                    if(!config.createNewFile()){
                        getLogger().error("have something wrong when create default file ->"+config.getName());
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        pluginConfig.save();
    }

    @Override
    public void reloadConfig() {
        Configuration temp = null;
        if(pluginConfig==null)
            loadConfig();
        else
            temp = loadConfig(true);
        if(temp!=null)
            this.pluginConfig.setSections(temp.getSections());
        else
            logger.warnTips("File: config.yml has not found!");
    }
}