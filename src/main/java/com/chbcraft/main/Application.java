package com.chbcraft.main;

import com.chbcraft.net.tranfer.TranProtocol;
import com.chbcraft.net.tranfer.TransferFrame;

public class Application {
    public static void main(String[] args) {
        byte[] bytes = new TransferFrame(TranProtocol.TASK_COMPLETED|TranProtocol.FILE_EXIST,1021615634).getBytes();
    }
}
