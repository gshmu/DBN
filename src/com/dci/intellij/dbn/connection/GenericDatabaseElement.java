package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.intellij.openapi.project.Project;

public interface GenericDatabaseElement {
    Project getProject();
    ConnectionHandler getConnectionHandler();
    GenericDatabaseElement getUndisposedElement();
    DynamicContent getDynamicContent(DynamicContentType dynamicContentType);
}
