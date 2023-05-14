package com.chbcraft.internals.components.entries.config;

import com.chbcraft.internals.components.entries.BaseConfig;
import com.chbcraft.internals.components.entries.ConfigSection;
import com.chbcraft.internals.components.entries.Section;
import com.chbcraft.internals.components.enums.SectionName;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Configuration implements BaseConfig {
    protected Object value;
    protected File originalFile;
    protected Map<String,Section> sections;
    protected Configuration(){
        sections = new HashMap<>();
    }
    public long getLong(String key){
        long ret;
        try{
            ret = Long.parseLong(getString(key));
        }catch (NumberFormatException e){
            ret = -1;
        }
        return ret;
    }
    @Override
    public double getDouble(String key) {
        String str = getString(key);
        double result;
        try{
            result = Double.parseDouble(str);
        }catch (NumberFormatException e){
            result = -1D;
        }
        return result;
    }

    @Override
    public int getInt(String key) {
        String str = getString(key);
        int result;
        try{
            result = Integer.parseInt(str);
        }catch (NumberFormatException e){
            result = -1;
        }
        return result;
    }

    @Override
    public boolean getBoolean(String key) {
        String str = getString(key);
        return Boolean.parseBoolean(str);
    }

    @Override
    public String getString(String key) {
        Object obj;
        if(key.contains("/")){
            obj = getValueByKeys(key);
        }else
            obj = getValueByKey(key);
        return obj==null?null:String.valueOf(obj);
    }
    /**
     * 通过键值获得Section
     * @param key 键值
     * @return 对应的Section,没有返回null
     */
    @Override
    public Section getSectionByKey(String key) {
        Section section0 = null;
        section0 = sections.getOrDefault(key,null);
        return section0;
    }

    /**
     * 通过键值获得属性值
     * @param key 键值
     * @return 返回属性
     */
    @Override
    public Object getValueByKey(String key) {
        Object value = null;
        Section section = getSectionByKey(key);
        if(section!=null)
            value = section.getValue();
        return value;
    }
    @Override
    public Object getValueByKey(SectionName sectionName){
        return getValueByKey(sectionName.value());
    }

    /**
     * 增加新的键值对Section
     * @param key 键值
     * @param value 属性
     */
    @Override
    public void addValueByKey(String key, Object value) {
        Section section = new ConfigSection(key,value);
        sections.put(key,section);
    }

    /**
     * 把内容写回磁盘
     */
    public void save(){
        boolean flag = true;
        if(originalFile==null){
            return;
        }
        if(!originalFile.getParentFile().exists()) {
            try {
                flag = originalFile.getParentFile().createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try{
            if(originalFile.exists()&&flag){
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(originalFile),StandardCharsets.UTF_8));
                String data = saveToString();
                if(data==null)
                    return;
                writer.write(data);
                writer.flush();
                writer.close();
            }
            else
            {
                if(originalFile.createNewFile())
                    save();
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 设置源文件
     * @param file 源文件
     */
    private void setOriginalFile(File file){
        if(file!=null){
            this.originalFile = file;
        }
    }

    @Override
    public void setSections(Map<String,Section> sections) {
        this.sections = sections;
    }

    @Override
    public Map<String,Section> getSections() {
        return this.sections;
    }
}
