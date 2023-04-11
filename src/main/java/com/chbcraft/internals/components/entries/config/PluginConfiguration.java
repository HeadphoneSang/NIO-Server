package com.chbcraft.internals.components.entries.config;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.entries.Section;

import java.util.List;

public class PluginConfiguration extends Configuration {
    public PluginConfiguration(){}
    /**
     * 通过嵌套键值查找属性
     * @param keys 嵌套键值
     * @return 返回对应的属性
     */
    @Override
    public Object getValueByKeys(String keys) {
        if(!keys.contains("/"))
            return null;
        String[] allKey = keys.split("/",2);
        Section section = getSectionByKey(allKey[0]);
        Object value = section.getValue();
        if(value instanceof List)
            return section.getValueByKey(allKey[1]);
        return value;
    }

    @Override
    public void setValueByKey(String key, Object value) {
        Section section;
        if((section=getSectionByKey(key))!=null){
            section.setValue(value);
        }
    }

    @Override
    public void setValueByKeys(String keys, Object value) {
        if(!keys.contains("/"))
            return;
        String[] allKey = keys.split("/",2);
        Section section = getSectionByKey(allKey[0]);
        Section resultSection = null;
        if(section!=null){
            resultSection = section.searchSectionByKey(allKey[1]);
        }
        if(resultSection==null){
            MessageBox.getLogger().warnTips("keys is incorrect!");
            return;
        }
        resultSection.setValue(value);
    }

    @Override
    public String saveToString() {
        return null;
    }
}
