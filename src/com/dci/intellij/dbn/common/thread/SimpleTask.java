package com.dci.intellij.dbn.common.thread;

import com.intellij.openapi.progress.ProcessCanceledException;

public abstract class SimpleTask implements RunnableTask<Integer>{
    private int option;

    @Override
    public void setOption(Integer result) {
        this.option = result;
    }

    @Override
    public Integer getOption() {
        return option;
    }

    public void start() {
        run();
    }

    protected boolean canExecute() {
        return true;
    }

    public void run() {
        try {
            if (canExecute()) {
                execute();
            } else {
                cancel();
            }
        } catch (ProcessCanceledException e) {
            // do nothing
        }
    }

    protected void cancel() {

    }

    protected abstract void execute();
}
