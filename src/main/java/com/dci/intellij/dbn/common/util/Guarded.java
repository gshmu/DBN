package com.dci.intellij.dbn.common.util;

import com.intellij.openapi.progress.ProcessCanceledException;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Callable;

public final class Guarded {
    private Guarded() {}


    @SneakyThrows
    public static <R> R call(R defaultValue, @NotNull Callable<R> callable){
        try {
            return callable.call();
        } catch (ProcessCanceledException | IllegalStateException | UnsupportedOperationException | AbstractMethodError ignore){
            return defaultValue;
        }
    }

    public static void run(@NotNull Runnable runnable){
        try {
            runnable.run();
        } catch (ProcessCanceledException | IllegalStateException | UnsupportedOperationException | AbstractMethodError ignore){}
    }
}
