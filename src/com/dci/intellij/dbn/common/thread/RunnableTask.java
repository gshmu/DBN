package com.dci.intellij.dbn.common.thread;

public interface RunnableTask<T> extends Runnable{
    void start();
    void setData(T data);
    T getData();
}
