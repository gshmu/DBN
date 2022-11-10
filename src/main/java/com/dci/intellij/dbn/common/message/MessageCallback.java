package com.dci.intellij.dbn.common.message;

import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import org.jetbrains.annotations.Nullable;

public interface MessageCallback extends ParametricRunnable<Integer, RuntimeException> {

    static void when(boolean condition, @Nullable Runnable runnable) {
        if (condition && runnable != null) {
            runnable.run();
        }
    }
}
