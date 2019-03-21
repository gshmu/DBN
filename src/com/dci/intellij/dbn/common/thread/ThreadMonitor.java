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
        boolean originalProperty = threadInfo.is(threadProperty);
        AtomicInteger processCounter = getProcessCounter(threadProperty);
        try {
            processCounter.incrementAndGet();
            threadInfo.set(threadProperty, true);
            threadInfo.merge(invoker);
            Failsafe.guarded(runnable);
        } finally {
            threadInfo.set(threadProperty, originalProperty);
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
        boolean originalProperty = threadInfo.is(threadProperty);
        AtomicInteger processCounter = getProcessCounter(threadProperty);
        try {
            processCounter.incrementAndGet();
            threadInfo.set(threadProperty, true);
            threadInfo.merge(invoker);
            return Failsafe.guarded(defaultValue, callable);
        } finally {
            threadInfo.set(threadProperty, originalProperty);
            threadInfo.unmerge(invoker);
            processCounter.decrementAndGet();
        }
    }

    public static boolean is(ThreadProperty ... properties) {
        ThreadInfo threadInfo = current();
        for (ThreadProperty property : properties) {
            if (threadInfo.is(property)) {
                return true;
            }
        }
        return false;
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

    public static boolean isFailsafe() {
        return current().is(ThreadProperty.FAILSAFE);
    }

    public static int getProcessCount(ThreadProperty property) {
        return getProcessCounter(property).intValue();
    }

    private static AtomicInteger getProcessCounter(ThreadProperty property) {
        return PROCESS_COUNTERS.computeIfAbsent(property, property1 -> new AtomicInteger(0));
    }

    public static <E extends Throwable> void wrap(@NotNull ThreadProperty threadProperty, ThrowableRunnable<E> runnable) throws E {
        ThreadInfo threadInfo = ThreadMonitor.current();
        boolean original = threadInfo.is(threadProperty);
        try {
            threadInfo.set(threadProperty, true);
            runnable.run();
        }
        finally {
            threadInfo.set(threadProperty, original);
        }
    }

    public static <R, E extends Throwable> R wrap(@NotNull ThreadProperty threadProperty, ThrowableCallable<R, E> callable) throws E {
        ThreadInfo threadInfo = ThreadMonitor.current();
        boolean original = threadInfo.is(threadProperty);
        try {
            threadInfo.set(threadProperty, true);
            return callable.call();
        }
        finally {
            threadInfo.set(threadProperty, original);
        }
    }
}
