package com.dci.intellij.dbn.common.ui;

import com.dci.intellij.dbn.common.routine.Consumer;
import lombok.Getter;

@Getter
public abstract class PresentableFactory<T extends Presentable> {
    private final String actionName;

    public PresentableFactory(String actionName) {
        this.actionName = actionName;
    }

    public abstract void create(Consumer<T> consumer);
}
