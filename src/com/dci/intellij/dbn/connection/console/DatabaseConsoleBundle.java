package com.dci.intellij.dbn.connection.console;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.vfs.DBConsoleVirtualFile;
import com.intellij.openapi.Disposable;

public class DatabaseConsoleBundle implements Disposable{
    private ConnectionHandlerRef connectionHandlerRef;

    private List<DBConsoleVirtualFile> consoles = new ArrayList<DBConsoleVirtualFile>();

    public DatabaseConsoleBundle(ConnectionHandler connectionHandler) {
        this.connectionHandlerRef = connectionHandler.getRef();
    }

    public List<DBConsoleVirtualFile> getConsoles() {
        if (consoles.size() == 0) {
            synchronized (this) {
                if (consoles.size() == 0) {
                    createConsole(getConnectionHandler().getName());
                }
            }

        }
        return consoles;
    }

    public Set<String> getConsoleNames() {
        Set<String> consoleNames = new HashSet<String>();
        for (DBConsoleVirtualFile console : consoles) {
            consoleNames.add(console.getName());
        }

        return consoleNames;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @NotNull
    public DBConsoleVirtualFile getDefaultConsole() {
        return getConsole(getConnectionHandler().getName(), true);
    }

    @Nullable
    public DBConsoleVirtualFile getConsole(String name) {
        for (DBConsoleVirtualFile console : consoles) {
            if (console.getName().equals(name)) {
                return console;
            }
        }
        return null;
    }

    public DBConsoleVirtualFile getConsole(String name, boolean create) {
        DBConsoleVirtualFile console = getConsole(name);
        if (console == null && create) {
            synchronized (this) {
                console = getConsole(name);
                if (console == null) {
                    return createConsole(name);
                }
            }
        }
        return console;
    }

    public DBConsoleVirtualFile createConsole(String name) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBConsoleVirtualFile console = new DBConsoleVirtualFile(connectionHandler, name);
        consoles.add(console);
        Collections.sort(consoles);
        List<String> consoleNames = connectionHandler.getSettings().getConsoleNames();
        if (!consoleNames.contains(name)) {
            consoleNames.add(name);
            Collections.sort(consoleNames);
        }

        return console;
    }

    public void removeConsole(String name) {
        DBConsoleVirtualFile console = getConsole(name);
        consoles.remove(console);
        DisposerUtil.dispose(console);
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(consoles);
    }

    public void renameConsole(String oldName, String newName) {
        DBConsoleVirtualFile console = getConsole(oldName);
        if (console != null) {
            console.setName(newName);
        }
    }
}
