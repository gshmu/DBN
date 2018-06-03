package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBConnectionVirtualFile extends DBVirtualFileImpl {
    private static final byte[] EMPTY_CONTENT = new byte[0];
    private ConnectionHandlerRef connectionHandlerRef;

    public DBConnectionVirtualFile(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        this.connectionHandlerRef = connectionHandler.getRef();
        this.name = connectionHandler.getName();
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @Override
    public DBSchema getDatabaseSchema() {
        return null;
    }

    @Nullable
    @Override
    public DatabaseSession getDatabaseSession() {
        return null;
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/

    @Override
    public boolean isValid() {
        return super.isValid() && connectionHandlerRef.isValid();
    }

    @Override
    public String getPresentableName() {
        return getName();
    }

    @NotNull
    public FileType getFileType() {
        return SQLFileType.INSTANCE;
    }

    public boolean isWritable() {
        return false;
    }

    public boolean isDirectory() {
        return true;
    }

    @Nullable
    public VirtualFile getParent() {
        return null;
    }

    public Icon getIcon() {
        return getConnectionHandler().getIcon();
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
        return EMPTY_CONTENT;
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

