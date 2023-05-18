package com.chbcraft.net.tranfer;

public enum FrameCtrl {
    /**
     * 任务创建协议帧
     */
    TASK_CREATING(0x01 << 4),
    /**
     * 任务进行时协议帧
     */
    TASK_RUNNING(0x02 << 4),
    /**
     * 任务完成协议帧
     */
    TASK_COMPLETED(0X03 <<4),
    /**
     * 任务失败协议帧
     */
    TASK_FAILED(0x04 << 4);
    private final int value;
    FrameCtrl(int value){
        this.value = value;
    }
    public int value(){
        return this.value;
    }
}
