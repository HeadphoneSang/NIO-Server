package com.chbcraft.io.net;

import com.chbcraft.internals.components.sysevent.Event;

import java.util.List;
import java.util.concurrent.*;

public abstract class AbstractServiceLoop extends AbstractExecutorService {
    private final BlockingQueue<? extends Event> eventsQueue;

    /**
     * Initiates an orderly shutdown in which previously submitted
     * tasks are executed, but no new tasks will be accepted.
     * Invocation has no additional effect if already shut down.
     *
     * <p>This method does not wait for previously submitted tasks to
     * complete execution.  Use {@link #awaitTermination awaitTermination}
     * to do that.
     *
     * @throws SecurityException if a security manager exists and
     *                           shutting down this ExecutorService may manipulate
     *                           threads that the caller is not permitted to modify
     *                           because it does not hold {@link
     *                           RuntimePermission}{@code ("modifyThread")},
     *                           or the security manager's {@code checkAccess} method
     *                           denies access.
     */
    @Override
    public void shutdown() {

    }

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks
     * that were awaiting execution.
     *
     * <p>This method does not wait for actively executing tasks to
     * terminate.  Use {@link #awaitTermination awaitTermination} to
     * do that.
     *
     * <p>There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks.  For example, typical
     * implementations will cancel via {@link Thread#interrupt}, so any
     * task that fails to respond to interrupts may never terminate.
     *
     * @return list of tasks that never commenced execution
     * @throws SecurityException if a security manager exists and
     *                           shutting down this ExecutorService may manipulate
     *                           threads that the caller is not permitted to modify
     *                           because it does not hold {@link
     *                           RuntimePermission}{@code ("modifyThread")},
     *                           or the security manager's {@code checkAccess} method
     *                           denies access.
     */
    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    /**
     * Returns {@code true} if this executor has been shut down.
     *
     * @return {@code true} if this executor has been shut down
     */
    @Override
    public boolean isShutdown() {
        return false;
    }

    /**
     * Returns {@code true} if all tasks have completed following shut down.
     * Note that {@code isTerminated} is never {@code true} unless
     * either {@code shutdown} or {@code shutdownNow} was called first.
     *
     * @return {@code true} if all tasks have completed following shut down
     */
    @Override
    public boolean isTerminated() {
        return false;
    }

    /**
     * Blocks until all tasks have completed execution after a shutdown
     * request, or the timeout occurs, or the current thread is
     * interrupted, whichever happens first.
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the timeout argument
     * @return {@code true} if this executor terminated and
     * {@code false} if the timeout elapsed before termination
     * @throws InterruptedException if interrupted while waiting
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    public AbstractServiceLoop(BlockingQueue<? extends Event> queue0) {
        super();
        this.eventsQueue = queue0;
    }

    /**
     * Executes the given command at some time in the future.  The command
     * may execute in a new thread, in a pooled thread, or in the calling
     * thread, at the discretion of the {@code Executor} implementation.
     *
     * @param command the runnable task
     * @throws RejectedExecutionException if this task cannot be
     *                                    accepted for execution
     * @throws NullPointerException       if command is null
     */
    @Override
    public void execute(Runnable command) {

    }
}
