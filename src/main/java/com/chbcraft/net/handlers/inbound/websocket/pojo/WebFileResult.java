package com.chbcraft.net.handlers.inbound.websocket.pojo;

public enum WebFileResult {
    TRANSPORT_CONTINUE(100),
    OUT_RELOAD_TIMES(503),
    RECONNECT(205),
    FILE_BROKEN(500),
    INIT_OK(101),
    TIME_OUT(203),
    CONNECT_ERROR(502),
    UPLOAD_COMPLETED(200),
    FORBIDDEN_WS(403);
    private int value;
    WebFileResult(int value){
        this.value = value;
    }

    public int value() {
        return value;
    }
}
