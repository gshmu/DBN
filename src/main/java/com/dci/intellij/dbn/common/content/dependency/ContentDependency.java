package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.dispose.UnlistedDisposable;
import org.jetbrains.annotations.NotNull;

public abstract class ContentDependency implements UnlistedDisposable {
    private byte signature;

    @NotNull
    public abstract DynamicContent getSourceContent();

    public void updateSignature() {
        signature = getSourceContent().getSignature();
    }

    public boolean isDirty() {
        return signature != getSourceContent().getSignature();
    }
}
