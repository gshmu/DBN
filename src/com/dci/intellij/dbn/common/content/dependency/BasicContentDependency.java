package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.VoidDynamicContent;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import org.jetbrains.annotations.NotNull;

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

    @Override
    public void dispose() {
        sourceContent = VoidDynamicContent.INSTANCE;
    }
}
