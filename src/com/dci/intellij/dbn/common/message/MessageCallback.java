package com.dci.intellij.dbn.common.message;

import com.dci.intellij.dbn.common.thread.SimpleTask;

public abstract class MessageCallback extends SimpleTask<Integer> {
    public MessageCallback() {
    }

    public MessageCallback(Integer executeOption) {
        super(executeOption);
    }
}
