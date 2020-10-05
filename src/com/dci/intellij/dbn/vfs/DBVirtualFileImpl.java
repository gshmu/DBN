package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.WeakRef;
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

@Nullifiable
public abstract class DBVirtualFileImpl extends VirtualFile implements DBVirtualFile, Presentable, VirtualFilePathWrapper {
    private static AtomicInteger ID_STORE = new AtomicInteger(0);
    private final int id;
    private final ProjectRef projectRef;
    private final WeakRef<DatabaseFileSystem> fileSystem;

    protected String name;
    protected String path;
    protected String url;
    private int documentHashCode;
    private boolean valid = true;

    public DBVirtualFileImpl(@NotNull Project project) {
        //id = ID_STORE.getAndIncrement();
        id = DummyFileIdGenerator.next();
        projectRef = ProjectRef.from(project);
        fileSystem = WeakRef.of(DatabaseFileSystem.getInstance());
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
        return Failsafe.nn(WeakRef.get(fileSystem));
    }

    @Override
    @NotNull
    public Project getProject() {
        return projectRef.ensure();
    }

    @Override
    @NotNull
    public Project ensureProject() {
        return projectRef.ensure();
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
        return valid;
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

    public void invalidate() {
        if (valid) {
            valid = false;
            DatabaseFileViewProvider cachedViewProvider = getCachedViewProvider();
            if (cachedViewProvider != null) {
                DebugUtil.performPsiModification("disposing database view provider", () -> cachedViewProvider.markInvalidated());
                List<PsiFile> cachedPsiFiles = cachedViewProvider.getCachedPsiFiles();
                for (PsiFile cachedPsiFile: cachedPsiFiles) {
                    if (cachedPsiFile instanceof DBLanguagePsiFile) {
                        Disposer.dispose(cachedPsiFile);
                    }
                }

                setCachedViewProvider(null);
            }
            putUserData(FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY, null);
        }
    }

}
