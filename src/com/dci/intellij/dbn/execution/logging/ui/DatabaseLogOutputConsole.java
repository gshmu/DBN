package com.dci.intellij.dbn.execution.logging.ui;

import java.io.Reader;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.diagnostic.logging.DefaultLogFilterModel;
import com.intellij.diagnostic.logging.LogConsoleBase;

public class DatabaseLogOutputConsole extends LogConsoleBase{
    private ConnectionHandlerRef connectionHandlerRef;
    public DatabaseLogOutputConsole(@NotNull ConnectionHandler connectionHandler, Reader reader, String title) {
        super(connectionHandler.getProject(), reader, title, true, createFilterModel(connectionHandler));
        connectionHandlerRef = connectionHandler.getRef();
    }

    private static DefaultLogFilterModel createFilterModel(ConnectionHandler connectionHandler) {
        return new DefaultLogFilterModel(connectionHandler.getProject());
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public ConnectionHandler getConnectionHandler() {
        return ConnectionHandlerRef.get(connectionHandlerRef);
    }
}
