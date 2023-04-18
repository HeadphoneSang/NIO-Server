package com.chbcraft.internals.base;

import com.chbcraft.internals.components.entries.config.Configuration;
import com.chbcraft.plugin.Plugin;

import java.net.URL;
import java.net.URLClassLoader;

public abstract class BaseComponentLoader extends URLClassLoader {
    public BaseComponentLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }
    public abstract void initialedPlugin(Plugin plugin, Configuration pluginDescription);
    public abstract void initialedPlugin(Plugin plugin);
}
