package com.chbcraft.main;
import com.chbcraft.internals.base.CommandLiner;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.*;
import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.sysevent.PluginCommandEvent;
import com.chbcraft.net.HttpProcessor;
import com.chbcraft.net.NioHttpAcceptor;
import io.netty.util.concurrent.DefaultPromise;

import java.util.Scanner;

public class Main {

    public static void main(String[] args){
        /**
         * 获得框架管理器
         */
        FloatPluginManager manager = (FloatPluginManager) FloatSphere.getPluginManager();
        /**
         * 加载所有的依赖和插件
         */
        manager.enablePlugins();

        /**
         * 启动网络组件
         */
        HttpProcessor processor = new HttpProcessor(new NioHttpAcceptor(FloatSphere.getProperties().getInt(SectionName.SERVER_PORT.value())));
        processor.execute();
        /**
         * 启动单独的一个线程去阻塞接受命令行消息
         */
        new CommandLiner(processor).start();
    }
}

