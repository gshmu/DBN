package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.PsiDocumentManagerBase;

public abstract class DBVirtualFileImpl extends VirtualFile implements DBVirtualFile {
    private static AtomicInteger ID_STORE = new AtomicInteger(0);
    private int id;
    protected String name;
    protected String path;
    protected String url;

    public DBVirtualFileImpl() {
        id = ID_STORE.getAndIncrement();
    }

    @Override
    public boolean isInLocalFileSystem() {
        return false;
    }

    public abstract Icon getIcon();

    @NotNull
    public abstract ConnectionHandler getConnectionHandler();

    public int getId() {
        return id;
    }

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
        return id;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof DBVirtualFileImpl && hashCode() == obj.hashCode();
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
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        Document cachedDocument = FileDocumentManager.getInstance().getCachedDocument(this);
        if (cachedDocument != null) PsiDocumentManagerBase.cachePsi(cachedDocument, null);
        putUserData(FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY, null);
    }


}
