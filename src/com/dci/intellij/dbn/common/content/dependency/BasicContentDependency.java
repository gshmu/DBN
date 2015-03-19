package com.dci.intellij.dbn.common.content.dependency;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;

public class BasicContentDependency extends ContentDependency {
    private DynamicContent sourceContent;

    public BasicContentDependency(@NotNull DynamicContent sourceContent) {
        this.sourceContent = sourceContent;
        reset();
    }

    @NotNull
    @Override
    public DynamicContent getSourceContent() {
        return FailsafeUtil.get(sourceContent);
    }

    public void dispose() {
        sourceContent = null;
    }
}
