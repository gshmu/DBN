package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface GenericDatabaseElement extends ConnectionProvider, Disposable {
    @NotNull
    String getName();

    @NotNull
    Project getProject();

    @Nullable GenericDatabaseElement getParentElement();

    GenericDatabaseElement getUndisposedElement();

    @Nullable
    default DynamicContent<?> getDynamicContent(DynamicContentType<?> dynamicContentType) {
        return null;
    }

    default DynamicContentType<?> getDynamicContentType() {
        return null;
    }

    @NotNull
    ConnectionId getConnectionId();

    @NotNull
    @Override
    ConnectionHandler getConnectionHandler();
}
