package com.dci.intellij.dbn.connection;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.openapi.project.Project;

public interface GenericDatabaseElement extends ConnectionProvider, Disposable {
    String getName();
    @NotNull Project getProject();
    @Nullable GenericDatabaseElement getParentElement();
    GenericDatabaseElement getUndisposedElement();
    DynamicContent getDynamicContent(DynamicContentType dynamicContentType);
}
