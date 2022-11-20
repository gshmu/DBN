package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.environment.EnvironmentType;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.dci.intellij.dbn.common.ui.Presentable;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFilePathWrapper;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
public abstract class DBVirtualFileImpl extends VirtualFile implements DBVirtualFile, Presentable, VirtualFilePathWrapper {
    private static final AtomicInteger ID_STORE = new AtomicInteger(1000);
    private final int id;
    private final ProjectRef project;
    private final WeakRef<DatabaseFileSystem> fileSystem;

    protected volatile String path;
    protected volatile String url;
    private volatile boolean valid = true;
    private volatile int documentSignature;


    private String name;
    public DBVirtualFileImpl(@NotNull Project project, @NotNull String name) {
        this.id = ID_STORE.incrementAndGet();
        this.name = name;
        //id = DummyFileIdGenerator.next();
        this.project = ProjectRef.of(project);
        this.fileSystem = WeakRef.of(DatabaseFileSystem.getInstance());
    }

    @NotNull
    @Override
    public EnvironmentType getEnvironmentType() {
        return getConnection().getEnvironmentType();
    }

    @NotNull
    public ConnectionId getConnectionId() {
        return getConnection().getConnectionId();
    }

    @Override
    public boolean isInLocalFileSystem() {
        return false;
    }

    @NotNull
    @Override
    public DatabaseFileSystem getFileSystem() {
        return fileSystem.ensure();
    }

    @Override
    @NotNull
    public Project getProject() {
        return project.ensure();
    }

    @Override
    public abstract Icon getIcon();

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public final String getPath() {
        if (path == null)
            path = DatabaseFileSystem.createFilePath(this);
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
        if (url == null)
            url = DatabaseFileSystem.createFileUrl(this);
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
    public void setCachedViewProvider(@Nullable DatabaseFileViewProvider viewProvider) {
        putUserData(DatabaseFileViewProvider.CACHED_VIEW_PROVIDER, viewProvider);
    }

    @Override
    @Nullable
    public DatabaseFileViewProvider getCachedViewProvider() {
        return getUserData(DatabaseFileViewProvider.CACHED_VIEW_PROVIDER);
    }

    public void invalidate() {
        if (!valid) return;

        valid = false;
        DatabaseFileViewProvider cachedViewProvider = getCachedViewProvider();
        if (cachedViewProvider != null) {
            DebugUtil.performPsiModification("disposing database view provider", () -> cachedViewProvider.markInvalidated());
            List<PsiFile> cachedPsiFiles = cachedViewProvider.getCachedPsiFiles();
            for (PsiFile cachedPsiFile: cachedPsiFiles) {
                if (cachedPsiFile instanceof DBLanguagePsiFile) {
                    DBLanguagePsiFile languagePsiFile = (DBLanguagePsiFile) cachedPsiFile;
                    Disposer.dispose(languagePsiFile);
                }
            }

            setCachedViewProvider(null);
        }
        putUserData(FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY, null);
    }

}
