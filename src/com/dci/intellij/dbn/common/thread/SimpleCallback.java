package com.dci.intellij.dbn.common.thread;

import org.jetbrains.annotations.Nullable;

public interface SimpleCallback<T>{
    void start(@Nullable T inputValue);
}
