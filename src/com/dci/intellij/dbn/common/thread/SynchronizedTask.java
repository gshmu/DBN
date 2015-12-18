package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SynchronizedTask extends SimpleTask {
    private static final SyncObjectProvider SYNC_OBJECT_PROVIDER = new SyncObjectProvider();

    public void start() {
        run();
    }

    @Override
    public final void run() {
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

}
