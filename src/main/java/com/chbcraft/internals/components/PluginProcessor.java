package com.chbcraft.internals.components;

import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.entries.PluginEntry;
import com.chbcraft.internals.components.enums.EventPriority;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.listen.EventHandler;
import com.chbcraft.internals.components.listen.RegisteredListener;
import com.chbcraft.internals.components.loader.Loader;
import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.internals.components.sysevent.EventExecutor;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import com.chbcraft.plugin.Plugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PluginProcessor implements Loader {

    private static volatile Loader INSTANCE;
    /**
     * 是否允许开启插件互相依赖
     */
    private boolean enableDepend = true;
    /**
     * 是否允许开启插件跨域访问
     */
    private boolean enableCrossDomain = true;
    /**
     * 管理所有插件的Class对象与对应的全限定名
     */
    private final ConcurrentHashMap<String,Class<?>> allClasses = new ConcurrentHashMap<>();
    /**
     * 查看是否开启安全域检查
     */
    private boolean checkSecurity = false;
    /**
     * 安全管理器
     */
    private PluginSecurityManager securityManager;
    /**
     * 保存所有的插件加载器的引用和对应的插件的名称
     */
    private final ConcurrentHashMap<String, PluginClassLoader> allPlugins = new ConcurrentHashMap<>();
    private PluginProcessor() {
        super();
        Configuration prop = FloatSphere.createProperties();
        Object enableCross = null;
        Object enableDepends = null;
        if(prop !=null){
            enableCross = prop.getValueByKey(SectionName.ENABLE_CROSS_DOMAIN);
            enableDepends = prop.getValueByKey(SectionName.ENABLE_DEPEND_OTHER);
            checkSecurity = Boolean.parseBoolean(String.valueOf(prop.getValueByKey(SectionName.ENABLE_SECURITY)));
        }
        if(enableDepends!=null)
            this.enableDepend = Boolean.parseBoolean(String.valueOf(enableDepends));
        if(enableCross!=null)
            this.enableCrossDomain = Boolean.parseBoolean(String.valueOf(enableCross));
        enableSecurityIO();
    }

    /**
     * 开启插件文件访问权限管理
     */
    private void enableSecurityIO(){
        if(checkSecurity){
            if(securityManager==null)
                securityManager = PluginSecurityManager.getManager();
            System.setSecurityManager(PluginSecurityManager.getManager());
            return;
        }
        MessageBox.getLogger().log("Security-IO is not enable...");
    }

    /**
     * 从Plugin对象和Listener对象中构造RegisteredListener数组
     * @param plugin 创建监听器的插件
     * @param listener 创建监听器的类
     * @return 返回键值对表M,Event的Class对象为键值,对应的集合处理为值,或者是null
     */
    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegistered(Plugin plugin, Listener listener){
        if(plugin==null){
            MessageBox.getLogger().warnTips("Plugin is not enable to set null!");
            return null;
        }
        if(listener==null){
            MessageBox.getLogger().warnTips("Listener in "+ Objects.requireNonNull(plugin).getName()+" is null!");
            return null;
        }
        if(!plugin.isEnable()){
            MessageBox.getLogger().warnTips(plugin.getName()+"had already disable!");
            return null;
        }
        Class<? extends Listener> clazz = listener.getClass();
        Method[] selfMethods = clazz.getDeclaredMethods();
        HashSet<Method> methods = new HashSet<>(Arrays.asList(selfMethods));
        Method[] selfAndParentsMethod = clazz.getDeclaredMethods();
        methods.addAll(Arrays.asList(selfAndParentsMethod));
        if(methods.size()==0){
            MessageBox.getLogger().warnTips("Listener in "+ Objects.requireNonNull(plugin).getName()+" is illegal!");
            return null;
        }
        Iterator<Method> iterator = methods.iterator();
        HashMap<Class<? extends Event>, Set<RegisteredListener>> returnMap = new HashMap<>();
        while(true){
            Method tempMethod;
            EventHandler annotation;
            do{
                do{
                    do{
                        if(!iterator.hasNext())
                            return returnMap;
                        tempMethod = iterator.next();
                        annotation = tempMethod.getAnnotation(EventHandler.class);
                    }while (annotation==null);
                }while(tempMethod.isBridge());
            }while (tempMethod.isSynthetic());
            if(tempMethod.getParameterCount()==1&&Event.class.isAssignableFrom(tempMethod.getParameterTypes()[0])){
                Class<? extends Event> eventClazz = tempMethod.getParameterTypes()[0].asSubclass(Event.class);
                Set<RegisteredListener> targetSet = returnMap.computeIfAbsent(eventClazz, k -> new HashSet<>());
                for(Class<?> clazzTemp = eventClazz;Event.class.isAssignableFrom(clazzTemp);clazzTemp = clazzTemp.getSuperclass()){
                    if(clazzTemp.getAnnotation(Deprecated.class)!=null){
                        MessageBox.getLogger().warnTips(listener.getClass().getSimpleName()+" is annotation Deprecated!");
                        break;
                    }
                }
                RegisteredListener newListener;
                Method finalTempMethod = tempMethod;
                EventExecutor executor = (listener1, event) -> {
                    if(eventClazz.isAssignableFrom(event.getClass())){
                        finalTempMethod.invoke(listener1,event);
                    }
                };
                EventPriority priority = annotation.value();
                newListener = new RegisteredListener(plugin,priority,executor,listener);
                targetSet.add(newListener);
            }
            else{
                MessageBox.getLogger().warnTips("We attempt register a EventHandler method in "+ listener.getClass().getSimpleName()+" but failed,The Method may has not just one Parameter");
            }
        }
    }
    /**
     * 通过插件名获得插件
     * @param pluginName 插件名
     * @return 返回插件本体或者时null
     */
    Plugin getPlugin(String pluginName) {
        Plugin result = null;
        PluginClassLoader loader = allPlugins.getOrDefault(pluginName,null);
        if(loader!=null)
            result = loader.getPlugin();
        return result;
    }

    @Override
    public int getPluginNumber() {
        return this.allPlugins.size();
    }

    @Override
    public ClassLoader getClassLoaderByPluginName(String name) {
        return allPlugins.getOrDefault(name, null);
    }

    @Override
    public void addClazz(String name, Class<?> clazz) {
        if(allClasses.containsKey(name)){
            return;
        }
        allClasses.put(name,clazz);
    }
    /**
     * 获得插件加载器
     * @return 返回加载器实例
     */
    public static Loader getInstance() {
        synchronized (PluginProcessor.class){
            if(INSTANCE==null){
                if(INSTANCE==null){
                    INSTANCE = new PluginProcessor();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * 通过插件的信息对象,加载插件的主入口文件,并且给插件绑定独立的类加载器
     * 并将新创建的类加载器放入全局表中,进行管理
     * @param pluginEntry 插件对应的文件
     * @param parentLoader 父加载器
     * @throws Exception
     * @throws NoClassDefFoundError
     */
    @Override
    public void loadPlugin(PluginEntry pluginEntry,ClassLoader parentLoader) throws Exception, NoClassDefFoundError {
        if(pluginEntry==null){
            MessageBox.getLogger().warnTips("The File is not a Plugin!");
            return;
        }
        File file = pluginEntry.getFile();
        if(!file.exists()){
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        if(file.isDirectory()){
            return;
        }
        PluginClassLoader classLoader;
        if(file.getName().endsWith(".jar")){
            Configuration pluginDescription = pluginEntry.getPluginYml();
            if(pluginDescription==null)
            {
                MessageBox.getLogger().warn("Plugin: "+file.getName()+" do not have a plugin.yml!Loading Fail!");
                return;
            }
            File dataFolder = new File(file.getParentFile(),pluginEntry.getPluginName());
            Object depends = pluginDescription.getValueByKey("after");
            if(depends!=null){
                if(depends instanceof List){
                    List<String> dependsName = ConfigurationUtil.castToList(depends);
                    for(String name:dependsName){
                        if(allPlugins.get(name)==null)
                            MessageBox.getLogger().warnTips("["+pluginEntry.getPluginName()+"]Depend Plugin["+name+"] is not install or not exist!Please install it");
                    }
                }
                else
                {
                    String depend = String.valueOf(depends);
                    if(allPlugins.get(depend)==null)
                        MessageBox.getLogger().warnTips("["+pluginEntry.getPluginName()+"]Depend Plugin["+depend+"] is not install or not exist!Please install it");
                }
            }
            //创建每个插件的独立的类加载器
            classLoader = new PluginClassLoader(this,parentLoader,file,dataFolder,pluginDescription,this.enableCrossDomain,this.enableDepend);
            this.allPlugins.put(pluginEntry.getPluginName(), classLoader);
        }
    }

    /**
     * 通过类名获得Class对象
     *
     * @param name 包名类名
     * @return 返回Class
     */
    Class<?> getClazzByName(String name) {
        Class<?> result = null;
        if(allClasses.containsKey(name)){
            result = allClasses.get(name);
        }
        if(result==null)
        {
            Enumeration<? extends PluginClassLoader> allLoaders = allPlugins.elements();
            PluginClassLoader pluginLoader;
            while(allLoaders.hasMoreElements()){
                pluginLoader = allLoaders.nextElement();
                try {
                    result = pluginLoader.findClass(name,false,false);
                } catch (ClassNotFoundException | IOException | IllegalAccessException ignored) {
                }
                if(result != null)
                    break;
            }
        }
        return result;
    }

    @Override
    public void disablePlugin(Plugin plugin) {
        if(plugin==null){
            MessageBox.getLogger().warnTips("The plugin is not exist!");
            return;
        }
        if(plugin.isEnable()){
            plugin.disable();
        }
        FloatSphere.getPluginManager().unregisterEventListener(plugin);
        this.allPlugins.remove(plugin.getName());
        ClassLoader loader = plugin.getClass().getClassLoader();
        if(!(loader instanceof PluginClassLoader))
            return;
        Set<String> allClasses = ((PluginClassLoader) loader).getClasses();
        Iterator<String> iterator = allClasses.iterator();
        String name;
        while(iterator.hasNext()){
            name = iterator.next();
            this.allClasses.remove(name);
        }
    }

    @Override
    public List<String> getPluginList() {
        List<String> ret = new ArrayList<>();
        Enumeration<String> temps = allPlugins.keys();
        while(temps.hasMoreElements())
            ret.add(temps.nextElement());
        return ret;
    }

}
