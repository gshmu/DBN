package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import java.io.IOException;
import java.io.InputStream;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBView;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;

public abstract class DBContentVirtualFile extends DBVirtualFileImpl implements FileConnectionMappingProvider  {
    protected DBEditableObjectVirtualFile mainDatabaseFile;
    protected DBContentType contentType;
    private FileType fileType;
    private boolean modified;

    public DBContentVirtualFile(@NotNull DBEditableObjectVirtualFile mainDatabaseFile, DBContentType contentType) {
        super(mainDatabaseFile.getProject());
        this.mainDatabaseFile = mainDatabaseFile;
        this.contentType = contentType;

        DBSchemaObject object = mainDatabaseFile.getObject();
        this.name = object.getName();

        DDLFileType ddlFileType = object.getDDLFileType(contentType);
        this.fileType = ddlFileType == null ? null : ddlFileType.getLanguageFileType();
    }

    @Nullable
    public ConnectionHandler getActiveConnection() {
        return getObject().getConnectionHandler();
    }

    @Nullable
    public DBSchema getCurrentSchema() {
        return getObject().getSchema();
    }

    @NotNull
    public DBEditableObjectVirtualFile getMainDatabaseFile() {
        return FailsafeUtil.get(mainDatabaseFile);
    }

    public DBContentType getContentType() {
        return contentType;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && mainDatabaseFile != null && mainDatabaseFile.isValid();
    }

    @NotNull
    public DBSchemaObject getObject() {
        return getMainDatabaseFile().getObject();
    }

    @NotNull
    @Override
    public ConnectionHandler getConnectionHandler() {
        return getMainDatabaseFile().getConnectionHandler();
    }

    @NotNull
    @Override
    public String getConnectionId() {
        return getMainDatabaseFile().getConnectionId();
    }

    public DBLanguageDialect getLanguageDialect() {
        DBSchemaObject object = getObject();
        DBLanguage language =
                object instanceof DBView ?
                        SQLLanguage.INSTANCE :
                        PSQLLanguage.INSTANCE;
        
        return object.getLanguageDialect(language);
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/
    @NotNull
    @NonNls
    public String getName() {
        return name;
    }

    @NotNull
    public FileType getFileType() {
        return fileType;
    }

    @NotNull
    @Override
    protected String createPath() {
        return DatabaseFileSystem.createPath(getObject().getRef(), contentType);
    }

    @NotNull
    @Override
    protected String createUrl() {
        return DatabaseFileSystem.createUrl(getObject().getRef(), contentType);
    }

    public boolean isWritable() {
        return true;
    }

    public boolean isDirectory() {
        return false;
    }

    @Nullable
    public VirtualFile getParent() {
        if (!isDisposed()) {
            DBObject parentObject = getObject().getParentObject();
            if (parentObject != null) {
                return parentObject.getVirtualFile();
            }
        }
        return null;
    }

    public Icon getIcon() {
        return getObject().getOriginalIcon();
    }

    public VirtualFile[] getChildren() {
        return VirtualFile.EMPTY_ARRAY;
    }

    public long getTimeStamp() {
        return 0;
    }

    public void refresh(boolean b, boolean b1, Runnable runnable) {

    }

    public InputStream getInputStream() throws IOException {
        return DevNullStreams.INPUT_STREAM;
    }

    public long getModificationStamp() {
        return 1;
    }

    @Override
    public void dispose() {
        super.dispose();
        mainDatabaseFile = null;
    }
}
