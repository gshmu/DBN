package com.dci.intellij.dbn.connection.context;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaces;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfacesProvider;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceContext;
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

    default InterfaceContext context() {
        return InterfaceContext.create(ensureConnection(), null, true);
    }
}
