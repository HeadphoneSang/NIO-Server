package com.chbcraft.net;

import com.chbcraft.internals.components.MessageBox;

import java.util.concurrent.Future;

public class HttpProcessor {

    private HttpAcceptor acceptor;
    public HttpProcessor(HttpAcceptor acceptor){
        this.acceptor = acceptor;

    }
    public void execute(){
        this.acceptor.accepting();
    }

    public Future<?> shutdown(){
        MessageBox.getLogger().log("shutdown server listening");
        return this.acceptor.shutdown();
    }
}
