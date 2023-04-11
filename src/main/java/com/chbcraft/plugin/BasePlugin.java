package com.chbcraft.plugin;

import com.chbcraft.exception.DuplicatePluginMainException;
import com.chbcraft.internals.components.PluginClassLoader;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public abstract class BasePlugin implements Plugin {
    private boolean isEnable = true;

    @Override
    public void disable() {
        this.isEnable = false;
    }

    public BasePlugin(){
        ClassLoader loader = this.getClass().getClassLoader();
        if(loader instanceof  PluginClassLoader){
            if(((PluginClassLoader)loader).getPlugin()!=null)
                throw new DuplicatePluginMainException(((PluginClassLoader) loader).getPluginName());
        }
    }

    @Override
    public boolean isEnable() {
        return isEnable;
    }

    public void enablePlugin(){
        this.isEnable = true;
    }
    public void disablePlugin(){
        this.isEnable = false;
    }
    public boolean checkEnable(){
        return this.isEnable;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj == this){
            return true;
        }
        else if(obj == null){
            return false;
        }
        else{
            return obj instanceof Plugin && this.getName().equals(((Plugin) obj).getName());
        }
    }
    protected Map<String,Object> getFileMap(InputStream inputStream){
        Yaml yaml = new Yaml();
        Object map = yaml.load(inputStream);
        if(map instanceof Map){
            return ConfigurationUtil.castToMap(map);
        }
        return null;
    }
}
