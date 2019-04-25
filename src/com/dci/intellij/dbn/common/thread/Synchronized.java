package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.NotNull;

public interface Synchronized {
    SyncObjectProvider SYNC_OBJECT_PROVIDER = new SyncObjectProvider();

    static void run(Object syncObject, Condition condition, Runnable runnable) {
        if(condition.evaluate()) {
            synchronized (syncObject) {
                if(condition.evaluate()) {
                    runnable.run();
                }
            }
        }
    }

    static void sync(@NotNull String syncKey, Runnable runnable) {
        try {
            Object syncObject = SYNC_OBJECT_PROVIDER.get(syncKey);
            synchronized (syncObject) {
                runnable.run();
            }
        }
        catch (ProcessCanceledException ignore) {}
        finally {
            SYNC_OBJECT_PROVIDER.release(syncKey);
        }
    }

    @FunctionalInterface
    interface Condition {
        boolean evaluate();
    }
}
