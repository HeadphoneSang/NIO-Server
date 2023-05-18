package com.chbcraft.net.tranfer;

public class TransferFrame {
    // 帧数据
    private final int protocol;
    // 数据长度|数据坐标
    private final long data;
    // 附加数据
    private Object addition;

    public TransferFrame(int protocol,long data){
        this.protocol = protocol;
        this.data = data;
    }

    public int getProtocol() {
        return protocol;
    }

    public long getData() {
        return data;
    }

    public Object getAddition() {
        return addition;
    }

    public void setAddition(Object addition) {
        this.addition = addition;
    }
}
