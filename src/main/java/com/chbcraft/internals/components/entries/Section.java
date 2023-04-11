package com.chbcraft.internals.components.entries;

import java.util.ArrayList;
import java.util.List;

public abstract class Section {
    private String key = null;
    public Section(String key){
        this.key = key;
    }
    /**
     * 根据单个键值或多个键值返回对象 注:KEYS格式-> key1/key2/key3
     * @param key 键值
     * @return 返回对应的属性
     */
    public Object getValueByKey(String key) {
        Section section = null;
        String[] keys = key.split("/",2);
        section = searchSectionByKey(keys[0]);
        if(key.contains("/"))
            if(section !=null){
                if(section.getValue() instanceof List)
                    return section.getValueByKey(keys[1]);
                return "Unnecessary Keys!";
            }
        return section!=null?section.getValue():"Wrong Keys!";
    }
    public abstract Section searchSectionByKey(String key);
    public abstract Object getValue();
    public String getKey(){
        return this.key;
    }
    public void setKey(String key){
        this.key = key;
    }
    public abstract void setValue(Object value);
    public abstract void setChildSection(ArrayList<ConfigSection> childSections);
}
