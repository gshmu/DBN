package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.navigation.psi.NavigationPsiCache;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ide.navigationToolbar.NavBarPresentation;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.UnknownFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBObjectVirtualFile<T extends DBObject> extends VirtualFile implements DBVirtualFile {
    private static final byte[] EMPTY_BYTE_CONTENT = new byte[0];
    protected DBObjectRef<T> objectRef;

    private String path;
    private String url;

    public DBObjectVirtualFile(T object) {
        this.objectRef = object.getRef();
    }

    public DBObjectRef<T> getObjectRef() {
        return objectRef;
    }

    @Nullable
    public T getObject() {
        return objectRef.get();
    }

    public ConnectionHandler getConnectionHandler() {
        return objectRef.lookupConnectionHandler();
    }

    public boolean equals(Object obj) {
        if (obj instanceof DBObjectVirtualFile) {
            DBObjectVirtualFile objectFile = (DBObjectVirtualFile) obj;
            return objectFile.objectRef.equals(objectRef);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return objectRef.hashCode();
    }

    public Project getProject() {
        T object = DBObjectRef.get(objectRef);
        return object == null ? null : object.getProject();
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/
    @NotNull
    @NonNls
    public String getName() {
        return objectRef.getFileName();
    }

    @NotNull
    public FileType getFileType() {
        return UnknownFileType.INSTANCE;
    }

    @NotNull
    public DatabaseFileSystem getFileSystem() {
        return DatabaseFileSystem.getInstance();
    }

    @NotNull
    public String getPath() {
        if (path == null) {
            path = DatabaseFileSystem.createPath(getObject());
        }
        return path;
    }

    @NotNull
    public String getUrl() {
        if (url == null) {
            url = DatabaseFileSystem.createUrl(getObject());
        }
        return url;
    }

    public boolean isWritable() {
        return false;
    }

    public boolean isDirectory() {
        return true;
    }

    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isInLocalFileSystem() {
        return false;
    }

    @Nullable
    public VirtualFile getParent() {
        if (CommonUtil.isCalledThrough(NavBarPresentation.class)) {
            T object = getObject();
            if (object != null) {
                BrowserTreeNode treeParent = object.getTreeParent();
                if (treeParent instanceof DBObjectList<?>) {
                    DBObjectList objectList = (DBObjectList) treeParent;
                    return NavigationPsiCache.getPsiDirectory(objectList).getVirtualFile();
                }
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

    public void release() {
    }
}

