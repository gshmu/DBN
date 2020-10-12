package com.dci.intellij.dbn.connection.session;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DatabaseSession implements Comparable<DatabaseSession>, Presentable {
    private final ConnectionHandlerRef connectionHandler;
    private final ConnectionType connectionType;
    private final SessionId id;
    private String name;

    public DatabaseSession(SessionId id, String name, ConnectionType connectionType, ConnectionHandler connectionHandler) {
        this.id = id == null ? SessionId.create() : id;
        this.name = name;
        this.connectionType = connectionType;
        this.connectionHandler = connectionHandler.getRef();
    }

    @NotNull
    public SessionId getId() {
        return id;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    public ConnectionType getConnectionType() {
        return connectionType;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return Safe.call(Icons.SESSION_CUSTOM, () -> {
            if (isPool()) {
                return Icons.SESSION_POOL;
            } else {
                DBNConnection connection = getConnectionHandler().getConnectionPool().getSessionConnection(id);
                if (connection == null || !connection.isValid()) {
                    return
                            isMain() ?  Icons.SESSION_MAIN :
                            isDebug() ? Icons.SESSION_DEBUG :
                                            Icons.SESSION_CUSTOM;
                } else if (connection.hasDataChanges()) {
                    return
                            isMain() ? Icons.SESSION_MAIN_TRANSACTIONAL :
                            isDebug() ? Icons.SESSION_DEBUG_TRANSACTIONAL :
                                        Icons.SESSION_CUSTOM_TRANSACTIONAL;
                } else {
                    return
                            isMain() ? Icons.SESSION_MAIN :
                            isDebug() ? Icons.SESSION_DEBUG :
                                        Icons.SESSION_CUSTOM;
                }
            }
        });
    }

    public boolean isMain() {
        return id == SessionId.MAIN;
    }

    public boolean isDebug() {
        return id == SessionId.DEBUG || id == SessionId.DEBUGGER;
    }

    public boolean isPool() {
        return id == SessionId.POOL;
    }

    public boolean isCustom() {
        return !isMain() && !isPool() && !isDebug();
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @Override
    public int compareTo(@NotNull DatabaseSession o) {
        if (id == SessionId.MAIN) return -1;
        if (id == SessionId.POOL) {
            return o.id == SessionId.MAIN ? 1 : -1;
        }
        return name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return name;
    }
}
