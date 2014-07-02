package com.dci.intellij.dbn.common.content;

public class DatabaseLoadMonitor {
    private static ThreadLocal<Boolean> loadingInBackground = new ThreadLocal<Boolean>();
    private static ThreadLocal<Boolean> ensureDataLoaded = new ThreadLocal<Boolean>();

    public static boolean isLoadingInBackground() {
        // default false
        Boolean isLoadingInBackground = loadingInBackground.get();
        return isLoadingInBackground != null && isLoadingInBackground;
    }

    public static void startBackgroundLoad() {
        loadingInBackground.set(true);
    }


    public static void endBackgroundLoad() {
        loadingInBackground.set(false);
    }

    public static boolean isEnsureDataLoaded() {
        // default true
        Boolean isEnsureDataLoaded = ensureDataLoaded.get();
        return isEnsureDataLoaded == null || isEnsureDataLoaded;
    }

    public static void setEnsureDataLoaded(boolean value) {
        ensureDataLoaded.set(value);
    }
}
