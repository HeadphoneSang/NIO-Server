package com.chbcraft.internals.components.entries;

import com.chbcraft.internals.components.enums.SectionName;

import java.util.ArrayList;
import java.util.Map;

public interface BaseConfig {
    /**
     * 通过键值获得浮点值
     * @param key 键值 允许层级键值key1/key2
     * @return 返回浮点值或键值
     */
    double getDouble(String key);
    /**
     * 通过key获得整数值
     * @param key 键值 允许层级键值key1/key2
     * @return 返回int或者null
     */
    int getInt(String key);
    /**
     * 通过key获得布尔值
     * @param key 键值 允许层级键值key1/key2
     * @return 返回布尔值或者null
     */
    boolean getBoolean(String key);
    /**
     * 通过key获得字符串
     * @param key 键值 允许层级键值key1/key2
     * @return 返回属性字符串或者null
     */
    String getString(String key);
    /**
     * 通过键值获得Section
     * @param key 键值 允许层级键值key1/key2
     * @return 对应的Section
     */
    Section getSectionByKey(String key);
    /**
     * 通过键值获得属性值
     * @param keys 键值列 例如 key1/key2
     * @return 返回属性
     */
    Object getValueByKeys(String keys);
    /**
     * 通过键值获得属性值
     * @param key 键值 允许层级键值key1/key2
     * @return 返回属性
     */
    Object getValueByKey(String key);

    /**
     * 通过键值枚举获得属性对象
     * @param sectionName 键值名
     * @return 返回属性
     */
    Object getValueByKey(SectionName sectionName);

    /**
     * 设置键值通过键值对
     * @param key 键
     * @param value 值
     */
    void setValueByKey(String key, Object value);

    /**
     * 通过嵌套键值对设置属性
     * @param keys 键值列 例如 key1/key2
     * @param value 属性
     */
    void setValueByKeys(String keys, Object value);

    /**
     * 通过键值对增加属性
     * @param key 键值
     * @param value 属性
     */
    void addValueByKey(String key, Object value);
    /**
     * 将内存对象转换为字符串
     * @return 返回字符串
     */
    String saveToString();

    /**
     * 设置所有的内容
     * @param obj 返回内容
     */
    void setSections(Map<String,Section> obj);

    /**
     * 获得所有的内容
     * @return 返回内容或Null
     */
    Map<String,Section> getSections();
}
