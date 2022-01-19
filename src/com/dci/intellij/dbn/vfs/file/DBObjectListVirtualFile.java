package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.Naming;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.DatabaseEntity;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectPsiFacade;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBObjectListVirtualFile<T extends DBObjectList> extends DBVirtualFileImpl {
    private static final byte[] EMPTY_BYTE_CONTENT = new byte[0];
    protected T objectList;

    public DBObjectListVirtualFile(T objectList) {
        super(objectList.getProject());
        this.objectList = objectList;
        this.name = Naming.capitalize(objectList.getName());
    }

    public T getObjectList() {
        return objectList;
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return objectList.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return objectList.getConnectionHandler();
    }

    @Nullable
    @Override
    public SchemaId getSchemaId() {
        DatabaseEntity parent = objectList.getParentEntity();
        if (parent instanceof DBObject) {
            DBObject object = (DBObject) parent;
            return SchemaId.from(object.getSchema());
        }
        return null;
    }

    @Nullable
    @Override
    public DatabaseSession getDatabaseSession() {
        return getConnectionHandler().getSessionBundle().getPoolSession();
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/

    @NotNull
    @Override
    public String getPresentableName() {
        return name;
    }

    @Override
    @NotNull
    public FileType getFileType() {
        return UnknownFileType.INSTANCE;
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    @Nullable
    public VirtualFile getParent() {
        if (Failsafe.check(objectList)) {
            DatabaseEntity parent = objectList.getParentEntity();
            if (parent instanceof DBObject) {
                DBObject parentObject = (DBObject) parent;
                return DBObjectPsiFacade.asPsiDirectory(parentObject).getVirtualFile();
            }

            if (parent instanceof DBObjectBundle) {
                DBObjectBundle objectBundle = (DBObjectBundle) parent;
                return objectBundle.getConnectionHandler().getPsiDirectory().getVirtualFile();
            }
        }

        return null;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && Failsafe.check(objectList);
    }

    @Override
    public Icon getIcon() {
        return null;
    }

    @Override
    public VirtualFile[] getChildren() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @Override
    @NotNull
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        return DevNullStreams.OUTPUT_STREAM;
    }

    @Override
    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        return EMPTY_BYTE_CONTENT;
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

    @Override
    public long getLength() {
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

    @Override
    public String getExtension() {
        return null;
    }
}

