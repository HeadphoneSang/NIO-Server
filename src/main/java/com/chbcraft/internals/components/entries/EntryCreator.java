package com.chbcraft.internals.components.entries;

import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.internals.components.utils.ConfigurationUtil;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class EntryCreator {
    public PluginEntry createPluginEntry(File jarFile) throws FileNotFoundException {
        Configuration pluginYml = ConfigurationUtil.getPluginConfigYML(jarFile);
        if(pluginYml==null){
            MessageBox.getLogger().warn(jarFile.getName()+" do not have a Plugin.yml,Loaded failed!");
            throw new FileNotFoundException(jarFile.getAbsolutePath());
        }
        Object pluginNameObj = pluginYml.getValueByKey("name");
        Object pluginDependsObj = pluginYml.getValueByKey("after");
        Object pluginBeforeObj = pluginYml.getValueByKey("before");
        if(pluginNameObj==null){
            MessageBox.getLogger().warn(jarFile.getName()+" Plugin.yml do not have a Name,Loaded failed!");
            throw new FileNotFoundException(jarFile.getAbsolutePath());
        }
        String pluginName = String.valueOf(pluginNameObj);
        PluginEntry pluginEntry;
        List<String> depends = null;
        List<String> before = null;
        if(pluginDependsObj!=null){
            if(pluginDependsObj instanceof List){
                depends = ConfigurationUtil.castToList(pluginDependsObj);
            }else {
                depends = new ArrayList<String>(){{add(String.valueOf(pluginDependsObj));}};
            }
        }
        if(pluginBeforeObj!=null){
            if(pluginBeforeObj instanceof List){
                before = ConfigurationUtil.castToList(pluginBeforeObj);
            }
            else {
                before = new ArrayList<String>(){{add(String.valueOf(pluginBeforeObj));}};
            }
        }
        return new PluginEntry(pluginName,pluginYml,jarFile,before,depends);
    }
}
