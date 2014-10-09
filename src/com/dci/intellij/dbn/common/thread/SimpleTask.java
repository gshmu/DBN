package com.dci.intellij.dbn.common.thread;

public abstract class SimpleTask extends RunnableTask{
    public final void start() {
        run();
    }

    public final void run() {
        execute();
    }

    public abstract void execute();
}
