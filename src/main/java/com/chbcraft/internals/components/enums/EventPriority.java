package com.chbcraft.internals.components.enums;

/**
 * 监听器的优先级
 */
public enum EventPriority {
    /**
     * 最高优先级
     * 最先触发监听事件
     */
    HIGHEST(1),
    /**
     * 高优先级
     * 第二个监听
     */
    HIGHER(1 << 1),
    /**
     * 中等优先级
     * 第三个监听
     */
    MIDDLE(1 << 2),
    /**
     * 第优先级第四个加载
     */
    LOWER(1 << 3),
    /**
     * 最低的优先级
     * 最后加载
     */
    LOWEST(1 << 4);
    private final int priority;
    EventPriority(int priority){
        this.priority = priority;
    }
    public int getPriority(){
        return this.priority;
    }
}
