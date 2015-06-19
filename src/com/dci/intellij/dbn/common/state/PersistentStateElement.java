package com.dci.intellij.dbn.common.state;


public interface PersistentStateElement<T>{
    void readState(T element);
    void writeState(T element);
}
