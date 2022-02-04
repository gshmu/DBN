package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated ambiguous
 */
public final class Synchronized {
    private Synchronized() {}

    private static final SyncObjectProvider SYNC_OBJECT_PROVIDER = new SyncObjectProvider();

    public static <T, E extends Throwable> T call(@NotNull String syncKey, ThrowableCallable<T, E> callable) throws E{
        try {
            Object syncObject = SYNC_OBJECT_PROVIDER.get(syncKey);
            synchronized (syncObject) {
                return callable.call();
            }
        }
        //catch (ProcessCanceledException ignore) {} // TODO return default?
        finally {
            SYNC_OBJECT_PROVIDER.release(syncKey);
        }
    }


    @FunctionalInterface
    public interface Condition {
        boolean evaluate();
    }
}
