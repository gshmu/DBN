package com.dci.intellij.dbn.connection.console;

import com.dci.intellij.dbn.common.dispose.DisposableContainers;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposableBase;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.object.impl.DBConsoleImpl;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.dci.intellij.dbn.common.dispose.Failsafe.nd;

public class DatabaseConsoleBundle extends StatefulDisposableBase {
    private final ConnectionRef connection;

    private final List<DBConsole> consoles = DisposableContainers.concurrentList(this);

    public DatabaseConsoleBundle(ConnectionHandler connection) {
        super(connection);
        this.connection = connection.ref();
    }

    public List<DBConsole> getConsoles() {
        if (consoles.isEmpty()) {
            synchronized (this) {
                if (consoles.isEmpty()) {
                    createConsole(getConnection().getName(), DBConsoleType.STANDARD);
                }
            }
        }
        return consoles;
    }

    @NotNull
    private Project getProject() {
        return getConnection().getProject();
    }

    public Set<String> getConsoleNames() {
        Set<String> consoleNames = new HashSet<>();
        for (DBConsole console : consoles) {
            consoleNames.add(console.getName());
        }

        return consoleNames;
    }

    public ConnectionHandler getConnection() {
        return connection.ensure();
    }

    @NotNull
    public DBConsole getDefaultConsole() {
        return getConsole(getConnection().getName(), DBConsoleType.STANDARD, true);
    }

    @Nullable
    public DBConsole getConsole(String name) {
        for (DBConsole console : consoles) {
            if (Objects.equals(console.getName(), name)) {
                return console;
            }
        }
        return null;
    }

    @NotNull
    public DBConsole ensureConsole(String name) {
        DBConsole console = getConsole(name);
        return nd(console);
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
        ConnectionHandler connection = getConnection();
        DBConsole console = new DBConsoleImpl(connection, name, type);
        consoles.add(console);
        Collections.sort(consoles);
        return console;
    }

    void removeConsole(String name) {
        DBConsole console = getConsole(name);
        removeConsole(console);
        Disposer.dispose(console, false);
    }

    void removeConsole(DBConsole console) {
        consoles.remove(console);
    }

    @Override
    public void disposeInner() {
    }

    void renameConsole(String oldName, String newName) {
        if (!Objects.equals(oldName, newName)) {
            DBConsole console = ensureConsole(oldName);
            console.setName(newName);
        }
    }
}
