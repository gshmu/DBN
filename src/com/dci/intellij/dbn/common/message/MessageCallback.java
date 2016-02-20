package com.dci.intellij.dbn.common.message;

import com.dci.intellij.dbn.common.thread.SimpleTask;

public abstract class MessageCallback extends SimpleTask<Integer> {
    public MessageCallback() {
        setOption(0);
    }

    public MessageCallback(Integer executeOption) {
        super(executeOption);
    }
}
