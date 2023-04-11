package com.chbcraft.internals.components;

import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.loader.Loader;
import com.chbcraft.internals.components.sysevent.PluginDisableEvent;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import com.chbcraft.plugin.Plugin;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class PluginClassLoader extends URLClassLoader{
    /**
     * 是否允许开启插件互相依赖
     */
    private final boolean enableDepend;
    /**
     * 是否允许开启插件跨域访问
     */
    private final boolean enableCrossDomain;
    /**
     * 插件名称
     */
    private final String pluginName;
    /**
     * 此插件以来的其他插件
     */
    private final List<String> dependPlugin;
    /**
     * 插件本体
     */
    private Plugin plugin;
    /**
     * 插件描述配置对象,plugin.yml
     */
    private final Configuration pluginDescription;
    /**
     * 插件执行器
     */
    private final Loader processor;
    /**
     * 插件对应的Jar包源文件的JarFile
     */
    private final JarFile jarFile;
    /**
     * Jar包主要信息对象
     */
    private final Manifest manifest;
    /**
     * 插件的URL地址对象
     */
    private final URL url;
    /**
     * 插件在Plugins中对应的文件夹文件对象
     */
    private final File dataFolder;
    /**
     * 输出对象
     */
    private final MessageBox Logger;
    /**
     * 缓存所有已经被加载过的Class对象
     */
    private final ConcurrentHashMap<String,Class<?>> classes = new ConcurrentHashMap<>();
    /**
     * 存储那些非法越界的访问插件的名称
     */
    private final Set<String> illegalAccessClass = new HashSet<>();
    static {
        ClassLoader.registerAsParallelCapable();
    }
    PluginClassLoader(Loader processor, ClassLoader parent, File originalFile, File dataFolder, Configuration pluginDescription,boolean enableCrossDomain,boolean enableDepend) throws Exception, NoClassDefFoundError{
        super(new URL[]{originalFile.toURI().toURL()}, parent);
        this.enableCrossDomain = enableCrossDomain;
        this.enableDepend = enableDepend;
        this.processor = processor;
        this.dataFolder = dataFolder;
        this.url = originalFile.toURI().toURL();
        this.jarFile = new JarFile(originalFile);
        this.manifest = jarFile.getManifest();
        this.pluginDescription = pluginDescription;
        Class<? extends CustomPlugin> clazz = null;
        if(pluginDescription==null){
            MessageBox.getLogger().warn("Your File:["+originalFile.getName()+"] don't have a plugin.yml config!,Load failed!");
            throw new IllegalArgumentException();
        }
        this.pluginName = String.valueOf(pluginDescription.getValueByKey(SectionName.PLUGIN_NAME));
        this.Logger = new MessageBox(pluginName);
        String mainClass = String.valueOf(pluginDescription.getValueByKey(SectionName.MAIN_CLASS));

        if(mainClass==null){
            Logger.warn("Your File:["+originalFile.getName()+"]'s plugin.yml have a wrong argument! -> main,Can not find main Class!Load failed!");
            throw new IllegalArgumentException();
        }
        Class<?> tempClass = null;
        try {
            tempClass = Class.forName(mainClass,true,this);
            //其他class怎么被加载的,所有的插件里面的类都是从插件的onEnable方法为主入口加载的,所以都是通过插件的主类的类加载器加载的,就是通过插件对应的加载器加载
        } catch (ClassNotFoundException e) {
           MessageBox.getLogger().warn("We can not find your mainClass"+mainClass+" in your plugin ->" + pluginName);
        }
        try{
            if(tempClass!=null)
                clazz = tempClass.asSubclass(CustomPlugin.class);
        }
        catch (ClassCastException e){
            Logger.warn(pluginName+": is not extends CustomPlugin.class,Load failed!");
            e.printStackTrace();
        }
        Object depends = pluginDescription.getValueByKey(SectionName.PLUGIN_DEPENDS);
        if(depends!=null){
            if(depends instanceof List){
                this.dependPlugin = ConfigurationUtil.castToList(depends);
            }else
                this.dependPlugin = new ArrayList<String>(){{add(String.valueOf(depends));}};
        }
        else
            dependPlugin = new ArrayList<>();
        if(clazz!=null){
            try{
                this.plugin = clazz.newInstance();
            }
            catch ( InstantiationException e){
                Logger.warn(pluginName+":May be you do not have a no parameter Constructor!Load failed!");
                throw e;
            }
            catch (IllegalAccessException e){
                Logger.warn(pluginName+":main Class don not have a public Constructor!Load failed!");
                throw e;
            }
        }
        try{
            this.plugin.onEnable();
        }
        catch (NoClassDefFoundError | NullPointerException error){
            throw error;
        }
    }

    /**
     * 获取插件的名字
     * @return 返回名字或NULL
     */
    public String getPluginName() {
        return this.pluginName;
    }

    /**
     * 从class名称中获得字节数据
     * @param className 类的包名类名
     * @return 返回字节数组
     */
    private byte[] getClazzBytes(String className) throws IOException {
        byte[] result = null;
        String path = className.replace(".","/").concat(".class");
        if(jarFile!=null){
            JarEntry jarEntry = jarFile.getJarEntry(path);
            if(jarEntry!=null){
                try (BufferedInputStream buffInput = new BufferedInputStream(jarFile.getInputStream(jarEntry));
                     ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                    byte[] buff = new byte[1024];
                    int num;
                    while ((num = buffInput.read(buff)) != -1) {
                        byteArrayOutputStream.write(buff, 0, num);
                    }
                    result = byteArrayOutputStream.toByteArray();
                }
            }
        }
        return result;
    }

    /**
     * 双亲委派最后一部查找name对应的类
     * @param name 全限定名
     * @return 返回Class
     * @throws ClassNotFoundException Class不存在
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return this.findClass(name,this.enableCrossDomain,this.enableDepend);
        } catch (IOException | IllegalAccessException e) {
        }
        return null;
    }

    /**
     * 从类的全限定名查找类Class
     * @param name 要加载的Class的全限定名
     * @param checkOther 是否查找其他的插件
     * @param checkDepends 是否查找依赖
     * @return 返回加载成功后的Class
     */
    public Class<?> findClass(String name,boolean checkOther,boolean checkDepends) throws ClassNotFoundException, IOException, IllegalAccessException {
        Class<?> result = null;
        if(classes.containsKey(name)){
            result = classes.get(name);
        }
        if(name.startsWith("com.chbcraft.")){
            throw new IllegalAccessException("You can not access to internal!");
        }
        if(result==null){
            if(checkDepends&&dependPlugin!=null){
                Iterator<String> depends = dependPlugin.iterator();
                String pluginName;
                while(result==null&&depends.hasNext()){
                    pluginName = depends.next();
                    ClassLoader loader = processor.getClassLoaderByPluginName(pluginName);
                    if(loader instanceof PluginClassLoader) {
                        try {
                            result = ((PluginClassLoader)loader).findClass(name);
                        } catch (ClassNotFoundException e) {
                            throw e;
                        }
                    }
                }
            }
            if(result==null){//从非依赖插件中查找
                if(checkOther){
                    result = ((PluginProcessor)processor).getClazzByName(name);
                    if(result!=null){
                        PluginClassLoader loader = (PluginClassLoader)result.getClassLoader();
                        String illegalAccessName = loader.getPluginName();
                        Logger.warnTips("Illegal Access visit depend Plugin -> ["+illegalAccessName+"] this is not a depend!");
                        assert dependPlugin != null;
                        dependPlugin.add(illegalAccessName);
                        illegalAccessClass.add(illegalAccessName);
                    }
                }
            }
        }
        if(result==null){
            byte[] clazzByte = getClazzBytes(name);
            if(clazzByte!=null){
                int dot = name.lastIndexOf(".");
                if(dot!=-1){
                    String packName = name.substring(0,dot);
                    if(this.getPackage(packName)==null){
                        if(manifest!=null){
                            this.definePackage(packName,this.manifest,this.url);
                        }
                        else{
                            this.definePackage(packName, null, null, null, null, null, null,this.url);
                        }
                    }
                }
                String jarPath = name.replace(".","/").concat(".class");
                CodeSource codeSource = getCodeSource(jarFile.getJarEntry(jarPath));
                result = defineClass(name,clazzByte,0,clazzByte.length,codeSource);
            }
        }
        if(result!=null){
            addClazz(name,result);
            processor.addClazz(name,result);
        }
        return result;
    }

    /**
     * 获得代码源对象
     * @param entry class的对应的jar文件对象
     * @return 返回源对象
     */
    private CodeSource getCodeSource(JarEntry entry) {
        CodeSigner[] codeSigners = entry.getCodeSigners();
        return new CodeSource(this.url,codeSigners);
    }

    private void addClazz(String name, Class<?> clazz) {
        if(classes.containsKey(name))
            return;
        classes.put(name, clazz);
    }
    /**
     * 反向注入属性
     * @param plugin 注入的插件
     */
    synchronized public void initialedPlugin(Plugin plugin) throws NoClassDefFoundError{
        if(plugin != null&&pluginDescription!=null){
            if(plugin instanceof CustomPlugin){
                CustomPlugin customPlugin = (CustomPlugin)plugin;
                customPlugin.init(pluginDescription, dataFolder,this.Logger);
            }
        }else
            MessageBox.getLogger().broadcastPluginWarn("插件出错!");
    }

    @Override
    protected void finalize() throws Throwable {
        this.Logger.warnTips("unloading!");
        FloatSphere.getPluginManager().callEvent(new PluginDisableEvent(plugin));
        if(jarFile!=null)
            jarFile.close();
    }

    /**
     * 获得插件本体
     * @return 返回插件本体或NULL
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * 返回所有被此类加载器加载的类的全限定名
     * @return 返回集合或者null
     */
    public Set<String> getClasses(){
        if(!classes.isEmpty())
            return classes.keySet();
        return null;
    }
}
