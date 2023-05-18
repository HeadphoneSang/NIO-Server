package com.chbcraft.net.tranfer;

public class FrameUtil {
    private FrameUtil(){}
    /**
     * 判断控制帧
     * @param value 协议帧
     * @param ctrlPtl 控制帧类型
     * @return true or false
     */
    public static boolean checkCtrl(int value,FrameCtrl ctrlPtl){
        return (value & ~0xf)==ctrlPtl.value();
    }

    /**
     * 判断状态帧
     * @param value 协议帧
     * @param statePtl 状态帧类型
     * @return true or false
     */
    public static boolean checkState(int value,FrameState statePtl){
        return (value & 0xf)== statePtl.value();
    }
}
