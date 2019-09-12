package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import org.jetbrains.annotations.Nullable;

public interface Safe {
    static <R, S, E extends Throwable> R call(S target, ParametricCallable<S, R, E> callable) throws E{
        if (target == null) {
            return null;
        } else {
            return callable.call(target);
        }
    }

    static <S, E extends Throwable> void run(S target, ParametricRunnable<S, E> runnable) throws E{
        if (target != null) {
            runnable.run(target);
        }
    }

    static <T> boolean equal(@Nullable T value1, @Nullable T value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 != null) {
            return value1.equals(value2);
        }
        return false;
    }

    static <T extends Comparable<T>> int compare(@Nullable T value1, @Nullable T value2) {
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
