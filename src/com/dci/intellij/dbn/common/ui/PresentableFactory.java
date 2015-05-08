package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.thread.SimpleCallback;

public abstract class PresentableFactory<T extends Presentable> {
    private String actionName;

    public PresentableFactory(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }

    public abstract void create(SimpleCallback<T> callback);
}
