package com.chbcraft.main;

import com.chbcraft.net.tranfer.*;

public class ServerApplication {
    public static void main(String[] args) {
        TransferFrame frame = new TransferFrame(TranProtocol.TASK_FAILED|TranProtocol.NEW_TASK,1135313);
        System.out.println(FrameUtil.checkCtrl(frame.getProtocol(), FrameCtrl.TASK_FAILED));
        System.out.println(FrameUtil.checkState(frame.getProtocol(), FrameState.NEW_TASK));
    }
}
