package com.chbcraft.net.tranfer;

public class TranProtocol {
    /**
     * 任务创建
     */
    public static final int TASK_CREATING = 0x01 << 4;
    /**
     * 任务进行
     */
    public static final int TASK_RUNNING = 0x02 << 4;
    /**
     * 任务完成
     */
    public static final int TASK_COMPLETED = 0X03 <<4;
    /**
     * 任务失败
     */
    public static final int TASK_FAILED = 0x04 << 4;
    /**
     * 新的传输任务
     */
    public static final int NEW_TASK = 0x01;
    /**
     * 继续的传输任务
     */
    public static final int CONTINUE_TASK = 0x02;
    /**
     * 可以继续
     */
    public static final int KEEP_CONTINUE = 0X03;
    /**
     * 取消任务
     */
    public static final int CANCEL_CONTINUE = 0X04;
    /**
     * 文件大小不一致
     */
    public static final int FILE_CHANGED = 0X05;
    /**
     * 文件已存在,无需上传
     */
    public static final int FILE_EXIST = 0x06;
    /**
     * 申请创建控制连接
     */
    public static final int CREATE_CTRL = 0x07;
    /**
     * 创建控制连接成功
     */
    public static final int CTRL_CREATED = 0x08;
    /**
     * 创建控制连接失败
     */
    public static final int CTRL_FAILED = 0x09;
    /**
     * PING
     */
    public static final int PING = 0xa;
    /**
     * PONG
     */
    public static final int PONG = 0xb;
}
