package com.dci.intellij.dbn.connection.session;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseSessionBundle extends DisposableBase implements Disposable{
    private ConnectionHandlerRef connectionHandlerRef;
    private DatabaseSession mainSession;
    private DatabaseSession debugSession;
    private DatabaseSession debuggerSession;
    private DatabaseSession poolSession;

    private List<DatabaseSession> sessions = CollectionUtil.createConcurrentList();

    public DatabaseSessionBundle(ConnectionHandler connectionHandler) {
        super(connectionHandler);
        this.connectionHandlerRef = connectionHandler.getRef();

        mainSession = new DatabaseSession(SessionId.MAIN, "Main", ConnectionType.MAIN, connectionHandler);
        sessions.add(mainSession);

        if (DatabaseFeature.DEBUGGING.isSupported(connectionHandler)) {
            debugSession = new DatabaseSession(SessionId.DEBUG, "Debug", ConnectionType.DEBUG, connectionHandler);
            debuggerSession = new DatabaseSession(SessionId.DEBUGGER, "Debugger", ConnectionType.DEBUGGER, connectionHandler);
            sessions.add(debugSession);
            sessions.add(debuggerSession);
        }

        poolSession = new DatabaseSession(SessionId.POOL, "Pool", ConnectionType.POOL, connectionHandler);
        sessions.add(poolSession);
    }

    public List<DatabaseSession> getSessions(ConnectionType ... connectionTypes) {
        List<DatabaseSession> sessions = new ArrayList<>();
        for (DatabaseSession session : this.sessions) {
            if (session.getConnectionType().matches(connectionTypes)) {
                sessions.add(session);
            }
        }

        return sessions;
    }

    public Set<String> getSessionNames() {
        Set<String> sessionNames = new HashSet<String>();
        for (DatabaseSession session : sessions) {
            sessionNames.add(session.getName());
        }

        return sessionNames;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.getnn();
    }

    public DatabaseSession getDebugSession() {
        return debugSession;
    }

    public DatabaseSession getDebuggerSession() {
        return debuggerSession;
    }

    public DatabaseSession getMainSession() {
        return mainSession;
    }

    public DatabaseSession getPoolSession() {
        return poolSession;
    }

    @Nullable
    public DatabaseSession getSession(String name) {
        for (DatabaseSession session : sessions) {
            if (session.getName().equals(name)) {
                return session;
            }
        }
        return null;
    }

    @NotNull
    public DatabaseSession getSession(SessionId id) {
        for (DatabaseSession session : sessions) {
            if (session.getId() == id) {
                return session;
            }
        }
        return mainSession;
    }

    void addSession(SessionId id, String name) {
        sessions.add(new DatabaseSession(id, name, ConnectionType.SESSION, getConnectionHandler()));
        java.util.Collections.sort(sessions);
    }

    DatabaseSession createSession(String name) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseSession session = new DatabaseSession(null, name, ConnectionType.SESSION, connectionHandler);
        sessions.add(session);
        java.util.Collections.sort(sessions);
        return session;
    }

    void deleteSession(SessionId id) {
        DatabaseSession session = getSession(id);
        sessions.remove(session);
        DisposerUtil.dispose(session);
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(sessions);
        CollectionUtil.clear(sessions);
    }

    void renameSession(String oldName, String newName) {
        DatabaseSession session = getSession(oldName);
        if (session != null) {
            session.setName(newName);
        }
    }
}
