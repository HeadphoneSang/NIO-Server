package com.chbcraft.internals.components;

import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.entries.PluginEntry;
import com.chbcraft.internals.components.enums.EventPriority;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.listen.*;
import com.chbcraft.internals.components.loader.Loader;
import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.internals.components.sysevent.EventExecutor;
import com.chbcraft.internals.components.sysevent.PluginDisableEvent;
import com.chbcraft.internals.components.sysevent.PluginLoadedEvent;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import com.chbcraft.internals.components.utils.RegexUtil;
import com.chbcraft.plugin.Plugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
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

    @Override
    public Set<RegisteredRouter> createRegisteredRouter(Plugin plugin, Routers routers) {
        if (!isValid(plugin)){
            return null;
        }
        if(routers==null){
            MessageBox.getLogger().warnTips("Routers in "+ Objects.requireNonNull(plugin).getName()+" is null!");
            return null;
        }
        Class<? extends Routers> clazz = routers.getClass();
        Method[] selfMethods = clazz.getDeclaredMethods();
        HashSet<Method> methods = new HashSet<>(Arrays.asList(selfMethods));
        Method[] selfAndParentsMethod = clazz.getMethods();
        methods.addAll(Arrays.asList(selfAndParentsMethod));
        if(methods.size()==0){
            MessageBox.getLogger().warnTips("Routers in "+ Objects.requireNonNull(plugin).getName()+" is illegal!");
            return null;
        }
        Iterator<Method> iterator = methods.iterator();
        Set<RegisteredRouter> returnSet = new LinkedHashSet<>();
        while(true){
            Method tempMethod;
            Annotation annotation;
            do{
                do{
                    do{
                        if(!iterator.hasNext())
                            return returnSet;
                        tempMethod = iterator.next();
                        annotation = tempMethod.getAnnotation(Get.class);
                        if(annotation==null) {
                            annotation = tempMethod.getAnnotation(Post.class);
                        }
                    }while (annotation==null);
                }while(tempMethod.isBridge());
            }while (tempMethod.isSynthetic());
            RegisteredRouter newRouter = new RegisteredRouter(plugin,tempMethod,routers);
            Annotation[] tags = tempMethod.getDeclaredAnnotations();
            for (Annotation tag : tags) {
                if(tag.annotationType().getAnnotation(RouteType.class)!=null)
                    continue;
                newRouter.addTags(tag.annotationType());
            }
            if(annotation instanceof Post)
            {
                if(!initPostRouter(newRouter,plugin,annotation,tempMethod))
                    continue;
            }
            else
                if(!initGetRouter(newRouter,plugin,annotation,tempMethod))
                    continue;
            returnSet.add(newRouter);
        }
    }

    /**
     * GET方法的路由的解析方法
     * @param newRouter 要添加到路由表的路由
     * @param plugin 注册路由的插件
     * @param annotation 路由方法的注解
     * @param tempMethod 路由的方法
     * @return 返回初始化是否从成功
     */
    private boolean initGetRouter(RegisteredRouter newRouter,Plugin plugin,Annotation annotation,Method tempMethod){
        Get get = (Get) annotation;
        newRouter.setRoute(get.value());
        newRouter.setMethod(RegisteredRouter.RouteMethod.GET);
        Map<String,Integer> varMap = RegexUtil.getPathVariable(get.value());
        int[] mapArr = new int[varMap.size()];
        /**
         * 检查GET的router是否声明为一个REST路由
         * 如果是REST路由,将路由方法的形参全部提取出来
         * 并且记录形参的个数
         */
        int index = 0;
        for (Annotation[] parameterAnnotation : tempMethod.getParameterAnnotations()) {
            for (Annotation ano : parameterAnnotation) {
                if (ano instanceof PathParams) {
                    newRouter.setRest(true);
                    newRouter.setParamLength(newRouter.getParamLength() + 1);
                    if (varMap.get(((PathParams) ano).value()) == null) {
                        MessageBox.getLogger().warn("invalid router method: " + tempMethod.getName() + " in " + plugin.getName()+"\n");
                        return false;
                    }
                    mapArr[varMap.get(((PathParams) ano).value())] = index;
                }
            }
            index++;
        }
        if(newRouter.isRest()&&newRouter.getParamLength()!=tempMethod.getParameterCount()){
            MessageBox.getLogger().warn("invalid router method: "+tempMethod.getName()+" in "+plugin.getName());
            return false;
        }else if(newRouter.isRest()){
            newRouter.setRoute(newRouter.getRoute().substring(0,newRouter.getRoute().indexOf("{")-1));
            newRouter.setIndexMap(mapArr);
        }else{
            newRouter.setMethodParamsLength(tempMethod.getParameterCount());
        }
        return true;
    }

    /**
     * 初始化POST方法的路由
     * @param newRouter 将要加入路由表的新路由
     * @param plugin 注册路由的插件
     * @param annotation 路由的方法的注解
     * @param tempMethod 路由的处理方法
     * @return 返回是否注册成功
     */
    public boolean initPostRouter(RegisteredRouter newRouter,Plugin plugin,Annotation annotation,Method tempMethod){
        Post post = (Post) annotation;
        newRouter.setRoute(post.value());
        newRouter.setMethod(RegisteredRouter.RouteMethod.POST);
        if(tempMethod.getParameterCount()>1){
            MessageBox.getLogger().warnTips("zanbuzhichi");
            return false;
        }else{
            if(tempMethod.getParameterCount()==1){
                for (Annotation ano : tempMethod.getParameterAnnotations()[0]) {
                    if (ano instanceof PojoRequest){
                        Class<? extends RegisteredRouter> clazz = newRouter.getClass();
                        try {
                            Field clazz1 = clazz.getDeclaredField("paramClazz");
                            clazz1.setAccessible(true);
                            clazz1.set(newRouter,tempMethod.getParameterTypes()[0]);
                        } catch (NoSuchFieldException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        newRouter.setMethodParamsLength(tempMethod.getParameterCount());
        return true;
    }


    /**
     * 从Plugin对象和Listener对象中构造RegisteredListener数组
     * @param plugin 创建监听器的插件
     * @param listener 创建监听器的类
     * @return 返回键值对表M,Event的Class对象为键值,对应的集合处理为值,或者是null
     */
    @Override
    public Map<Class<? extends Event>, Set<RegisteredListener>> createRegistered(Plugin plugin, Listener listener){
        if(!isValid(plugin))
            return null;
        if(listener==null){
            MessageBox.getLogger().warnTips("Listener in "+ Objects.requireNonNull(plugin).getName()+" is null!");
            return null;
        }
        Class<? extends Listener> clazz = listener.getClass();
        Method[] selfMethods = clazz.getDeclaredMethods();
        HashSet<Method> methods = new HashSet<>(Arrays.asList(selfMethods));
        Method[] selfAndParentsMethod = clazz.getMethods();
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
     * 检查注册的插件是否还可以用
     * @param plugin 注册的插件
     * @return 返回是否可用
     */
    public boolean isValid(Plugin plugin){
        if(plugin==null){
            MessageBox.getLogger().warnTips("Plugin is not enable to set null!");
            return false;
        }
        if(!plugin.isEnable()){
            MessageBox.getLogger().warnTips(plugin.getName()+"had already disable!");
            return false;
        }
        return true;
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
            classLoader = new PluginClassLoader(this,parentLoader,file,dataFolder,pluginDescription,this.enableCrossDomain,this.enableDepend);
            FloatSphere.getPluginManager().callEvent(new PluginLoadedEvent(classLoader.getPluginName()));
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
        FloatSphere.getPluginManager().unregisterRouter(plugin);
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
        try {
            ((PluginClassLoader) loader).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        FloatSphere.getPluginManager().callEvent(new PluginDisableEvent(plugin.getName()));
    }

    @Deprecated
    @Override
    public List<String> getPluginList() {
        List<String> ret = new ArrayList<>();
        Enumeration<String> temps = allPlugins.keys();
        while(temps.hasMoreElements())
            ret.add(temps.nextElement());
        return ret;
    }



}
