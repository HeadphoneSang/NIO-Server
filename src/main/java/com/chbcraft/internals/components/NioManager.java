package com.chbcraft.internals.components;

public interface NioManager extends AbstractManager{
    @Override
    void registerEventListener(Listener listener);

}
