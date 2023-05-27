package com.chbcraft.internals.components.eventloop;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class FloatSchedule {
    private static ScheduledThreadPoolExecutor loop = new ScheduledThreadPoolExecutor(1);

    /**
     * 使用服务器预置的1核心线程池,目的控制服务器的线程数量
     * 创建并执行在给定延迟后启用的一次性操作。
     * @param runnable
     * @param delay
     * @param unit
     */
    public static ScheduledFuture<?> schedule(Runnable runnable, long delay, TimeUnit unit){
        return loop.schedule(runnable,delay,unit);
    }

    /**
     * 创建一个任务
     * @param command 任务
     */
    public static void execute(Runnable command){
        loop.execute(command);

    }

    /**
     * 利用服务器的事件循环执行一个定期循环任务
     * @param command 定时任务
     * @param initDelay 启示间隔时间
     * @param period 间隔时间
     * @param unit 时间单位
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initDelay, long period, TimeUnit unit){
        return loop.scheduleAtFixedRate(command, initDelay, period, unit);
    }

    /**
     * 删除任务
     * @param task 任务
     */
    public static void removeTask(Runnable task){
        loop.remove(task);
    }
}
