package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.DynamicContentType;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.util.Unsafe;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DatabaseEntity extends ConnectionProvider, StatefulDisposable {
    @NotNull
    String getName();

    @NotNull
    default String getQualifiedName() {
        return getName();
    }

    @NotNull
    Project getProject();

    @Nullable
    default <E extends DatabaseEntity> E getParentEntity() {
        return null;
    }

    @Nullable
    default <E extends DatabaseEntity> E getUndisposedEntity() {
        return Unsafe.cast(this);
    }

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
