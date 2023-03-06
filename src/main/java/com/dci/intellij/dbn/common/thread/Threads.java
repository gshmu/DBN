package com.dci.intellij.dbn.common.thread;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;

@Slf4j
public final class Threads {
    private static final Map<String, AtomicInteger> THREAD_COUNTERS = new ConcurrentHashMap<>();

    private static final ExecutorService DATABASE_INTERFACE_EXECUTOR = newThreadPool("DBN - Database Interface Thread", true,  5, 100);
    private static final ExecutorService CANCELLABLE_EXECUTOR        = newThreadPool("DBN - Cancellable Calls Thread",  true,  5, 100);
    private static final ExecutorService BACKGROUND_EXECUTOR         = newThreadPool("DBN - Background Thread",         true,  5, 200);
    private static final ExecutorService DEBUG_EXECUTOR              = newThreadPool("DBN - Database Debugger Thread",  true,  3, 20);
    private static final ExecutorService TIMEOUT_EXECUTOR            = newThreadPool("DBN - Timeout Execution Thread",  false, 5, 100);
    private static final ExecutorService TIMEOUT_DAEMON_EXECUTOR     = newThreadPool("DBN - Timeout Execution Daemon",  true,  5, 200);
    private static final ExecutorService CODE_COMPLETION_EXECUTOR    = newThreadPool("DBN - Code Completion Thread",    true,  5, 100);
    private static final ExecutorService OBJECT_LOOKUP_EXECUTOR      = newThreadPool("DBN - Object Lookup Thread",      true,  5, 100);

    private Threads() {}

    @NotNull
    public static ThreadFactory createThreadFactory(String name, boolean daemon) {
        return runnable -> {
            AtomicInteger index = THREAD_COUNTERS.computeIfAbsent(name, n -> new AtomicInteger(0));
            String indexedName = name + " (" + index.incrementAndGet() + ")";
            log.info("Creating thread \"" + indexedName + "\"");
            Thread thread = new Thread(() -> {
                    try {
                        guarded(runnable, r -> r.run());
                    } catch (Throwable t) {
                        log.error(name + " - Execution failed: " + t.getMessage(), t);
                    }
                }, name);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(daemon);
            return thread;
        };
    }

    public static ExecutorService newCachedThreadPool(String name, boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(name, daemon);
        return Executors.newCachedThreadPool(threadFactory);
    }

    private static ExecutorService newThreadPool(String name, boolean daemon, int corePoolSize, int maximumPoolSize) {
        ThreadFactory threadFactory = createThreadFactory(name, daemon);
        SynchronousQueue<Runnable> queue = new SynchronousQueue<>();
        //BlockingQueue<Runnable> queue = new LinkedBlockingDeque<>();
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, 1L, TimeUnit.MINUTES, queue, threadFactory);
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

    static void delay(Object sync) {
        LockSupport.parkNanos(sync, 1_000_000);
    }
}
