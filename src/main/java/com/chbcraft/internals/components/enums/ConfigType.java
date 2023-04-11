package com.chbcraft.internals.components.enums;

public enum ConfigType {
    TYPE_PLUGIN(1),
    TYPE_CONFIG(1 << 1);
    private final int typeValue;

    private ConfigType(int value){
        this.typeValue = value;
    }

    public int getTypeValue() {
        return typeValue;
    }
}
