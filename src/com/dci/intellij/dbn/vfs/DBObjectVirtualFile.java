package com.dci.intellij.dbn.vfs;

import javax.swing.Icon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ide.navigationToolbar.NavBarPresentation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.vfs.VirtualFile;

public class DBObjectVirtualFile<T extends DBObject> extends DBVirtualFileImpl {
    private static final byte[] EMPTY_BYTE_CONTENT = new byte[0];
    protected DBObjectRef<T> objectRef;

    public DBObjectVirtualFile(T object) {
        super(object.getProject());
        this.objectRef = DBObjectRef.from(object);
        this.name = objectRef.getFileName();
    }

    public DBObjectRef<T> getObjectRef() {
        return objectRef;
    }

    @NotNull
    public T getObject() {
        return FailsafeUtil.get(objectRef.get());
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return getObject().getConnectionHandler();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && objectRef.get() != null;
    }    

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/
    @NotNull
    public FileType getFileType() {
        return UnknownFileType.INSTANCE;
    }

    @NotNull
    public DatabaseFileSystem getFileSystem() {
        return DatabaseFileSystem.getInstance();
    }

    @NotNull
    @Override
    protected String createPath() {
        return DatabaseFileSystem.createPath(objectRef);
    }

    @NotNull
    @Override
    protected String createUrl() {
        return DatabaseFileSystem.createUrl(objectRef);
    }

    public boolean isWritable() {
        return false;
    }

    public boolean isDirectory() {
        return true;
    }

    @Nullable
    public VirtualFile getParent() {
        if (CommonUtil.isCalledThrough(NavBarPresentation.class)) {
            T object = getObject();
            BrowserTreeNode treeParent = object.getTreeParent();
            if (treeParent instanceof DBObjectList<?>) {
                DBObjectList objectList = (DBObjectList) treeParent;
                return NavigationPsiCache.getPsiDirectory(objectList).getVirtualFile();
            }
        }
        return null;
    }

    public Icon getIcon() {
        return objectRef.getObjectType().getIcon();
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
}

