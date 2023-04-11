package com.chbcraft.internals.components.entries;

public class NormalEntry<T> {
    public String getKey() {
        return key;
    }
    public T getValue() {
        return value;
    }
    private String key;
    private T value;
    public NormalEntry(String key, T value){this.key = key;this.value = value;}
}
