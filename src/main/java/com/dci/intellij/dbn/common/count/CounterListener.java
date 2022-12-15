package com.dci.intellij.dbn.common.count;

import java.util.EventListener;

public interface CounterListener extends EventListener {
    void when(int value);
}
