package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.util.ThreadLocalFlag;

public class BackgroundMonitor {
    private static ThreadLocalFlag backgroundProcess = new ThreadLocalFlag(false);

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
}
