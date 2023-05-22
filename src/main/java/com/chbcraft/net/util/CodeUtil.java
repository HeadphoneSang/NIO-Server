package com.chbcraft.net.util;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.enums.SectionName;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Base64;

public class CodeUtil {
    private CodeUtil(){}
    /**
     * 将BASE64加密的字符串解密
     * @param encodeStr 加密的字符串
     * @return 返回解密后的结果
     */
    public static String decodeBase64(String encodeStr){
        byte[] bytes;
        String ret = null;
        String charset = FloatSphere.getProperties().getString(SectionName.DECODE_CHARSET.value());
        try {
            encodeStr = URLDecoder.decode(encodeStr,charset);
            bytes = Base64.getDecoder().decode(encodeStr.getBytes(charset));
            ret = new String(bytes,0,bytes.length,charset);
        } catch (UnsupportedEncodingException | IllegalArgumentException e) {
            try {
                bytes = Base64.getDecoder().decode(encodeStr.replace(" ","+").getBytes(charset));
                ret = new String(bytes,0,bytes.length,charset);
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                unsupportedEncodingException.printStackTrace();
            }
        }
        if(ret==null)
            return encodeStr;
        return ret;
    }


    /**
     * 将字符串BASE64加密
     * @param name 要加密的字符串
     * @return 返回BASE64加密后
     */
    public static String encodeBase64(String name){
        String charset = FloatSphere.getProperties().getString(SectionName.DECODE_CHARSET.value());
        try {
            byte[] b = Base64.getEncoder().encode(name.getBytes(charset));
            String retStr = new String(b,0,b.length);
            retStr = URLEncoder.encode(retStr,charset);
            return retStr;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decodeURL(String url){
        try {
            return URLDecoder.decode(url,FloatSphere.getProperties().getString(SectionName.DECODE_CHARSET.value()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
