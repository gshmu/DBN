package com.dci.intellij.dbn.connection.context;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceContext;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaces;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfacesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ConnectionProvider extends DatabaseInterfacesProvider {

    @Nullable
    ConnectionHandler getConnection();

    @NotNull
    default ConnectionHandler ensureConnection() {
        return Failsafe.nn(getConnection());
    }

    default DatabaseInterfaces getInterfaces() {
        return ensureConnection().getInterfaces();
    }

    default DatabaseInterfaceContext context() {
        return DatabaseInterfaceContext.create(ensureConnection().getConnectionId(), null, true);
    }
}
