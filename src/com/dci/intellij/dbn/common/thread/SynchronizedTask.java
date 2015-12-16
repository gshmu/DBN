package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SynchronizedTask extends SimpleTask {
    private Object syncObject;
    private static final SyncObjectProvider SYNC_OBJECT_PROVIDER = new SyncObjectProvider();

    public SynchronizedTask(Object syncObject) {
        this.syncObject = syncObject;
    }

    public SynchronizedTask() {
    }

    public void start() {
        run();
    }

    @Override
    public final void run() {
        try {
            if (canExecute()) {
                String syncKey = getSyncKey();
                Object syncObject = this.syncObject;
                try {
                    if (syncObject == null) {
                        syncObject = SYNC_OBJECT_PROVIDER.get(syncKey);
                    }

                    if (syncObject == null) {
                        execute();
                    } else {
                        synchronized (syncObject) {
                            execute();
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

    protected String getSyncKey() {
        return null;
    }

}
