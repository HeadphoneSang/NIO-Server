package com.chbcraft.net.handlers.inbound.websocket.utils;

import com.alibaba.fastjson.JSONObject;
import com.chbcraft.net.handlers.inbound.websocket.pojo.WebFileResult;

public class ResultUtil {
    private ResultUtil(){}
    public static String getResultString(WebFileResult result,Object attach){
        JSONObject ret = new JSONObject();
        if(attach!=null){
            ret.put("data",attach);
        }
        ret.put("code",result.value());
        return ret.toJSONString();
    }
}
