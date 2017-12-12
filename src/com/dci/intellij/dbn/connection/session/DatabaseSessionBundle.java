package com.dci.intellij.dbn.connection.session;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.intellij.openapi.Disposable;

public class DatabaseSessionBundle extends DisposableBase implements Disposable{
    private ConnectionHandlerRef connectionHandlerRef;

    private List<DatabaseSession> sessions = new CopyOnWriteArrayList<>();

    public DatabaseSessionBundle(ConnectionHandler connectionHandler) {
        super(connectionHandler);
        this.connectionHandlerRef = connectionHandler.getRef();
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

    @Nullable
    public DatabaseSession getSession(String name) {
        for (DatabaseSession session : sessions) {
            if (session.getName().equals(name)) {
                return session;
            }
        }
        return null;
    }

    public DatabaseSession getSession(String name, boolean create) {
        DatabaseSession session = getSession(name);
        if (session == null && create) {
            synchronized (this) {
                session = getSession(name);
                if (session == null) {
                    return createSession(name);
                }
            }
        }
        return session;
    }

    public DatabaseSession createSession(String name) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DatabaseSession session = new DatabaseSession(connectionHandler, name);
        sessions.add(session);
        Collections.sort(sessions);
        return session;
    }

    public void removeSession(String name) {
        DatabaseSession session = getSession(name);
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
