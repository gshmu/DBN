package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.latent.Loader;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;

public abstract class SynchronizedTask<T> extends SimpleTask<T> {
    private static final SyncObjectProvider SYNC_OBJECT_PROVIDER = new SyncObjectProvider();

    @Override
    public void start() {
        run();
    }

    @Override
    public final void run() {
        trace(this);
        Failsafe.lenient(() -> {
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
        });
    }

    protected abstract String getSyncKey();

    public static <T> void invoke(Loader<String> syncKey, ParametricRunnable.Unsafe<T> runnable) {
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
