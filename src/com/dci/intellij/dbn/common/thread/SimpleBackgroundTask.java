package com.dci.intellij.dbn.common.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.jetbrains.annotations.NotNull;

public abstract class SimpleBackgroundTask extends SynchronizedTask{
    public static final ExecutorService POOL = Executors.newCachedThreadPool(new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable, "DBN - Background Thread");
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            return thread;
        }
    });

    String name;

    public SimpleBackgroundTask(String name) {
        super(null);
        this.name = name;
    }

    public SimpleBackgroundTask(Object syncObject) {
        super(syncObject);
    }

    public final void start() {
        POOL.submit(this);
    }
}
