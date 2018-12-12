package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.latent.Loader;
import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SynchronizedTask<T> extends SimpleTask<T> {
    private static final SyncObjectProvider SYNC_OBJECT_PROVIDER = new SyncObjectProvider();

    public void start() {
        run();
    }

    @Override
    public final void run() {
        trace(this);
        try {
            if (canExecute()) {
                String syncKey = getSyncKey();
                try {
                    Object syncObject = SYNC_OBJECT_PROVIDER.get(syncKey);

                    if (syncObject == null) {
                        execute();
                    } else {
                        synchronized (syncObject) {
                            if (canExecute()) {
                                execute();
                            }
                        }
                    }
                } finally {
                    SYNC_OBJECT_PROVIDER.release(syncKey);
                }
            } else {
                cancel();
            }
        } catch (ProcessCanceledException ignore) {
        }
    }

    protected abstract String getSyncKey();

    public static <T> void invoke(Loader<String> syncKey, SimpleRunnable<T> runnable) {
        new SynchronizedTask<T>() {
            @Override
            protected void execute() {
                runnable.run(getData());
            }

            @Override
            protected String getSyncKey() {
                return syncKey.load();
            }
        }.start();
    }
}
