package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.intellij.openapi.progress.ProcessCanceledException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public final class Safe {
    private Safe() {}

    public static <R, E extends Throwable> R call(R defaultValue, @NotNull ThrowableCallable<R, E> callable) throws E{
        try {
            return callable.call();
        } catch (ProcessCanceledException e){
            return defaultValue;
        }
    }

    public static void run(@NotNull Runnable runnable){
        try {
            runnable.run();
        } catch (ProcessCanceledException ignore){}
    }


    public static <R, S, E extends Throwable> R call(@Nullable S target, ParametricCallable<S, R, RuntimeException> callable, R defaultValue) throws E {
        if (target == null) {
            return defaultValue;
        } else {
            return callable.call(target);
        }
    }

    @Nullable
    public static <R, S, E extends Throwable> R call(@Nullable S target, @NotNull ParametricCallable<S, R, E> callable) throws E{
        if (target == null) {
            return null;
        } else {
            return callable.call(target);
        }
    }

    public static <S, E extends Throwable> void run(@Nullable S target, @NotNull ParametricRunnable<S, E> runnable) throws E{
        if (target != null) {
            runnable.run(target);
        }
    }

    public static <T> boolean equal(@Nullable T value1, @Nullable T value2) {
        if (value1 == null && value2 == null) {
            return true;
        }

        if (value1 == value2) {
            return true;
        }

        if (value1 != null) {
            return value1.equals(value2);
        }
        return false;
    }

    public static <T> boolean equal(@Nullable T value1, @Nullable T value2, Function<T, ?> valueProvider) {
        if (value1 == null && value2 == null) {
            return true;
        }

        if (value1 == value2) {
            return true;
        }

        if (value1 != null) {
            return equal(
                    valueProvider.apply(value1),
                    valueProvider.apply(value2));
        }
        return false;
    }

    public static <T extends Comparable<T>> int compare(@Nullable T value1, @Nullable T value2) {
        if (value1 == null && value2 == null) {
            return 0;
        }
        if (value1 == null) {
            return -1;
        }

        if (value2 == null) {
            return 1;
        }

        return value1.compareTo(value2);
    }

}
