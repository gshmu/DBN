package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.ThreadLocalFlag;

import java.util.concurrent.atomic.AtomicInteger;

public class BackgroundMonitor {
    private static AtomicInteger backgroundProcessCounter = new AtomicInteger(0);
    private static ThreadLocalFlag backgroundProcess = new ThreadLocalFlag(false);
    private static ThreadLocalFlag timeoutProcess = new ThreadLocalFlag(false);


    public static boolean isBackgroundProcess() {
        // default false
        return backgroundProcess.get();
    }

    static void startBackgroundProcess() {
        backgroundProcess.set(true);
        backgroundProcessCounter.incrementAndGet();
    }


    static void endBackgroundProcess() {
        backgroundProcess.set(false);
        backgroundProcessCounter.decrementAndGet();
    }

    public static boolean isTimeoutProcess() {
        // default false
        return timeoutProcess.get();
    }

    public static void startTimeoutProcess() {
        timeoutProcess.set(true);
    }


    public static void endTimeoutProcess() {
        timeoutProcess.set(false);
    }

    public static int getBackgroundProcessCount() {
        return backgroundProcessCounter.get();
    }
}
