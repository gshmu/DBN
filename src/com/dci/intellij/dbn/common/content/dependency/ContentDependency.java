package com.dci.intellij.dbn.common.content.dependency;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.intellij.openapi.Disposable;

public abstract class ContentDependency implements Disposable {
    private long changeTimestamp;

    @NotNull
    public abstract DynamicContent getSourceContent();

    public void reset() {
        changeTimestamp = getSourceContent().getChangeTimestamp();
    }

    public boolean isDirty() {
        return changeTimestamp != getSourceContent().getChangeTimestamp();
    }

    public void markSourcesDirty() {
        getSourceContent().markDirty();
    }
}
