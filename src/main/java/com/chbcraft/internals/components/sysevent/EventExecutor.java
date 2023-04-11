package com.chbcraft.internals.components.sysevent;

import com.chbcraft.internals.components.Listener;

import java.lang.reflect.InvocationTargetException;

public interface EventExecutor {
    public void executor(Listener listener,Event event) throws InvocationTargetException, IllegalAccessException;
}
