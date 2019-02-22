package com.dci.intellij.dbn.common.message;

import com.dci.intellij.dbn.common.routine.ParametricRunnable;
import com.dci.intellij.dbn.common.thread.SimpleTask;

public abstract class MessageCallback extends SimpleTask<Integer> {
    private Integer executeOption;

    private MessageCallback(Integer executeOption) {
        this.executeOption = executeOption;
    }

    @Override
    protected boolean canExecute() {
        return executeOption == null || executeOption.equals(getData());
    }

    public static MessageCallback create(Integer executeOption, ParametricRunnable.Unsafe<Integer> runnable) {
        return new MessageCallback(executeOption) {
            @Override
            protected void execute() {
                runnable.run(getData());
            }
        };
    }
}
