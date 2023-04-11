package com.chbcraft.internals.components;

import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.entries.config.Configuration;
import org.fusesource.jansi.Ansi;

import java.util.Calendar;

import static org.fusesource.jansi.Ansi.ansi;

public class MessageBox {
    private volatile static MessageBox logger = null;
    private boolean isOpen = true;
    private final String messageHeader;
    private final Calendar dateMechain;

    /**
     * 消息盒子的构造器
     * @param messageHeader 信息头
     */
    public MessageBox(String messageHeader){
        if(messageHeader.equals(""))
            messageHeader = "[Console";
        this.messageHeader = "["+messageHeader;
        dateMechain = Calendar.getInstance();
        Configuration prop = null;
        prop = FloatSphere.createProperties();
        if(prop!=null)
            isOpen = prop.getBoolean(SectionName.PLUGIN_LOADING.value());
    }

    /**
     * 广播普通的消息
     * @param message 消息内容
     */
    public void log(String message){
        System.out.println(split(message));
    }

    /**
     * 广播错误信息
     * @param message 错误消息
     */
    public void error(String message){
        error(message, Ansi.Color.RED);
    }
    public void error(String message, Ansi.Color color){
        System.out.println(ansi().fg(color).a(split(message)).fg(Ansi.Color.DEFAULT));
    }

    /**
     * 广播加载插件时遇到的Exception时的提示
     * @param reason 可能的原因
     */
    public void warn(String reason){
        reason = "Possible error causes:"+"\nCaused by: "+reason;
        System.out.print(ansi().fg(Ansi.Color.RED).a(reason).fg(Ansi.Color.DEFAULT));
    }
    public void warnTips(String reason){
        error(reason, Ansi.Color.YELLOW);
    }

    /**
     * 对消息进行格式化处理
     * @param message 播放的信息
     * @return 返回处理好的消息
     */
    private String split(String message){
        return (messageHeader+" "+ dateMechain.getTime() +"]").replace(" ","-")+message;
    }

    /**
     * 广播插件加载刚开始的消息
     * @param name 插件的名字
     */
    public void broadcastPluginLoading(String name){
        if(isOpen)
            log("[PluginLoading]["+name+"] has initializing!");
    }

    /**
     * 广播插件加载时遇到错误是的提示信息
     * @param name 插件的名字
     */
    public void broadcastPluginWarn(String name){
        if(isOpen)
            error("[PluginLoading]["+name+"] had initialized! failed Loading!");
    }

    /**
     * 广播插件加载信息
     * @param name 插件名称
     * @param start 插件加载开始的时间
     */
    public void broadcastPlugin(String name,long start){
        if (isOpen)
            log("[PluginLoading]["+name+"] has initialized!"+" Spend "+(System.currentTimeMillis()-start)+"ms");
    }

    /**
     * 获得一个消息盒子对象(单例)
     * @return 返回一个消息盒子
     */
    public static MessageBox getLogger(){
        if (logger==null){
            logger = new MessageBox("CONSOLE");
        }
        return logger;
    }
}
