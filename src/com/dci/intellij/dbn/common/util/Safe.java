package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

public final class Safe {
    private Safe() {}

    @SneakyThrows
    public static <R> R call(R defaultValue, @NotNull Callable<R> callable){
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


    public static <R, S> R call(@Nullable S target, Function<S, R> supplier, R defaultValue) {
        if (target == null) {
            return defaultValue;
        } else {
            return supplier.apply(target);
        }
    }

    @Nullable
    public static <R, S> R call(@Nullable S target, @NotNull Function<S, R> supplier){
        if (target == null) {
            return null;
        } else {
            return supplier.apply(target);
        }
    }

    public static <S> void run(@Nullable S target, @NotNull Consumer<S> runnable){
        if (target != null) {
            runnable.accept(target);
        }
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
