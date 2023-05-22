package com.chbcraft.net.tranfer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.util.Map;

public class TransferFrame {
    // 帧数据
    private final int protocol;
    // 数据长度|数据坐标
    private final long data;
    // 附加数据
    private Map<String,Object> addition;

    public TransferFrame(int protocol,long data){
        this.protocol = protocol;
        this.data = data;
    }

    public TransferFrame(int protocol,long data,Map<String,Object> addition){
        this.protocol = protocol;
        this.data = data;//262144
        this.addition = addition;
    }

    public int getProtocol() {
        return protocol;
    }

    public long getData() {
        return data;
    }

    public Map<String, Object> getAddition() {
        return addition;
    }

    public void setAddition(Map<String, Object> addition) {
        this.addition = addition;
    }

    public byte[] getBytes(){
        ByteBuf buff = Unpooled.buffer(12);
        buff.writeInt(getProtocol());
        buff.writeLong(getData());
        return buff.array();
    }
}
