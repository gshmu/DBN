package com.dci.intellij.dbn.connection.session;

import com.dci.intellij.dbn.common.dispose.Disposed;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.index.IdentifiableMap;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.connection.ConnectionType;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static com.dci.intellij.dbn.common.dispose.SafeDisposer.replace;
import static com.dci.intellij.dbn.common.util.Commons.nvl;
import static com.dci.intellij.dbn.common.util.Lists.filtered;
import static com.dci.intellij.dbn.common.util.Lists.first;

public class DatabaseSessionBundle extends StatefulDisposable.Base implements Disposable{
    private final ConnectionRef connection;
    private DatabaseSession mainSession;
    private DatabaseSession debugSession;
    private DatabaseSession debuggerSession;
    private DatabaseSession poolSession;

    private List<DatabaseSession> sessions = CollectionUtil.createConcurrentList();
    private final IdentifiableMap<SessionId, DatabaseSession> index = new IdentifiableMap<>();

    public DatabaseSessionBundle(ConnectionHandler connection) {
        super(connection);
        this.connection = connection.ref();

        mainSession = new DatabaseSession(SessionId.MAIN, "Main", ConnectionType.MAIN, connection);
        sessions.add(mainSession);

        if (!connection.isVirtual()) {
            if (DatabaseFeature.DEBUGGING.isSupported(connection)) {
                debugSession = new DatabaseSession(SessionId.DEBUG, "Debug", ConnectionType.DEBUG, connection);
                debuggerSession = new DatabaseSession(SessionId.DEBUGGER, "Debugger", ConnectionType.DEBUGGER, connection);
                sessions.add(debugSession);
                sessions.add(debuggerSession);
            }

            poolSession = new DatabaseSession(SessionId.POOL, "Pool", ConnectionType.POOL, connection);
            sessions.add(poolSession);
            rebuildIndex();
        }
    }

    private void rebuildIndex() {
        Collections.sort(sessions);
        this.index.rebuild(sessions);
    }

    public List<DatabaseSession> getSessions(ConnectionType ... connectionTypes) {
        List<DatabaseSession> sessions = filtered(this.sessions, session -> session.getConnectionType().matches(connectionTypes));
        sessions.sort(Comparator.comparingInt(session -> session.getConnectionType().getPriority()));
        return sessions;
    }

    public Set<String> getSessionNames() {
        return sessions.stream().map(s -> s.getName()).collect(Collectors.toSet());
    }

    public ConnectionHandler getConnection() {
        return connection.ensure();
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

    public boolean hasSession(SessionId id) {
        return index.contains(id);
    }

    @NotNull
    public DatabaseSession getSession(SessionId id) {
        DatabaseSession session = index.get(id);
        return nvl(session, getMainSession());
    }

    void addSession(SessionId id, String name) {
        sessions.add(new DatabaseSession(id, name, ConnectionType.SESSION, getConnection()));
        rebuildIndex();
    }

    DatabaseSession createSession(String name) {
        ConnectionHandler connection = getConnection();
        DatabaseSession session = new DatabaseSession(null, name, ConnectionType.SESSION, connection);
        sessions.add(session);
        rebuildIndex();
        return session;
    }

    void deleteSession(SessionId id) {
        DatabaseSession session = getSession(id);
        rebuildIndex();
    }

    void renameSession(String oldName, String newName) {
        DatabaseSession session = getSession(oldName);
        if (session != null) {
            session.setName(newName);
        }
    }

    @Override
    public void disposeInner() {
        sessions = replace(sessions, Disposed.list(), false);
        index.clear();
        nullify();
    }
}
