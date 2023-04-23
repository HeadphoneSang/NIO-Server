package com.chbcraft.internals.base;

import com.chbcraft.internals.components.FloatPluginManager;
import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.MessageBox;
import com.chbcraft.internals.components.PluginManager;
import com.chbcraft.internals.components.sysevent.PluginCommandEvent;
import com.chbcraft.net.HttpProcessor;
import io.netty.util.concurrent.DefaultPromise;

import java.util.Scanner;

public class CommandLiner extends Thread{

    private final PluginManager manager = FloatSphere.getPluginManager();

    private final HttpProcessor processor;

    public CommandLiner(HttpProcessor processor){
        this.processor =processor;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while(true){
            String command;
            try{
                command = scanner.nextLine();
                if(command.equalsIgnoreCase("stop"))
                {
                    manager.disablePlugins();
                    DefaultPromise<?> promise = (DefaultPromise<?>) processor.shutdown();
                    promise.addListener(future -> {
                        if(future.isSuccess()){
                            MessageBox.getLogger().log("stop server success");
                            System.exit(0);
                        }
                    });
                }
            }catch (Exception e){
                continue;
            }
            try{
                manager.callEvent(new PluginCommandEvent(command));
            }catch (ArrayIndexOutOfBoundsException e){
                MessageBox.getLogger().warn("? illegal command format");
            }
        }
    }
}
