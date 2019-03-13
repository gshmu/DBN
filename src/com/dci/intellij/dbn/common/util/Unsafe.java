package com.dci.intellij.dbn.common.util;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;

public interface Unsafe {
    static void run(ThrowableRunnable<Throwable> runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }

    static <T> T call(ThrowableCallable<T, Throwable> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
}
