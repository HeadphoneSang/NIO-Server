package com.chbcraft.io.net;

import com.chbcraft.internals.components.sysevent.Event;

import java.util.concurrent.BlockingQueue;

public class AcceptorLoop extends AbstractServiceLoop{
    public AcceptorLoop(BlockingQueue<? extends Event> queue0) {
        super(queue0);
    }

    @Override
    public void execute(Runnable command) {
        super.execute(command);
    }
}
