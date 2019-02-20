package com.dci.intellij.dbn.common.thread;

import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface OptionCallback<T>{
    void start(@NotNull T inputValue);
}
