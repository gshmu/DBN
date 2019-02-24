package com.dci.intellij.dbn.common.thread;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadFactory {

    private static final ExecutorService DATABASE_INTERFACE_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Database Interface Thread", true));

    private static final ExecutorService CANCELLABLE_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Cancellable Call Thread", true));

    private static final ExecutorService BACKGROUND_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Background Thread", true));

    private static final ExecutorService DEBUG_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Database Debug Thread", true));

    private static final ExecutorService TIMEOUT_DAEMON_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Timed-out Execution Thread", true));

    private static final ExecutorService TIMEOUT_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Timed-out Execution Thread (non-daemon)", false));


    @NotNull
    private static java.util.concurrent.ThreadFactory createThreadFactory(final String name, final boolean daemon) {
        return runnable -> {
            Thread thread = new Thread(runnable, name);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(daemon);
            return thread;
        };
    }


    public static ExecutorService timeoutExecutor(boolean daemon) {
        return daemon ? TIMEOUT_DAEMON_EXECUTOR : TIMEOUT_EXECUTOR;
    }

    public static ExecutorService backgroundExecutor() {
        return BACKGROUND_EXECUTOR;
    }

    public static ExecutorService cancellableExecutor() {
        return CANCELLABLE_EXECUTOR;
    }

    public static ExecutorService debugExecutor() {
        return DEBUG_EXECUTOR;
    }

    public static ExecutorService databaseInterfaceExecutor() {
        return DATABASE_INTERFACE_EXECUTOR;
    }
}
