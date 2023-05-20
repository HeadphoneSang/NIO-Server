package com.chbcraft.net.tranfer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class FrameUtil {
    private FrameUtil(){}

    public static void writeFrame(ByteBuf ret,int ptl,long data){
//        ret.writeBytes(JSON.toJSONString(new TransferFrame(ptl,data)).getBytes(StandardCharsets.UTF_8));

        ret.writeInt(ptl);
        ret.writeBytes((""+data).getBytes());
    }
}
