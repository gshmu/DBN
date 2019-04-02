package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;
import org.jetbrains.annotations.Nullable;

public class ThreadInfo extends PropertyHolderImpl<ThreadProperty> {
    @Override
    protected ThreadProperty[] properties() {
        return ThreadProperty.values();
    }

    @Override
    public ThreadInfo clone() {
        return (ThreadInfo) super.clone();
    }

    @Override
    public void merge(@Nullable PropertyHolder<ThreadProperty> source) {
        if (source != null) {
            for (ThreadProperty property : properties()) {
                if (property.propagatable && source.is(property)) {
                    set(property, true);
                }
            }
        }
    }

    @Override
    public void unmerge(@Nullable PropertyHolder<ThreadProperty> source) {
        if (source != null) {
            for (ThreadProperty property : properties()) {
                if (property.propagatable() && source.is(property)) {
                    set(property, false);
                }
            }
        }
    }
}
