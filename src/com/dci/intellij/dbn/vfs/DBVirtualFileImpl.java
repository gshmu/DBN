package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class DBVirtualFileImpl extends VirtualFile implements DBVirtualFile {
    protected String name;
    protected String path;
    protected String url;
    private int hashCode = -1;

    @Override
    public boolean isInLocalFileSystem() {
        return false;
    }

    public abstract Icon getIcon();

    @NotNull
    public abstract ConnectionHandler getConnectionHandler();

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public final String getPath() {
        if (path == null) {
            synchronized (this) {
                if (path == null) {
                    path = createPath();
                }
            }
        }
        return path;
    }

    @NotNull
    @Override
    public final String getUrl() {
        if (url == null) {
            synchronized (this) {
                if (url == null) {
                    url = createUrl();
                }
            }
        }
        return url;
    }

    @Override
    public final int hashCode() {
        if (disposed) return super.hashCode();
        if (hashCode == -1) {
            synchronized (this) {
                if (hashCode == -1) {
                    DatabaseFileManager databaseFileManager = DatabaseFileManager.getInstance(getProject());
                    String sessionId = databaseFileManager.getSessionId();
                    hashCode = (getUrl() + "#" + sessionId).hashCode();
                }
            }
        }

        return hashCode;
    }

    @Override
    public final boolean equals(Object obj) {
        return !disposed && obj instanceof DBVirtualFileImpl && hashCode() == obj.hashCode();
    }

    @NotNull protected abstract String createPath();
    @NotNull protected abstract String createUrl();
    @NotNull protected abstract Project getProject();

    @Override
    public boolean isValid() {
        return !isDisposed();
    }

    private boolean disposed;

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public final boolean isDisposed() {
        return disposed;
    }


}
