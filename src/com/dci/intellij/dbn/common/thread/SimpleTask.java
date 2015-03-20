package com.dci.intellij.dbn.common.thread;

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
        if (canExecute()) {
            execute();
        } else {
            cancel();
        }
    }

    protected void cancel() {

    }

    protected abstract void execute();
}
