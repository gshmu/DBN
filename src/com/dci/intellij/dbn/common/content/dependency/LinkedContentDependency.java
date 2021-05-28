package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.content.VoidDynamicContent;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import org.jetbrains.annotations.NotNull;

public class LinkedContentDependency extends ContentDependency {
    private GenericDatabaseElement sourceContentOwner;
    private final DynamicContentType sourceContentType;

    public LinkedContentDependency(@NotNull GenericDatabaseElement sourceContentOwner, @NotNull DynamicContentType sourceContentType) {
        this.sourceContentOwner = sourceContentOwner;
        this.sourceContentType = sourceContentType;
        reset();
    }

    @Override
    @NotNull
    public DynamicContent getSourceContent() {
        if (sourceContentOwner != null) {
            DynamicContent sourceContent = sourceContentOwner.getDynamicContent(sourceContentType);
            if (sourceContent != null) {
                return sourceContent;
            }
        }
        return VoidDynamicContent.INSTANCE;
    }

    @Override
    public void dispose() {
        sourceContentOwner = null;
    }
}
