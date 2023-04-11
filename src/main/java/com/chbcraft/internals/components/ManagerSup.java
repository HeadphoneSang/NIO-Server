package com.chbcraft.internals.components;

import com.chbcraft.internals.components.listen.Cancelable;
import com.chbcraft.internals.components.listen.HandlerList;
import com.chbcraft.internals.components.listen.RegisteredListener;
import com.chbcraft.internals.components.sysevent.Event;

import org.jetbrains.annotations.NotNull;



import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class ManagerSup{
    public void callEvent(@NotNull Event event) {
        if(event instanceof Cancelable)
        {
            if(((Cancelable) event).isCancel())
                return;
        }
        HandlerList handlerList = event.getHandler();
        if(handlerList==null){
            MessageBox.getLogger().warn("The Event can not search a static HandlerList ->"+event.getEventType());
            return;
        }
        fireEvent(event,handlerList.getListeners());
    }
    /**
     * 从Class中获得静态变量HandlerList
     * @param type Class对象
     * @return 返回HandlerList或者Null
     */
    HandlerList getHandlerListByClazz(Class<? extends Event> type){
        try {
            Method method = searchClassAll(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 递归查找HandlerList方法
     * @param clazz 当前查找的Class对象
     * @return 返回含有这个方法的Class对象
     */
    private Class<? extends Event> searchClassAll(Class<? extends Event> clazz){
        try{
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        } catch (NoSuchMethodException e) {
            Class<?> tempClass = clazz.getSuperclass();
            if(!tempClass.equals(Event.class)&&Event.class.isAssignableFrom(tempClass)){
                return searchClassAll(tempClass.asSubclass(Event.class));
            }
            else{
                MessageBox.getLogger().warn("Can not find a static method named getHandlerList in "+clazz.getSimpleName());
                throw new IllegalArgumentException();
            }
        }
    }
    abstract void fireEvent(Event event, RegisteredListener[] registeredListeners);
}
