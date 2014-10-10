package com.dci.intellij.dbn.common.thread;

public abstract class SimpleTask implements RunnableTask{
    private int option;

    public int getOption() {
        return option;
    }

    public void setOption(int option) {
        this.option = option;
    }

    public void start() {
        run();
    }

    public void run() {
        execute();
    }

    protected abstract void execute();
}
