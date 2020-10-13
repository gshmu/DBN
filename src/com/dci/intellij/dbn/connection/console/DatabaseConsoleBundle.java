package com.dci.intellij.dbn.connection.console;

import com.dci.intellij.dbn.common.dispose.DisposableContainer;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.thread.Synchronized;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.object.impl.DBConsoleImpl;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseConsoleBundle extends StatefulDisposable.Base {
    private final ConnectionHandlerRef connectionHandler;

    private final List<DBConsole> consoles = DisposableContainer.concurrentList(this);

    public DatabaseConsoleBundle(ConnectionHandler connectionHandler) {
        super(connectionHandler);
        this.connectionHandler = connectionHandler.getRef();
    }

    public List<DBConsole> getConsoles() {
        Synchronized.run(this,
                () -> consoles.isEmpty(),
                () -> createConsole(getConnectionHandler().getName(), DBConsoleType.STANDARD));
        return consoles;
    }

    public Set<String> getConsoleNames() {
        Set<String> consoleNames = new HashSet<String>();
        for (DBConsole console : consoles) {
            consoleNames.add(console.getName());
        }

        return consoleNames;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler.ensure();
    }

    @NotNull
    public DBConsole getDefaultConsole() {
        return getConsole(getConnectionHandler().getName(), DBConsoleType.STANDARD, true);
    }

    @Nullable
    public DBConsole getConsole(String name) {
        for (DBConsole console : consoles) {
            if (console.getName().equals(name)) {
                return console;
            }
        }
        return null;
    }

    public DBConsole getConsole(String name, DBConsoleType type, boolean create) {
        DBConsole console = getConsole(name);
        if (console == null && create) {
            synchronized (this) {
                console = getConsole(name);
                if (console == null) {
                    return createConsole(name, type);
                }
            }
        }
        return console;
    }

    DBConsole createConsole(String name, DBConsoleType type) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBConsole console = new DBConsoleImpl(connectionHandler, name, type);
        consoles.add(console);
        Collections.sort(consoles);
        return console;
    }

    void removeConsole(String name) {
        DBConsole console = getConsole(name);
        consoles.remove(console);
        SafeDisposer.dispose(console);
    }

    @Override
    public void disposeInner() {
    }

    void renameConsole(String oldName, String newName) {
        DBConsole console = getConsole(oldName);
        if (console != null) {
            console.setName(newName);
        }
    }
}
