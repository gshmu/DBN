package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.BasicCallable;
import com.dci.intellij.dbn.common.routine.BasicRunnable;

public interface Unsafe {
    static void invoke(BasicRunnable runnable) {
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
