package com.dci.intellij.dbn.common.util;

import org.jetbrains.annotations.NotNull;

public class InitializedThreadLocal<T> extends ThreadLocal<T>{
    public InitializedThreadLocal(@NotNull T value) {
        super();
        set(value);
    }
}
