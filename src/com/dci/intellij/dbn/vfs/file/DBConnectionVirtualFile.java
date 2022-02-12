package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.sql.SQLFileType;
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
    private final ConnectionHandlerRef connectionHandler;

    public DBConnectionVirtualFile(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        this.connectionHandler = connectionHandler.getRef();
        this.name = connectionHandler.getName();
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return connectionHandler.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection() {
        return connectionHandler.ensure();
    }

    @Nullable
    @Override
    public SchemaId getSchemaId() {
        return null;
    }

    @Nullable
    @Override
    public DatabaseSession getSession() {
        return null;
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/

    @Override
    public boolean isValid() {
        return super.isValid() && connectionHandler.isValid();
    }

    @NotNull
    @Override
    public String getPresentableName() {
        return getName();
    }

    @Override
    @NotNull
    public FileType getFileType() {
        return SQLFileType.INSTANCE;
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
        return null;
    }

    @Override
    public Icon getIcon() {
        return getConnection().getIcon();
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
        return EMPTY_CONTENT;
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

