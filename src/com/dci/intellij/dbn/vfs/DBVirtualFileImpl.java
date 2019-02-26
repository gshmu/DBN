package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFilePathWrapper;
import com.intellij.openapi.vfs.ex.dummy.DummyFileIdGenerator;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DBVirtualFileImpl extends VirtualFile implements DBVirtualFile, Presentable, VirtualFilePathWrapper {
    private static AtomicInteger ID_STORE = new AtomicInteger(0);
    private int documentHashCode;
    private int id;
    protected String name;
    protected String path;
    protected String url;
    private ProjectRef projectRef;
    private DatabaseFileSystem fileSystem = DatabaseFileSystem.getInstance();

    public DBVirtualFileImpl(Project project) {
        //id = ID_STORE.getAndIncrement();
        id = DummyFileIdGenerator.next();
        projectRef = ProjectRef.from(project);
    }



    @NotNull
    @Override
    public EnvironmentType getEnvironmentType() {
        return getConnectionHandler().getEnvironmentType();
    }

    public int getDocumentHashCode() {
        return documentHashCode;
    }

    public void setDocumentHashCode(int documentHashCode) {
        this.documentHashCode = documentHashCode;
    }

    @NotNull
    public ConnectionId getConnectionId() {
        return getConnectionHandler().getConnectionId();
    }

    @Override
    public boolean isInLocalFileSystem() {
        return false;
    }

    @NotNull
    @Override
    public DatabaseFileSystem getFileSystem() {
        return fileSystem;
    }

    @Override
    @Nullable
    public Project getProject() {
        return projectRef.get();
    }

    @Override
    @NotNull
    public Project ensureProject() {
        return projectRef.getnn();
    }


    @Override
    public abstract Icon getIcon();

    public int getId() {
        return id;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
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
    public String getPresentablePath() {
        return getFileSystem().extractPresentablePath(getPath());
    }

    @Override
    public boolean enforcePresentableName() {
        return false;
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
    public void rename(Object requestor, @NotNull String newName) throws IOException {
        throw DatabaseFileSystem.READONLY_FILE_SYSTEM;
    }

    @Override
    public void move(Object requestor, @NotNull VirtualFile newParent) throws IOException {
        throw DatabaseFileSystem.READONLY_FILE_SYSTEM;
    }

    @NotNull
    @Override
    public VirtualFile copy(Object requestor, @NotNull VirtualFile newParent, @NotNull String copyName) throws IOException {
        throw DatabaseFileSystem.READONLY_FILE_SYSTEM;
    }

    @Override
    public void delete(Object requestor) throws IOException {
        throw DatabaseFileSystem.READONLY_FILE_SYSTEM;
    }

    @Override
    public final int hashCode() {
        return id;
    }

    @Override
    public final boolean equals(Object obj) {
        return this == obj || (obj instanceof DBVirtualFileImpl && hashCode() == obj.hashCode());
    }

    @NotNull
    private String createPath() {
        return DatabaseFileSystem.createPath(this);
    }
    @NotNull
    private String createUrl() {
        return DatabaseFileSystem.createUrl(this);
    }

    @Override
    public boolean isValid() {
        return !isDisposed() && getProject() != null;
    }

    private boolean disposed;

    @Override
    public final boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        if (!disposed) {
            disposed = true;
            DatabaseFileViewProvider cachedViewProvider = getCachedViewProvider();
            if (cachedViewProvider != null) {
                DebugUtil.performPsiModification("disposing database view provider", () -> cachedViewProvider.markInvalidated());
                List<PsiFile> cachedPsiFiles = cachedViewProvider.getCachedPsiFiles();
                for (PsiFile cachedPsiFile: cachedPsiFiles) {
                    if (cachedPsiFile instanceof DBLanguagePsiFile) {
                        DisposerUtil.dispose((DBLanguagePsiFile) cachedPsiFile);
                    }
                }

                setCachedViewProvider(null);
            }
            putUserData(FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY, null);
        }
    }

    @Override
    public void setCachedViewProvider(@Nullable DatabaseFileViewProvider viewProvider) {
        putUserData(DatabaseFileViewProvider.CACHED_VIEW_PROVIDER, viewProvider);
    }

    @Override
    @Nullable
    public DatabaseFileViewProvider getCachedViewProvider() {
        return getUserData(DatabaseFileViewProvider.CACHED_VIEW_PROVIDER);
    }
}
