package com.chbcraft.internals.components.entries.config;

import com.chbcraft.internals.components.entries.config.Configuration;

import java.util.Enumeration;
import java.util.Properties;

public class PropertiesConfiguration extends Configuration {
    public PropertiesConfiguration(Properties prop){
        Enumeration<?> names = prop.propertyNames();
        while(names.hasMoreElements()) {
            String name = (String) names.nextElement();
            addValueByKey(name, prop.getProperty(name));
        }
    }
    @Override
    public Object getValueByKeys(String keys) {
        return null;
    }

    @Override
    public void setValueByKey(String key, Object value) {

    }
    @Override
    public void setValueByKeys(String key, Object value) {

    }

    @Override
    public String saveToString() {
        return null;
    }
}
