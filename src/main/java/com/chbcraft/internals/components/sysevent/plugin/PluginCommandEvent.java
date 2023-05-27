package com.chbcraft.internals.components.sysevent.plugin;

import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.sysevent.PluginEvent;

public class PluginCommandEvent extends PluginEvent {
    private final String command;
    private String[] args ;
    private static final HandlerList handlerList = new HandlerList();
    public PluginCommandEvent(String command) throws ArrayIndexOutOfBoundsException{
        String[] temp = command.split(" ");
        int length = temp.length;
        this.command = temp[0];
        if(temp.length>1){
            args = new String[length-1];
            System.arraycopy(temp,1,args,0,length-1);
        }
    }
    @Override
    protected HandlerList getHandler() {
        return handlerList;
    }
    public String getCommand(){
        return this.command;
    }
    public String[] getArgs(){
        return this.args;
    }
    private static HandlerList getHandlerList(){
        return handlerList;
    }
}
