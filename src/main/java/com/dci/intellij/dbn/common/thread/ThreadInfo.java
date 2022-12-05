package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.common.property.PropertyHolderBase;
import com.intellij.openapi.project.Project;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

@Getter
@Setter
public class ThreadInfo extends PropertyHolderBase.IntStore<ThreadProperty> {
    private volatile transient Project project;

    @Override
    protected ThreadProperty[] properties() {
        return ThreadProperty.VALUES;
    }

    @Override
    public void merge(@Nullable PropertyHolder<ThreadProperty> source) {
        if (source == null) return;

        for (ThreadProperty property : properties()) {
            if (property.propagatable() && source.is(property)) {
                set(property, true);
            }
        }
    }

    @Override
    public void unmerge(@Nullable PropertyHolder<ThreadProperty> source) {
        if (source == null) return;

        for (ThreadProperty property : properties()) {
            if (property.propagatable() && source.is(property)) {
                set(property, false);
            }
        }
    }
}
