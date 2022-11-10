package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import org.jetbrains.annotations.NotNull;

public interface SubcontentDependencyAdapter extends ContentDependencyAdapter{
    @NotNull
    DynamicContent getSourceContent();

    boolean isSourceContentReady();

    static SubcontentDependencyAdapter create(@NotNull DatabaseEntity sourceContentOwner, @NotNull DynamicContentType sourceContentType) {
        return new SubcontentDependencyAdapterImpl(sourceContentOwner, sourceContentType);
    }

}
