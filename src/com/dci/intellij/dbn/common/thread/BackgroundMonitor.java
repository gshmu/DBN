package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.ThreadLocalFlag;

public class BackgroundMonitor {
    private static ThreadLocalFlag backgroundProcess = new ThreadLocalFlag(false);
    private static ThreadLocalFlag timeoutProcess = new ThreadLocalFlag(false);

    public static boolean isBackgroundProcess() {
        // default false
        return backgroundProcess.get();
    }

    static void startBackgroundProcess() {
        backgroundProcess.set(true);
    }


    static void endBackgroundProcess() {
        backgroundProcess.set(false);
    }

    public static boolean isTimeoutProcess() {
        // default false
        return timeoutProcess.get();
    }

    static void startTimeoutProcess() {
        timeoutProcess.set(true);
    }


    static void endTimeoutProcess() {
        timeoutProcess.set(false);
    }

}
