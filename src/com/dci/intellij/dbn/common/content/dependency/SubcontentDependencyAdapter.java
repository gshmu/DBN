package com.dci.intellij.dbn.common.content.dependency;

import com.dci.intellij.dbn.common.content.DynamicContent;
import org.jetbrains.annotations.NotNull;

public interface SubcontentDependencyAdapter extends ContentDependencyAdapter{
    @NotNull
    DynamicContent getSourceContent();

    boolean isSourceContentReady();

}
