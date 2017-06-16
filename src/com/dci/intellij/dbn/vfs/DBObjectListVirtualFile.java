package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.util.NamingUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.GenericDatabaseElement;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.vfs.VirtualFile;

public class DBObjectListVirtualFile<T extends DBObjectList> extends DBVirtualFileImpl {
    private static final byte[] EMPTY_BYTE_CONTENT = new byte[0];
    protected T objectList;

    public DBObjectListVirtualFile(T objectList) {
        super(objectList.getProject());
        this.objectList = objectList;
        this.name = NamingUtil.capitalize(objectList.getName());
    }

    public T getObjectList() {
        return objectList;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return objectList.getConnectionHandler();
    }

    @Override
    public DBSchema getCurrentSchema() {
        GenericDatabaseElement parent = objectList.getParentElement();
        if (parent instanceof DBObject) {
            DBObject object = (DBObject) parent;
            return object.getSchema();
        }
        return null;
    }

    @NotNull
    @Override
    public String getConnectionId() {
        return getConnectionHandler().getId();
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/
    @NotNull
    @NonNls
    public String getName() {
        return name;
    }

    @Override
    public String getPresentableName() {
        return name;
    }

    @NotNull
    public FileType getFileType() {
        return UnknownFileType.INSTANCE;
    }

    @NotNull
    @Override
    protected String createPath() {
        return DatabaseFileSystem.createPath(objectList);
    }

    @NotNull
    @Override
    protected String createUrl() {
        return DatabaseFileSystem.createUrl(objectList);
    }

    public boolean isWritable() {
        return false;
    }

    public boolean isDirectory() {
        return true;
    }

    @Nullable
    public VirtualFile getParent() {
        GenericDatabaseElement parent = objectList.getParentElement();
        if (parent instanceof DBObject) {
            DBObject parentObject = (DBObject) parent;
            return NavigationPsiCache.getPsiDirectory(parentObject).getVirtualFile();
        }

        if (parent instanceof DBObjectBundle) {
            DBObjectBundle objectBundle = (DBObjectBundle) parent;
            return NavigationPsiCache.getPsiDirectory(objectBundle.getConnectionHandler()).getVirtualFile();

        }

        return null;
    }

    public Icon getIcon() {
        return null;
    }

    public VirtualFile[] getChildren() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @NotNull
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        return DevNullStreams.OUTPUT_STREAM;
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        return EMPTY_BYTE_CONTENT;
    }

    public long getTimeStamp() {
        return 0;
    }

    public long getLength() {
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
    public String getExtension() {
        return null;
    }

    @Override
    public void dispose() {
        super.dispose();
        objectList = null;
    }

}

