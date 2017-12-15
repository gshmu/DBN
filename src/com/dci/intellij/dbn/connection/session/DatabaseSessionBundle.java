package com.dci.intellij.dbn.connection.session;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.SessionId;
import com.intellij.openapi.Disposable;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class DatabaseSessionBundle extends DisposableBase implements Disposable{
    private ConnectionHandlerRef connectionHandlerRef;
    private DatabaseSession mainSession;
    private DatabaseSession poolSession;

    private List<DatabaseSession> sessions = new CopyOnWriteArrayList<DatabaseSession>();

    public DatabaseSessionBundle(ConnectionHandler connectionHandler) {
        super(connectionHandler);
        this.connectionHandlerRef = connectionHandler.getRef();
        mainSession = new DatabaseSession(SessionId.MAIN, "Main", connectionHandler);
        poolSession = new DatabaseSession(SessionId.POOL, "Pool", connectionHandler);
        sessions.add(mainSession);
        sessions.add(poolSession);
    }

    public List<DatabaseSession> getSessions() {
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
        return connectionHandlerRef.get();
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

    @Nullable
    public DatabaseSession getSession(SessionId id) {
        for (DatabaseSession session : sessions) {
            if (session.getId() == id) {
                return session;
            }
        }
        return null;
    }

    public void addSession(SessionId id, String name) {
        sessions.add(new DatabaseSession(id, name, getConnectionHandler()));
        Collections.sort(sessions);
    }

    public DatabaseSession createSession(String name) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseSession session = new DatabaseSession(null, name, connectionHandler);
        sessions.add(session);
        Collections.sort(sessions);
        return session;
    }

    public void removeSession(SessionId id) {
        DatabaseSession session = getSession(id);
        sessions.remove(session);
        DisposerUtil.dispose(session);
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(sessions);
    }

    public void renameSession(String oldName, String newName) {
        DatabaseSession session = getSession(oldName);
        if (session != null) {
            session.setName(newName);
        }
    }
}
