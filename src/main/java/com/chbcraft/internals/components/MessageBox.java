package com.chbcraft.internals.components;

import com.chbcraft.internals.components.enums.SectionName;
import com.chbcraft.internals.components.entries.config.Configuration;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class MessageBox {
    private static MessageBox loggerBox = null;
    private boolean isOpen = true;
    private final Logger logger;
 
    /**
     * 消息盒子的构造器
     * @param messageHeader 信息头
     */
    public MessageBox(String messageHeader){
        if(messageHeader.equals(""))
            messageHeader = "CONSOLE";
        this.logger = LogManager.getLogger(messageHeader);
        LoggerContext.getContext(false).getLogger(this.logger.getName()).setLevel(Level.INFO);
        Configuration prop = null;
        prop = FloatSphere.createProperties();
        if(prop!=null)
            isOpen = prop.getBoolean(SectionName.PLUGIN_LOADING.value());
    }

    /**
     * 广播普通的消息
     * @param message 消息内容
     */
    @Deprecated
    public void log(Object message){
        logger.info(message);
    }

    public void log(String message){logger.info(message);}

    public void log(Object message,Exception e){
        logger.info(message,e);
    }

    public void log(String message,Object...params){
        logger.info(message,params);
    }

    /**
     * 广播错误信息
     * @param message 错误消息
     */
    @Deprecated
    public void error(Object message){
        logger.fatal(message);
    }

    public void error(String message){
        logger.error(message);
    }

    public void error(String msg,Object...params){
        logger.fatal(msg,params);
    }

    /**
     * 广播错误信息
     * @param message 错误消息
     */
    public void error(Object message,Exception e){
        logger.fatal(message,e);
    }

    /**
     * 广播加载插件时遇到的Exception时的提示
     * @param reason 可能的原因
     */
    @Deprecated
    public void warn(Object reason){
        logger.error(reason);
    }
    public void warn(String msg,Object...params){
        logger.error(msg,params);
    }
    public void warn(Object msg,Exception e){
        logger.error(msg,e);
    }

    public void warnTips(String msg){
        logger.warn(msg);
    }

    @Deprecated
    public void warnTips(Object msg){
        logger.warn(msg);
    }
    public void warnTips(Object msg,Exception e){
        logger.warn(msg,e);
    }
    public void warnTips(String msg,Object...params){
        logger.warn(msg,params);
    }

    public void trace(Object msg){
        logger.trace(msg);
    }

    public void trace(String msg,Object...params){
        logger.trace(msg, params);
    }

    public void debug(Object obj){
        logger.debug(obj);
    }

    public void debug(String msg,Object...params){
        logger.debug(msg,params);
    }

    /**
     * 广播插件加载时遇到错误是的提示信息
     * @param name 插件的名字
     */
    public void broadcastPluginWarn(Object name){
        if(isOpen)
            warnTips("[PluginLoading]["+name+"] had initialized! failed Loading!");
    }

    /**
     * 广播插件加载信息
     * @param name 插件名称
     * @param start 插件加载开始的时间
     */
    public void broadcastPlugin(Object name,long start){
        if (isOpen)
            log("[PluginLoading]["+name+"] has initialized!"+" Spend "+(System.currentTimeMillis()-start)+"ms");
    }

    /**
     * 获得一个消息盒子对象(单例)
     * @return 返回一个消息盒子
     */
    public static MessageBox getLogger(){
        if (loggerBox==null){
            loggerBox = new MessageBox("CONSOLE");
        }
        return loggerBox;
    }
}
