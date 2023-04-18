package com.chbcraft.internals.components;

import com.chbcraft.exception.ExceptionPrinter;
import com.chbcraft.internals.base.LibrariesClassLoader;
import com.chbcraft.internals.components.entries.PluginEntry;
import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.listen.*;
import com.chbcraft.internals.components.loader.Loader;
import com.chbcraft.internals.components.sysevent.Event;
import com.chbcraft.internals.components.sysevent.ManagerSup;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import com.chbcraft.plugin.Plugin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public class FloatPluginManager extends ManagerSup implements PluginManager{
    private static final HashSet<String> disablePlugins = new HashSet<>();
    /**
     * 允许加载的最大插件数
     */
    private int MAX_PLUGIN = Integer.MAX_VALUE;
    /**
     * 插件进程管理器
     */
    protected final Loader processor;
    /**
     * 插件路径->?/?/plugins
     */
    protected String PLUGIN_PATH;
    /**
     * 消息盒子
     */
    protected final MessageBox logger;
    /**
     * 路由表
     */
    private static final Map<RegisteredRouter.RouteMethod,Map<String,RegisteredRouter>> routeMap = new HashMap<>();

    public LibrariesClassLoader libLoader;

    static{
        routeMap.put(RegisteredRouter.RouteMethod.GET,new HashMap<>());
        routeMap.put(RegisteredRouter.RouteMethod.POST,new HashMap<>());
    }

    FloatPluginManager(String path){
        PLUGIN_PATH = FloatSphere.getRootPath()+path;
        processor = PluginProcessor.getInstance();
        logger = MessageBox.getLogger();
        Configuration prop = FloatSphere.createProperties();
        boolean enableReplaceSys = false;
        if(prop!=null){
            Object limit = prop.getValueByKey(SectionName.MAX_PLUGIN_LIMIT);
            if(limit!=null)
                MAX_PLUGIN = Integer.parseInt(String.valueOf(limit));
            Object replaceOut = prop.getValueByKey(SectionName.ENABLE_SYSTEM_OUT);
            if(replaceOut!=null){
                enableReplaceSys = Boolean.parseBoolean(String.valueOf(replaceOut));
            }
        }
        if(enableReplaceSys)
            System.setErr(new ExceptionPrinter(System.out));
    }
    @Override
    public void registerEventListener(Plugin plugin, Listener listener) {
        if(!plugin.isEnable()){
            MessageBox.getLogger().warn(plugin.getName()+"had disable!");
            return;
        }
        Map<Class<? extends Event>,Set<RegisteredListener>> map = processor.createRegistered(plugin,listener);
        for(Entry<Class<? extends Event>,Set<RegisteredListener>> entry : map.entrySet()){
            Class<? extends Event> eventClazz = entry.getKey();
            if(eventClazz!=null){
                HandlerList handlerList = getHandlerListByClazz(eventClazz);
                handlerList.registerAll(entry.getValue());
            }
        }
    }

    @Override
    public void registerRouters(Plugin plugin, Routers routers) {
        if(!plugin.isEnable()){
            MessageBox.getLogger().warn(plugin.getName()+"had disable!");
            return;
        }
        Set<RegisteredRouter> registeredRouterSet = processor.createRegisteredRouter(plugin,routers);
        registeredRouterSet.forEach(router ->{
            RegisteredRouter res = routeMap.get(router.getMethod()).computeIfAbsent(router.getRoute(),key->{
                routeMap.get(router.getMethod()).put(router.getRoute(),router);
                return null;
            });
            if(res!=null){
                logger.warnTips("Route "+res.getPlugin().getName()+":"+res.getRoute()+" conflicts with route "+router.getPlugin().getName()+":"+router.getRoute());
            }
        });
    }

    /**
     * 空方法
     * @param listener
     */
    @Deprecated
    @Override
    public void registerEventListener(Listener listener) {

    }

    /**
     * 注销掉所有此插件注册过的监听器
     * @param plugin 删除的插件
     */
    public void unregisterEventListener(Plugin plugin){
        if(plugin.isEnable())
            return;
        HandlerList.unregisterAll(plugin);
    }
    /**
     * 注销掉所有此插件注册过的路由
     * @param plugin 删除的插件
     */
    public void unregisterRouter(Plugin plugin){
        if(plugin.isEnable())
            return;
        removeRouteInMap(plugin, RegisteredRouter.RouteMethod.GET);
        removeRouteInMap(plugin, RegisteredRouter.RouteMethod.POST);
    }

    /**
     * 删除路由表中所有这个插件的路由
     * @param plugin 要删除路由的插件
     * @param method 路由的方法类型
     */
    public void removeRouteInMap(Plugin plugin, RegisteredRouter.RouteMethod method){
        Iterator<Entry<String,RegisteredRouter>> iterator = routeMap.get(method).entrySet().iterator();
        while (iterator.hasNext()){
            RegisteredRouter router = iterator.next().getValue();
            if(router.getPlugin()==plugin)
                iterator.remove();
        }
    }


    @Override
    public void enablePlugins() {
        if(libLoader==null){
            URL[] urls = new URL[1];
            try {
                URL url = new File(FloatSphere.getRootPath(),"libs").toURI().toURL();
                urls[0] = url;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            libLoader = new LibrariesClassLoader(urls,FloatPluginManager.class.getClassLoader());
            MessageBox.getLogger().warnTips("loaded libs:");
            for(String name : libLoader.getLibsName())
                MessageBox.getLogger().log(name);
        }
        long start = System.currentTimeMillis();
        int number = 0;
        number = loadPlugins();
        logger.log("Complete loads all plugins\nSuccess Loaded "+number+" Plugin Spend "+(System.currentTimeMillis()-start)+"ms");
    }

    /**
     * 读取plugins文件夹的内容
     * 文件夹不存在的话创建新的文件夹
     * 将所有jar包读取到内存中,并不是读取jar包字节码数据,而是读取jar包的配置信息
     * 根据每个插件的配置信息,对所有的插件进行拓扑排序,保证形成一个合理的插件加载时序链
     * 排序后,有序的将所有的插件,同步加载
     * @return 返回成功加载的插件的数目
     */
    protected int loadPlugins(){
        File file = new File(PLUGIN_PATH);
        File[] files;
        boolean flag = true;
        if(!file.exists())
            flag = file.mkdirs();
        files = file.listFiles(pathname -> pathname.getName().endsWith(".jar"));
        ArrayList<PluginEntry> pluginEntries = new ArrayList<>();
        if(files!=null&&flag){
            for (File value : files){
                try {
                    pluginEntries.add(FloatSphere.createPluginEntry(value));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            TopologyMachine topologyMachine = FloatSphere.createTopologyMachine();
            topologyMachine.sort(pluginEntries);
        }
        long  start;
        for(PluginEntry pluginEntry : pluginEntries){
            start = System.currentTimeMillis();
            try {
                if(processor.getPluginNumber()>=MAX_PLUGIN)
                    break;
                loadPlugin(pluginEntry);//根据插件的配置信息加载插件
            } catch (Exception | NoClassDefFoundError e) {
                MessageBox.getLogger().warn("["+pluginEntry.getPluginName()+"] crashed and your plugin could not be loaded!\n");
                e.printStackTrace();
            }
            MessageBox.getLogger().broadcastPlugin(pluginEntry.getPluginName(),start-pluginEntry.getStartTime());
        }
        return processor.getPluginNumber();
    }

    /**
     * 尝试注销掉插件
     * 不能保证百分百注销,所以需要插件自己在onDisable里面释放一些资源
     * @param pluginName 要注销的插件
     */
    @Override
    public void disablePlugin(String pluginName) {
        processor.disablePlugin(((PluginProcessor)this.processor).getPlugin(pluginName));
        System.gc();
    }

    /**
     * 尝试注销所有的插件
     * 不能保证百分百注销,但是会唤醒所有的onDisable,让插件处理要处理的数据
     */
    @Override
    public void disablePlugins() {
        for (String name : getPluginList()) {
            processor.disablePlugin(((PluginProcessor)this.processor).getPlugin(name));
        }
        libLoader.disableAllPlugin();
        try {
            libLoader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            System.gc();
        }
    }

    /**
     * 开放给用户的加载插件接口
     * @param pluginName 插件的名称
     * @throws Exception 报错
     */
    @Override
    public void loadPlugin(String pluginName) throws Exception {
        PluginEntry targetPlugin;
        File pluginFile = searchFileByName(pluginName,true);
        if(pluginFile==null){
            MessageBox.getLogger().warn("The plugin "+pluginName+" is not a plugin or can not find!");
            return;
        }
        targetPlugin = FloatSphere.createPluginEntry(pluginFile);
        try{
            this.processor.loadPlugin(targetPlugin,libLoader);
        }
        catch (NullPointerException e){
            assert targetPlugin != null;
            MessageBox.getLogger().warn("We can not load this plugin ->" + targetPlugin.getPluginName());
        }
    }

    @Override
    public List<String> getPluginList() {
        List<String> strList = null;
        if(processor!=null)
            strList = processor.getPluginList();
        return strList;
    }

    @Override
    public void noticeDis(String name) {
        disablePlugins.add(name);
    }

    /**
     * 从plugins目录下找插件
     * @param fileName 插件的文件名称或独一名称
     * @return 返回插件文件或对象
     */
    public File searchFileByName(String fileName,boolean checkInner){
        String targetName = fileName+".jar";
        File dataFolder = new File(PLUGIN_PATH);
        File ans = null;
        if(dataFolder.exists()){
            if(dataFolder.isDirectory()){
                File[] files = dataFolder.listFiles();
                if(files!=null){
                    int i = 0;
                    while(i<files.length){
                        File tempFile = null;
                        do{
                            if(i>=files.length)
                                break;
                            tempFile = files[i++];
                        }while(!tempFile.getName().equals(targetName));
                        if(tempFile.getName().equals(targetName)){
                            ans = tempFile;
                            break;
                        }
                    }
                    if(ans==null&&checkInner){
                        File tempFile = null;
                        i = 0;
                        while(i<files.length){
                            do{
                                if(i>=files.length)
                                    break;
                                tempFile = files[i++];
                            }while(!tempFile.getName().endsWith(".jar"));
                            if(!tempFile.getName().endsWith(".jar"))
                                break;
                            Configuration pluginYml = ConfigurationUtil.getPluginConfigYML(tempFile);
                            if(pluginYml!=null){
                                String name = pluginYml.getString("name");
                                if(name.concat(".jar").equals(targetName)){
                                    ans = tempFile;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return ans;
    }

    /**
     * 系统内部调用的加载插件接口
     * @param entry 插件对象
     * @throws Exception 报错
     */
    private void loadPlugin(PluginEntry entry) throws Exception {
        processor.loadPlugin(entry,libLoader);
    }
    protected void fireEvent(Event event,RegisteredListener[] registeredListeners){
        for(RegisteredListener listener : registeredListeners){
            Plugin plugin = listener.getPlugin();
            if(plugin.isEnable()){
                if(((Cancelable)event).isCancel())
                    break;
                listener.callEvent(event);
            }
        }
    }

    /**
     * 返回路由对应的路由器
     * @param method 路由的方法类型
     * @param route 路由的地址
     * @param mLength 路由方法的参数长度
     * @return 返回路由处理器
     */
    @Override
    public RegisteredRouter getRouter(String method, String route,int length,int mLength) {
        RegisteredRouter ret = null;
        Map<String,RegisteredRouter> map = routeMap.get(RegisteredRouter.RouteMethod.valueOf(method));
        ret = map.get(route);
        if(ret==null)
            return null;
        if(ret.getMethod()== RegisteredRouter.RouteMethod.POST)
            return ret;
        if(mLength==-1){
            /**
             * 两种可能:
             * 1。一个无参的普通的GET请求
             * 2。一个带有length参数的REST请求
             */

            if(ret.getParamLength()!=length||ret.getMethodParamsLength()!=0)
                ret = null;
        }else{
            if(ret.hasTags(MapParam.class)&&length==0)
                return ret;
            if(ret==null||ret.getMethodParamsLength()!=mLength)
                ret = null;
        }
        return ret;
    }
}
