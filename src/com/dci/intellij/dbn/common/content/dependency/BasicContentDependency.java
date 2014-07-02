package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.VoidDynamicContent;
import org.jetbrains.annotations.NotNull;

public class BasicContentDependency extends ContentDependency {
    private DynamicContent sourceContent;

    public BasicContentDependency(DynamicContent sourceContent) {
        this.sourceContent = sourceContent;
        reset();
    }

    @NotNull
    @Override
    public DynamicContent getSourceContent() {
        return sourceContent;
    }

    public void dispose() {
        sourceContent = VoidDynamicContent.INSTANCE;
    }
}
