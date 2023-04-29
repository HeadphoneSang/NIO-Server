package com.chbcraft.net.handlers.inbound.websocket.pojo;

public enum WebFileResult {
    TRANSPORT_CONTINUE(100),
    INIT_OK(101),
    FORBIDDEN_WS(403);
    private int value;
    WebFileResult(int value){
        this.value = value;
    }

    public int value() {
        return value;
    }
}
