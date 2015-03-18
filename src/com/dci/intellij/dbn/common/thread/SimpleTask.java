package com.dci.intellij.dbn.common.thread;

public abstract class SimpleTask implements RunnableTask<Integer>{
    private int result;

    @Override
    public void setResult(Integer result) {
        this.result = result;
    }

    @Override
    public Integer getResult() {
        return result;
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
