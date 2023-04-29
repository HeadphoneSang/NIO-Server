package com.chbcraft.net.util;

import com.chbcraft.internals.components.FloatSphere;
import com.chbcraft.internals.components.enums.SectionName;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
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

    public static String decodeURL(String url){
        try {
            return URLDecoder.decode(url,FloatSphere.getProperties().getString(SectionName.DECODE_CHARSET.value()));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
