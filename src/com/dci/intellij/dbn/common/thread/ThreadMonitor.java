package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.BasicCallable;

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



    public static void run(ThreadProperty threadProperty, Runnable runnable){
        ThreadInfo threadInfo = thread();
        try {
            threadInfo.set(threadProperty, true);
            Failsafe.lenient(runnable);
        } finally {
            threadInfo.set(threadProperty, false);
        }
    }

    public static <T> T call(ThreadProperty threadProperty, T defaultValue, BasicCallable<T> callable) {
        ThreadInfo threadInfo = thread();
        try {
            threadInfo.set(threadProperty, true);
            return Failsafe.lenient(defaultValue, callable);
        } finally {
            threadInfo.set(threadProperty, false);
        }
    }

    public static boolean isBackgroundProcess() {
        // default false
        ThreadInfo threadInfo = thread();
        return
            threadInfo.is(ThreadProperty.BACKGROUND_THREAD) ||
            threadInfo.is(ThreadProperty.BACKGROUND_TASK) ||
            threadInfo.is(ThreadProperty.MODAL_TASK);
    }

    public static boolean isTimeoutProcess() {
        // default false
        return thread().is(ThreadProperty.TIMEOUT_PROCESS);
    }

    public static int getBackgroundProcessCount() {
        return backgroundProcessCounter.get();
    }
}
