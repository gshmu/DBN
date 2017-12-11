package com.dci.intellij.dbn.connection.session;

import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.SessionId;

public class DatabaseSession extends DisposableBase implements Comparable<DatabaseSession>{
    private ConnectionHandlerRef connectionHandlerRef;
    private final SessionId id = SessionId.create();
    private String name;

    public DatabaseSession(ConnectionHandler connectionHandler, String name) {
        this.connectionHandlerRef = connectionHandler.getRef();
        this.name = name;
    }

    public SessionId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Override
    public int compareTo(@NotNull DatabaseSession o) {
        return name.compareTo(o.name);
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
