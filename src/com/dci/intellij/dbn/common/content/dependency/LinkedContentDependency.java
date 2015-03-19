package com.dci.intellij.dbn.common.content.dependency;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;

public class LinkedContentDependency extends ContentDependency {
    private GenericDatabaseElement sourceContentOwner;
    private DynamicContentType sourceContentType;

    public LinkedContentDependency(@NotNull GenericDatabaseElement sourceContentOwner, @NotNull DynamicContentType sourceContentType) {
        this.sourceContentOwner = sourceContentOwner;
        this.sourceContentType = sourceContentType;
        reset();
    }

    @NotNull
    public DynamicContent getSourceContent() {
        if (sourceContentOwner != null) {
            DynamicContent sourceContent = sourceContentOwner.getDynamicContent(sourceContentType);
            if (sourceContent != null) {
                return sourceContent;
            }
        }
        throw AlreadyDisposedException.INSTANCE;
    }

    public void dispose() {
        sourceContentOwner = null;
    }
}
