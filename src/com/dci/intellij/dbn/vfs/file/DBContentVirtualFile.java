package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.connection.ConnectionHandler;
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
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.VirtualFileStatus;
import com.dci.intellij.dbn.vfs.VirtualFileStatusHolder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public abstract class DBContentVirtualFile extends DBVirtualFileImpl implements PropertyHolder<VirtualFileStatus>  {
    DBEditableObjectVirtualFile mainDatabaseFile;
    protected DBContentType contentType;
    private FileType fileType;
    private VirtualFileStatusHolder status = new VirtualFileStatusHolder();

    public DBContentVirtualFile(@NotNull DBEditableObjectVirtualFile mainDatabaseFile, DBContentType contentType) {
        super(mainDatabaseFile.getProject());
        this.mainDatabaseFile = mainDatabaseFile;
        this.contentType = contentType;

        DBSchemaObject object = mainDatabaseFile.getObject();
        this.name = object.getName();

        DDLFileType ddlFileType = object.getDDLFileType(contentType);
        this.fileType = ddlFileType == null ? null : ddlFileType.getLanguageFileType();
    }

    @Override
    public boolean set(VirtualFileStatus status, boolean value) {
        return this.status.set(status, value);
    }

    @Override
    public boolean is(VirtualFileStatus status) {
        return this.status.is(status);
    }

    @Override
    public boolean isNot(VirtualFileStatus status) {
        return this.status.isNot(status);
    }

    @Nullable
    public DBSchema getDatabaseSchema() {
        return getObject().getSchema();
    }

    @NotNull
    public DBEditableObjectVirtualFile getMainDatabaseFile() {
        return FailsafeUtil.get(mainDatabaseFile);
    }

    public DBContentType getContentType() {
        return contentType;
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
