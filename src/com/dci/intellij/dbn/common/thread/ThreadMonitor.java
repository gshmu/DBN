package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.BasicCallable;
import com.dci.intellij.dbn.common.routine.BasicRunnable;
import com.intellij.openapi.progress.ProcessCanceledException;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadMonitor {
    private static AtomicInteger backgroundProcessCounter = new AtomicInteger(0);
    private static ThreadLocal<ThreadInfo> THREAD_PROPERTIES = new ThreadLocal<>();

    public static ThreadInfo thread() {
        ThreadInfo threadInfo = THREAD_PROPERTIES.get();
        if (threadInfo == null) {
            threadInfo = new ThreadInfo();
            THREAD_PROPERTIES.set(threadInfo);
        }
        return threadInfo;
    }



    public static <E extends Throwable> void run(ThreadProperty threadProperty, BasicRunnable<E> runnable) throws E{
        ThreadInfo threadInfo = thread();
        try {
            threadInfo.set(threadProperty, true);
            runnable.run();
        } catch (ProcessCanceledException ignore){
        } finally {
            threadInfo.set(threadProperty, false);
        }
    }

    public static <T, E extends Throwable> T call(ThreadProperty threadProperty, T defaultValue, BasicCallable<T, E> runnable) throws E{
        ThreadInfo threadInfo = thread();
        try {
            threadInfo.set(threadProperty, true);
            return runnable.call();
        } catch (ProcessCanceledException ignore){
        } finally {
            threadInfo.set(threadProperty, false);
        }
        return defaultValue;
    }

    public static boolean isBackgroundProcess() {
        // default false
        ThreadInfo threadInfo = thread();
        return
            threadInfo.is(ThreadProperty.BACKGROUND_PROCESS) ||
            threadInfo.is(ThreadProperty.BACKGROUND_PROGRESS);
    }

    public static boolean isTimeoutProcess() {
        // default false
        return thread().is(ThreadProperty.TIMEOUT_PROCESS);
    }

    public static int getBackgroundProcessCount() {
        return backgroundProcessCounter.get();
    }
}
