package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.BasicCallable;

public interface Unsafe {
    static void invoke(Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    static <T> T invoke(BasicCallable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
}
