package com.chbcraft.main;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.*;
import com.chbcraft.internals.components.sysevent.PluginCommandEvent;
import com.chbcraft.io.event.ChannelEvent;
import com.chbcraft.io.net.AcceptorLoop;

import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String[] args) throws Exception {
        MessageBox log = MessageBox.getLogger();
        //获得一个插件管理器
        FloatPluginManager manager = (FloatPluginManager) FloatSphere.getPluginManager();
        manager.enablePlugins();

        AcceptorLoop acceptor = new AcceptorLoop(null);
        ExecutorService loop = new ThreadPoolExecutor(10,50,10, TimeUnit.SECONDS,new ArrayBlockingQueue<>(10));
//        FloatSphere.getPluginManager().registerEventListener();
        loop.execute(()->{
            Scanner scanner = new Scanner(System.in);
            while(true){
            String command = scanner.nextLine();
            manager.callEvent(new PluginCommandEvent(command));
            }
        });

    }
}

