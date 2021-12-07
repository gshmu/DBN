package com.dci.intellij.dbn.connection.session;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.dci.intellij.dbn.common.util.Lists.filtered;
import static com.dci.intellij.dbn.common.util.Lists.first;

public class DatabaseSessionBundle extends StatefulDisposable.Base implements Disposable{
    private final ConnectionHandlerRef connectionHandler;
    private DatabaseSession mainSession;
    private DatabaseSession debugSession;
    private DatabaseSession debuggerSession;
    private DatabaseSession poolSession;

    private final List<DatabaseSession> sessions = CollectionUtil.createConcurrentList();

    public DatabaseSessionBundle(ConnectionHandler connectionHandler) {
        super(connectionHandler);
        this.connectionHandler = connectionHandler.getRef();

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
        List<DatabaseSession> sessions = filtered(this.sessions, session -> session.getConnectionType().matches(connectionTypes));
        sessions.sort(Comparator.comparingInt(session -> session.getConnectionType().getPriority()));
        return sessions;
    }

    public Set<String> getSessionNames() {
        Set<String> sessionNames = new HashSet<>();
        for (DatabaseSession session : sessions) {
            sessionNames.add(session.getName());
        }

        return sessionNames;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    public DatabaseSession getDebugSession() {
        return debugSession;
    }

    public DatabaseSession getDebuggerSession() {
        return debuggerSession;
    }

    @NotNull
    public DatabaseSession getMainSession() {
        return Failsafe.nn(mainSession);
    }

    public DatabaseSession getPoolSession() {
        return poolSession;
    }

    @Nullable
    public DatabaseSession getSession(String name) {
        return first(sessions, session -> Objects.equals(session.getName(), name));
    }

    @NotNull
    public DatabaseSession getSession(SessionId id) {
        for (DatabaseSession session : sessions) {
            if (session.getId() == id) {
                return session;
            }
        }
        return getMainSession();
    }

    void addSession(SessionId id, String name) {
        sessions.add(new DatabaseSession(id, name, ConnectionType.SESSION, getConnectionHandler()));
        Collections.sort(sessions);
    }

    DatabaseSession createSession(String name) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseSession session = new DatabaseSession(null, name, ConnectionType.SESSION, connectionHandler);
        sessions.add(session);
        Collections.sort(sessions);
        return session;
    }

    void deleteSession(SessionId id) {
        DatabaseSession session = getSession(id);
        sessions.remove(session);
    }

    void renameSession(String oldName, String newName) {
        DatabaseSession session = getSession(oldName);
        if (session != null) {
            session.setName(newName);
        }
    }

    @Override
    public void disposeInner() {
        SafeDisposer.dispose(sessions, true, false);
        mainSession = null;
        debugSession = null;
        debuggerSession = null;
        poolSession = null;
    }
}
