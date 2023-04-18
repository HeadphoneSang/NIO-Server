package com.chbcraft.net;



import java.util.concurrent.Future;

public abstract class HttpAcceptor {
    private int port;
    public HttpAcceptor(int port){
        this.port = port;
    }
    abstract void accepting();
    abstract Future<?> shutdown();
}
