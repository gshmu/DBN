package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;

public final class Guarded {
    private Guarded() {}


    @SneakyThrows
    public static <R> R call(R defaultValue, @Nullable Callable<R> callable){
        try {
            return callable == null ? defaultValue : callable.call();
        } catch (ProcessCanceledException | IllegalStateException /*| UnsupportedOperationException*/ | AbstractMethodError ignore){
            return defaultValue;
        }
    }

    public static void run(@Nullable Runnable runnable){
        try {
            if (runnable != null) runnable.run();
        } catch (ProcessCanceledException | IllegalStateException /*| UnsupportedOperationException*/ | AbstractMethodError ignore){}
    }
}
