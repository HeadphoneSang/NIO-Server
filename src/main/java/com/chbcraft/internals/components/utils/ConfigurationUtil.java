package com.chbcraft.internals.components.utils;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.entries.NormalEntry;
import com.chbcraft.internals.components.enums.ConfigType;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ConfigurationUtil {
    private ConfigurationUtil(){};
    public static Configuration getPluginConfigYML(File file){
        if(!file.exists())
            return null;
        if(file.isDirectory())
            return null;
        if(!file.getName().endsWith(".jar"))
            return null;
        Configuration configuration = null;
        try (JarFile jarFile = new JarFile(file)){
            JarEntry entry = jarFile.getJarEntry("plugin.yml");
            Object obj = null;
            if(entry!=null){
                try(InputStream inputStream = jarFile.getInputStream(entry)){
                    Yaml yml = new Yaml();
                    obj = yml.load(inputStream);
                }
            }
            if(obj!=null){
                configuration = FloatSphere.createConfig(obj, ConfigType.TYPE_CONFIG);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return configuration;
    }
    /**
     * 转换Object转换为Map返回
     * @param obj 目标对象
     * @return 返回转换结果
     */
    @SuppressWarnings("unchecked")
    public static Map<String,Object> castToMap(Object obj){
        Map<String,Object> returnMap = null;
        if(obj instanceof Map)
            returnMap = (Map<String, Object>) obj;
        return returnMap;
    }
    @SuppressWarnings("unchecked")
    public static List<String> castToList(Object obj){
        List<String> list = null;
        if(obj instanceof List){
            list = ((List<Object>)obj).stream().map(String::valueOf).collect(Collectors.toList());
        }
        return list;
    }
    @SuppressWarnings("unchecked")
    public static ArrayList<Object> castToObjList(Object obj){
        ArrayList<Object> list = null;
        if(obj instanceof List)
            list = (ArrayList<Object>) obj;
        return list;
    }
}
