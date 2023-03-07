package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;
import static com.dci.intellij.dbn.common.thread.ThreadProperty.*;

public class ThreadMonitor {
    private static final ThreadLocal<ThreadInfo> THREAD_INFO = new ThreadLocal<>();
    private static final Map<ThreadProperty, AtomicInteger> PROCESS_COUNTERS = new ConcurrentHashMap<>();

    public static ThreadInfo current() {
        ThreadInfo threadInfo = THREAD_INFO.get();
        if (threadInfo == null) {
            threadInfo = new ThreadInfo();
            THREAD_INFO.set(threadInfo);
        }
        return threadInfo;
    }

    @Nullable
    public static Project getProject() {
        return current().getProject();
    }

    public static <E extends Throwable> void surround(
            @Nullable Project project,
            @NotNull ThreadProperty property,
            ThrowableRunnable<E> runnable) throws E {
        ThreadInfo threadInfo = current();
        Project originalProject = threadInfo.getProject();

        try {
            threadInfo.set(property, true);
            threadInfo.setProject(project);
            guarded(runnable);
        } finally {
            threadInfo.set(property, false);
            threadInfo.setProject(originalProject);
        }
    }

    public static <T, E extends Throwable> T surround(
            @Nullable Project project,
            @NotNull ThreadProperty property,
            ThrowableCallable<T, E> callable) throws E {
        ThreadInfo threadInfo = current();
        Project originalProject = threadInfo.getProject();

        try {
            threadInfo.set(property, true);
            threadInfo.setProject(project);
            return guarded(null, callable, c -> c.call());
        } finally {
            threadInfo.set(property, false);
            threadInfo.setProject(originalProject);
        }
    }


    public static <E extends Throwable> void surround(
            @Nullable Project project,
            @Nullable ThreadInfo invoker,
            @NotNull ThreadProperty property,
            ThrowableRunnable<E> runnable) throws E {

        ThreadInfo threadInfo = current();
        boolean originalProperty = threadInfo.is(property);
        Project originalProject = threadInfo.getProject();

        AtomicInteger processCounter = getProcessCounter(property);
        try {
            processCounter.incrementAndGet();
            threadInfo.set(property, true);
            threadInfo.setProject(project);
            threadInfo.merge(invoker);
            guarded(runnable);
        } finally {
            threadInfo.set(property, originalProperty);
            threadInfo.setProject(originalProject);
            threadInfo.unmerge(invoker);
            processCounter.decrementAndGet();
        }
    }

    public static <T, E extends Throwable> T surround(
            @Nullable Project project,
            @Nullable ThreadInfo invoker,
            @NotNull ThreadProperty property,
            T defaultValue,
            ThrowableCallable<T, E> callable) throws E {

        ThreadInfo threadInfo = current();
        boolean originalProperty = threadInfo.is(property);
        Project originalProject = threadInfo.getProject();

        AtomicInteger processCounter = getProcessCounter(property);
        try {
            processCounter.incrementAndGet();
            threadInfo.set(property, true);
            threadInfo.setProject(project);
            threadInfo.merge(invoker);
            return guarded(defaultValue, callable, c -> c.call());
        } finally {
            threadInfo.set(property, originalProperty);
            threadInfo.setProject(originalProject);
            threadInfo.unmerge(invoker);
            processCounter.decrementAndGet();
        }
    }

    public static boolean isTimeoutProcess() {
        return current().is(TIMEOUT);
    }

    public static boolean isProgressProcess() {
        return current().is(PROGRESS);
    }

    public static boolean isBackgroundProcess() {
        return current().is(BACKGROUND);
    }

    public static boolean isModalProcess() {
        return current().is(MODAL);
    }

    public static boolean isDisposerProcess() {
        return current().is(DISPOSER);
    }

    public static boolean isDispatchThread() {
        return ApplicationManager.getApplication().isDispatchThread();
    }

    public static int getProcessCount(ThreadProperty property) {
        return getProcessCounter(property).intValue();
    }

    private static AtomicInteger getProcessCounter(ThreadProperty property) {
        return PROCESS_COUNTERS.computeIfAbsent(property, p -> new AtomicInteger(0));
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
