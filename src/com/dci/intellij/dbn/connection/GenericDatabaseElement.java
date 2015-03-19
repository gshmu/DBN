package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.openapi.project.Project;

public interface GenericDatabaseElement extends ConnectionProvider, Disposable {
    Project getProject();
    GenericDatabaseElement getUndisposedElement();
    DynamicContent getDynamicContent(DynamicContentType dynamicContentType);
}
