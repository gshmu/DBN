package com.dci.intellij.dbn.connection.session;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.SessionId;

public class DatabaseSession extends DisposableBase implements Comparable<DatabaseSession>, Presentable {
    private ConnectionHandlerRef connectionHandlerRef;
    private final SessionId id;
    private String name;

    public DatabaseSession(SessionId id, String name, ConnectionHandler connectionHandler) {
        this.id = id == null ? SessionId.create() : id;
        this.name = name;
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    @NotNull
    public SessionId getId() {
        return id;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return
            isMain() ? Icons.SESSION_MAIN :
            isPool() ? Icons.SESSION_POOL :
                       Icons.SESSION_CUSTOM;
    }

    public boolean isMain() {
        return id == SessionId.MAIN;
    }

    public boolean isPool() {
        return id == SessionId.POOL;
    }

    public boolean isCustom() {
        return !isMain() && !isPool();
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public int compareTo(@NotNull DatabaseSession o) {
        return name.compareTo(o.name);
    }
}
