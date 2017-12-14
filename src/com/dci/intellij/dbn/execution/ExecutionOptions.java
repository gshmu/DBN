package com.dci.intellij.dbn.execution;

public class ExecutionOptions implements Cloneable{
    private boolean enableLogging = false;
    private boolean commitAfterExecution = false;

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public boolean isCommitAfterExecution() {
        return commitAfterExecution;
    }

    public void setCommitAfterExecution(boolean commitAfterExecution) {
        this.commitAfterExecution = commitAfterExecution;
    }

    public ExecutionOptions clone() {
        ExecutionOptions clone = new ExecutionOptions();
        clone.enableLogging = enableLogging;
        clone.commitAfterExecution = commitAfterExecution;
        return clone;
    }
}
