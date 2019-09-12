package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;

public abstract class ContentDependency implements Disposable {
    private short changeSignature;

    @NotNull
    public abstract DynamicContent getSourceContent();

    public void reset() {
        changeSignature = getSourceContent().getChangeSignature();
    }

    public boolean isDirty() {
        return changeSignature != getSourceContent().getChangeSignature();
    }
}
