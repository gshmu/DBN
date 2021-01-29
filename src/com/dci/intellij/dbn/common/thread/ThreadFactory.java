package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadFactory {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    private static final Map<String, AtomicInteger> THREAD_COUNTERS = new ConcurrentHashMap<>();

    private static final ExecutorService DATABASE_INTERFACE_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Database Interface Thread", true));

    private static final ExecutorService CANCELLABLE_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Cancellable Calls Thread", true));

    private static final ExecutorService BACKGROUND_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Background Thread", true));

    private static final ExecutorService DEBUG_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Database Debugger Thread", true));

    private static final ExecutorService TIMEOUT_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Timeout Execution Thread", false));

    private static final ExecutorService TIMEOUT_DAEMON_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Timeout Execution Thread (non-daemon)", true));

    private static final ExecutorService CODE_COMPLETION_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Code Completion Thread", true));

    private static final ExecutorService OBJECT_LOOKUP_EXECUTOR = Executors.newCachedThreadPool(createThreadFactory("DBN - Object Lookup Thread", true));


    @NotNull
    private static java.util.concurrent.ThreadFactory createThreadFactory(String name, boolean daemon) {
        return runnable -> {
            AtomicInteger index = THREAD_COUNTERS.computeIfAbsent(name, s -> new AtomicInteger(0));
            String indexedName = name + " (" + index.incrementAndGet() + ")";
            LOGGER.info("Creating thread \"" + indexedName + "\"");
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

    public static ExecutorService getCodeCompletionExecutor() {
        return CODE_COMPLETION_EXECUTOR;
    }

    public static ExecutorService getObjectLookupExecutor() {
        return OBJECT_LOOKUP_EXECUTOR;
    }
}
