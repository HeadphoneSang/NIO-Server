package com.chbcraft.net.tranfer.frameHandlers;

public class CtrlFrameAdaptor extends FrameAdaptor{
    @Override
    public FrameAdaptor addFrameHandler(int protocol, FrameHandler handler) {
        return super.addFrameHandler(protocol, handler);
    }
}
