package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.BasicCallable;
import com.dci.intellij.dbn.common.routine.BasicRunnable;

public interface Unsafe {
    static void invoke(BasicRunnable<Exception> runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    static <T> T invoke(BasicCallable<T, Exception> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
}
