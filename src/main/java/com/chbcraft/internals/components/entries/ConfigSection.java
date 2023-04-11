package com.chbcraft.internals.components.entries;

import com.chbcraft.internals.components.utils.ConfigurationUtil;

import java.util.ArrayList;
import java.util.Map;

public class ConfigSection extends Section{
    /**
     *
     */
    private ArrayList<ConfigSection> childSections = null;
    /**
     *
     */
    private Object value = null;

    /**
     *
     * @param key 选项的键值
     * @param value 选项的属性
     */
    public ConfigSection(String key,Object value){
        super(key);
        if(value instanceof Map){
            childSections = new ArrayList<>();
            for(Map.Entry<String,Object> entry: ConfigurationUtil.castToMap(value).entrySet()) {
                childSections.add(new ConfigSection(entry.getKey(), entry.getValue()));
            }
            this.value = childSections;
        }else
            this.value = value;
    }

    /**
     * 设置子选项页
     * @param childSections 子选项页
     */
    public void setChildSection(ArrayList<ConfigSection> childSections) {
        this.childSections = childSections;
    }

    public String getKey() {
        return super.getKey();
    }

    public void setKey(String key) {
        super.setKey(key);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
    /**
     * 通过索引查找子选项
     * @param key 键值 可以是单键值,或者是多键值key1/key2/key3
     * @return 返回子选项或者null
     */
    @Override
    public Section searchSectionByKey(String key){
        String[] allKey = key.split("/",2);
        Section nowSection = null;
        for(Section check : childSections){
            if(check.getKey().equals(allKey[0])){
                nowSection = check;
                break;
            }
        }
        if(nowSection==null)
            return null;
        if(allKey.length==1){
            return nowSection;
        }
        return nowSection.searchSectionByKey(allKey[1]);
    }
}
