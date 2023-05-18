package com.chbcraft.net.tranfer;

public enum FrameState {
    /**
     * 新创建的任务
     */
    NEW_TASK(0x01),
    /**
     * 续传的任务
     */
    CONTINUE_TASK(0x02),
    /**
     * 可以续传
     */
    KEEP_CONTINUE(0X03),
    /**
     * 不可以续传
     */
    CANCEL_CONTINUE(0X04);
    private final int value;
    FrameState(int value){
        this.value = value;
    }
    public int value(){
        return this.value;
    }
}
