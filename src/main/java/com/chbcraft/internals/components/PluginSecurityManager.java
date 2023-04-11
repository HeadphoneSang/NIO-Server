package com.chbcraft.internals.components;

import com.chbcraft.plugin.Plugin;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Objects;

public class PluginSecurityManager extends SecurityManager{
    private static volatile PluginSecurityManager INSTANCE;
    private static File pluginsFileDir;
    private static final ArrayList<String> allowFile = new ArrayList<>();
    private PluginSecurityManager(){
        String path = FloatSphere.getRootPath()+"/plugins/";
        MessageBox.getLogger().log(" Security start success!");
        PluginSecurityManager.pluginsFileDir = new File(path);
        if(!pluginsFileDir.exists()){
            if(!pluginsFileDir.mkdirs()){
                throw new IllegalArgumentException();
            }
        }
        for(File nowFile : Objects.requireNonNull(pluginsFileDir.listFiles(),"plugins is not find!")){
            if(nowFile.getName().endsWith(".jar"))
                allowFile.add(nowFile.getAbsolutePath());
        }
    }

    /**
     * 添加一个文件的操作权限
     * @param file 添加的文件
     */
    void addPermission(File file){
        if(!PluginSecurityManager.allowFile.contains(file.getAbsolutePath())){
            PluginSecurityManager.allowFile.add(file.getAbsolutePath());
        }
    }
    /**
     *  获得唯一的插件IO权限管理器
     * @return 返回插件管理器
     */
    public synchronized static PluginSecurityManager getManager(){
        if (INSTANCE == null) {
            if (INSTANCE == null) {
                INSTANCE = new PluginSecurityManager();
            }
        }
        return PluginSecurityManager.INSTANCE;
    }

    /**
     * 插件给自己注册安全域
     * @param plugin 注册插件
     */
    public static void registerPluginSecurity(Plugin plugin){
        String pluginName = plugin.getName();
        if(!plugin.getName().startsWith("/"))
            pluginName = "/"+pluginName;
        PluginSecurityManager.allowFile.add(pluginsFileDir.getAbsolutePath()+plugin.getName());
    }
    @Override
    public void checkRead(String file) {
        if(allowFile.contains(file)||file.contains("Java\\jdk"))
            return;
        super.checkRead(file);
    }
    @Override
    public void checkWrite(String file) {
        if(allowFile.contains(file))
            return;
        super.checkWrite(file);
    }

    @Override
    public void checkPermission(Permission perm) {

    }

    @Override
    public void checkPackageAccess(String pkg) {

    }

    @Override
    public void checkAccess(ThreadGroup g) {

    }

    @Override
    public void checkCreateClassLoader() {

    }

    @Override
    public Object getSecurityContext() {
        return super.getSecurityContext();
    }

    @Override
    public void checkDelete(String file) {
        if(allowFile.contains(file))
            return;
        super.checkDelete(file);
    }
}
