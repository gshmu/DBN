package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadMonitor {
    private static ThreadLocal<ThreadInfo> THREAD_PROPERTIES = new ThreadLocal<>();
    private static Map<ThreadProperty, AtomicInteger> PROCESS_COUNTERS = ContainerUtil.newConcurrentMap();

    public static ThreadInfo current() {
        ThreadInfo threadInfo = THREAD_PROPERTIES.get();
        if (threadInfo == null) {
            threadInfo = new ThreadInfo();
            THREAD_PROPERTIES.set(threadInfo);
        }
        return threadInfo;
    }



    public static <E extends Throwable> void run(
            @Nullable ThreadInfo invoker,
            @NotNull ThreadProperty threadProperty,
            ThrowableRunnable<E> runnable) throws E {

        ThreadInfo threadInfo = current();
        AtomicInteger processCounter = getProcessCounter(threadProperty);
        try {
            processCounter.incrementAndGet();
            threadInfo.set(threadProperty, true);
            threadInfo.merge(invoker);
            Failsafe.lenient(runnable);
        } finally {
            threadInfo.set(threadProperty, false);
            processCounter.decrementAndGet();
            threadInfo.unmerge(invoker);
        }
    }

    public static <T, E extends Throwable> T call(
            @Nullable ThreadInfo invoker,
            @NotNull ThreadProperty threadProperty,
            T defaultValue,
            ThrowableCallable<T, E> callable) throws E{

        ThreadInfo threadInfo = current();
        AtomicInteger processCounter = getProcessCounter(threadProperty);
        try {
            processCounter.incrementAndGet();
            threadInfo.set(threadProperty, true);
            threadInfo.merge(invoker);
            return Failsafe.lenient(defaultValue, callable);
        } finally {
            threadInfo.set(threadProperty, false);
            threadInfo.unmerge(invoker);
            processCounter.decrementAndGet();
        }
    }

    public static boolean isBackgroundProcess() {
        // default false
        ThreadInfo threadInfo = current();
        return
            threadInfo.is(ThreadProperty.BACKGROUND_THREAD) ||
            threadInfo.is(ThreadProperty.BACKGROUND_TASK) ||
            threadInfo.is(ThreadProperty.MODAL_TASK);
    }

    public static boolean isTimeoutProcess() {
        // default false
        return current().is(ThreadProperty.TIMEOUT_PROCESS);
    }

    public static int getProcessCount(ThreadProperty property) {
        return getProcessCounter(property).intValue();
    }

    private static AtomicInteger getProcessCounter(ThreadProperty property) {
        return PROCESS_COUNTERS.computeIfAbsent(property, property1 -> new AtomicInteger(0));
    }
}
