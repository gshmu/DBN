package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Objects;

public class DBSessionBrowserVirtualFile extends DBVirtualFileImpl implements Comparable<DBSessionBrowserVirtualFile> {
    private final ConnectionHandlerRef connectionHandlerRef;
    private long modificationTimestamp = LocalTimeCounter.currentTime();
    private CharSequence content = "";

    public DBSessionBrowserVirtualFile(ConnectionHandler connectionHandler) {
        super(connectionHandler.getProject());
        this.connectionHandlerRef = connectionHandler.getRef();
        this.name = connectionHandler.getName() + " Sessions";
        setCharset(connectionHandler.getSettings().getDetailSettings().getCharset());
    }

    @Override
    public Icon getIcon() {
        return Icons.FILE_SESSION_BROWSER;
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return connectionHandlerRef.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.ensure();
    }

    @Nullable
    @Override
    public SchemaId getSchemaId() {
        return null;
    }

    @Nullable
    @Override
    public DatabaseSession getDatabaseSession() {
        return getConnectionHandler().getSessionBundle().getPoolSession();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && connectionHandlerRef.isValid();
    }    

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public boolean isDefault() {return Objects.equals(name, getConnectionHandler().getName());}

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

    @Override
    @NotNull
    public OutputStream getOutputStream(Object requestor, final long modificationTimestamp, long newTimeStamp) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                DBSessionBrowserVirtualFile.this.modificationTimestamp = modificationTimestamp;
                content = toString();
            }
        };
    }

    @Override
    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        Charset charset = getCharset();
        return content.toString().getBytes(charset.name());
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

  @Override
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

    @NotNull
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
