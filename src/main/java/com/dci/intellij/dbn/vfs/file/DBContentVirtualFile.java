package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.property.PropertyHolder;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.ddl.DDLFileManager;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.WeakRef;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLLanguage;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBView;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBVirtualFileBase;
import com.dci.intellij.dbn.vfs.VirtualFileStatus;
import com.dci.intellij.dbn.vfs.VirtualFileStatusHolder;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;

public abstract class DBContentVirtualFile extends DBVirtualFileBase implements PropertyHolder<VirtualFileStatus>  {
    private final WeakRef<DBEditableObjectVirtualFile> mainDatabaseFile;
    private final FileType fileType;

    private final VirtualFileStatusHolder status = new VirtualFileStatusHolder();

    protected DBContentType contentType;

    public DBContentVirtualFile(@NotNull DBEditableObjectVirtualFile mainDatabaseFile, DBContentType contentType) {
        super(mainDatabaseFile.getProject(), mainDatabaseFile.getObjectRef().getObjectName());
        this.mainDatabaseFile = WeakRef.of(mainDatabaseFile);
        this.contentType = contentType;

        DBObjectRef<DBSchemaObject> objectRef = mainDatabaseFile.getObjectRef();

        Project project = getProject();
        DDLFileManager ddlFileManager = DDLFileManager.getInstance(project);
        DDLFileType ddlFileType = ddlFileManager.getDDLFileType(objectRef.getObjectType(), contentType);
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
    @Nullable
    public SchemaId getSchemaId() {
        DBSchema schema = getObject().getSchema();
        return SchemaId.from(schema);
    }

    @NotNull
    public DBEditableObjectVirtualFile getMainDatabaseFile() {
        return mainDatabaseFile.ensure();
    }

    public DBContentType getContentType() {
        return contentType;
    }

    @Override
    public boolean isValid() {
        DBEditableObjectVirtualFile mainDatabaseFile = this.mainDatabaseFile.get();
        return super.isValid() && mainDatabaseFile != null && mainDatabaseFile.isValid();
    }

    @NotNull
    public DBSchemaObject getObject() {
        return getMainDatabaseFile().getObject();
    }


    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return getMainDatabaseFile().getConnectionId();
    }

    @NotNull
    @Override
    public ConnectionHandler getConnection() {
        return getMainDatabaseFile().getConnection();
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

    @Override
    @NotNull
    public FileType getFileType() {
        return fileType;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    @Nullable
    public VirtualFile getParent() {
        if (isValid()) {
            DBObject parentObject = getObject().getParentObject();
            if (parentObject != null) {
                return parentObject.getVirtualFile();
            }
        }
        return null;
    }

    @Override
    public Icon getIcon() {
        return getObject().getOriginalIcon();
    }

    @Override
    public VirtualFile[] getChildren() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public void refresh(boolean b, boolean b1, Runnable runnable) {

    }

    @NotNull
    @Override
    public InputStream getInputStream() throws IOException {
        return DevNullStreams.INPUT_STREAM;
    }

    @Override
    public long getModificationStamp() {
        return 1;
    }
}
