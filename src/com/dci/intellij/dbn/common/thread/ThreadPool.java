package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public final class ThreadPool {
    private static final Map<String, AtomicInteger> THREAD_COUNTERS = new ConcurrentHashMap<>();

    private static final ExecutorService DATABASE_INTERFACE_EXECUTOR = newThreadPool("DBN - Database Interface Thread", true,  3, 100);
    private static final ExecutorService CANCELLABLE_EXECUTOR        = newThreadPool("DBN - Cancellable Calls Thread",  true,  3, 100);
    private static final ExecutorService BACKGROUND_EXECUTOR         = newThreadPool("DBN - Background Thread",         true,  3, 1000);
    private static final ExecutorService DEBUG_EXECUTOR              = newThreadPool("DBN - Database Debugger Thread",  true,  3, 20);
    private static final ExecutorService TIMEOUT_EXECUTOR            = newThreadPool("DBN - Timeout Execution Daemon",  false, 3, 100);
    private static final ExecutorService TIMEOUT_DAEMON_EXECUTOR     = newThreadPool("DBN - Timeout Execution Thread",  true,  3, 100);
    private static final ExecutorService CODE_COMPLETION_EXECUTOR    = newThreadPool("DBN - Code Completion Thread",    true,  3, 100);
    private static final ExecutorService OBJECT_LOOKUP_EXECUTOR      = newThreadPool("DBN - Object Lookup Thread",      true,  3, 100);

    private ThreadPool() {}

    @NotNull
    private static ThreadFactory createThreadFactory(String name, boolean daemon) {
        return runnable -> {
            AtomicInteger index = THREAD_COUNTERS.computeIfAbsent(name, s -> new AtomicInteger(0));
            String indexedName = name + " (" + index.incrementAndGet() + ")";
            log.info("Creating thread \"" + indexedName + "\"");
            Thread thread = new Thread(() -> {
                    try {
                        runnable.run();
                    } catch (ProcessCanceledException ignore) {
                    } catch (Throwable t) {
                        log.error(name + " - Execution failed: " + t.getMessage(), t);
                    }
                }, name);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(daemon);
            return thread;
        };
    }

    private static ExecutorService newThreadPool(String name, boolean daemon, int corePoolSize, int maximumPoolSize) {
        ThreadFactory threadFactory = createThreadFactory(name, daemon);
        SynchronousQueue<Runnable> queue = new SynchronousQueue<>();
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 60L, TimeUnit.SECONDS, queue, threadFactory);
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

    public static ExecutorService objectLookupExecutor() {
        return OBJECT_LOOKUP_EXECUTOR;
    }
}
