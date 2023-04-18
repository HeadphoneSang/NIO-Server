package com.chbcraft.internals.components.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {
    private RegexUtil(){}

    public static void main(String[] args) {
        getPathVariable("12131{1231231}11{12311111231}");
    }

    /**
     * 从REST路径地址中解析出{形参名}:index的映射表
     * @param path REST风格的路径
     * @return 返回映射表
     */
    public static Map<String,Integer> getPathVariable(String path){
        String regxVar = "(\\{(.*?)\\})";
        Pattern pattern = Pattern.compile(regxVar);
        Matcher matcher = pattern.matcher(path);
        Map<String,Integer> retMap = new HashMap<>();
        int index = 0;
        while(matcher.find())
            retMap.put(matcher.group(2),index++);
        return retMap;
    }

}
