package com.dci.intellij.dbn.common.thread;

public abstract class RunnableTask implements Runnable{
    private int option;

    public abstract void start();

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }
}
