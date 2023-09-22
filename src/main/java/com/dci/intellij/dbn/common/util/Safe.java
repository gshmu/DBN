package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

@UtilityClass
public final class Safe {

    public static <R, S> R call(@Nullable S target, Function<S, R> supplier, R defaultValue) {
        if (target == null) {
            return defaultValue;
        } else {
            return supplier.apply(target);
        }
    }

    @Nullable
    public static <R, S, E extends Throwable> R call(@Nullable S target, @NotNull ParametricCallable<S, R, E> supplier) throws E{
        if (target == null) {
            return null;
        } else {
            return supplier.call(target);
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
