package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBSchema;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class DBSessionBrowserVirtualFile extends DBVirtualFileImpl implements Comparable<DBSessionBrowserVirtualFile> {
    private long modificationTimestamp = LocalTimeCounter.currentTime();
    private CharSequence content = "";
    private ConnectionHandlerRef connectionHandlerRef;

    public DBSessionBrowserVirtualFile(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        this.connectionHandlerRef = connectionHandler.getRef();
        this.name = connectionHandler.getName() + " Sessions";
        setCharset(connectionHandler.getSettings().getDetailSettings().getCharset());
    }

    public Icon getIcon() {
        return Icons.FILE_SESSION_BROWSER;
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
        return getConnectionHandler().getSessionBundle().getPoolSession();
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && connectionHandlerRef.isValid();
    }    

    @NotNull
    @Override
    protected String createPath() {
        return DatabaseFileSystem.createPath(getConnectionHandler()) + "sessions" + File.separatorChar + name;

    }

    @NotNull
    @Override
    protected String createUrl() {
        return DatabaseFileSystem.createUrl(getConnectionHandler()) + "/session_browser#" + name;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public boolean isDefault() {return name.equals(getConnectionHandler().getName());}

    @Override
    public VirtualFile getParent() {
        ConnectionHandler connectionHandler = getConnectionHandler();
        return connectionHandler.getPsiDirectory().getVirtualFile();
    }

    @Override
    public VirtualFile[] getChildren() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return SQLFileType.INSTANCE;
    }

    @NotNull
    public OutputStream getOutputStream(Object requestor, final long modificationTimestamp, long newTimeStamp) throws IOException {
        return new ByteArrayOutputStream() {
            public void close() {
                DBSessionBrowserVirtualFile.this.modificationTimestamp = modificationTimestamp;
                content = toString();
            }
        };
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        Charset charset = getCharset();
        return content.toString().getBytes(charset.name());
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

  public long getModificationStamp() {
    return modificationTimestamp;
  }

    @Override
    public long getLength() {
        return 0;
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(contentsToByteArray());
    }

    @Override
    public String getExtension() {
        return "sql";
    }

    @Override
    public int compareTo(@NotNull DBSessionBrowserVirtualFile o) {
        return name.compareTo(o.name);
    }

}
