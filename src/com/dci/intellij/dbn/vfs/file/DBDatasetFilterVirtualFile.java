package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBParseableVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class DBDatasetFilterVirtualFile extends DBVirtualFileImpl implements DBParseableVirtualFile {
    private final DBObjectRef<DBDataset> datasetRef;
    private long modificationTimestamp = LocalTimeCounter.currentTime();
    private CharSequence content = "";

    public DBDatasetFilterVirtualFile(DBDataset dataset, String content) {
        super(dataset.getProject());
        this.datasetRef = DBObjectRef.of(dataset);
        this.content = content;
        name = dataset.getName();
        ConnectionHandler connectionHandler = Failsafe.nn(getConnectionHandler());
        setCharset(connectionHandler.getSettings().getDetailSettings().getCharset());
        putUserData(PARSE_ROOT_ID_KEY, "subquery");
    }

    @Override
    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = Failsafe.nn(getConnectionHandler());
        DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
        return languageDialect == null ? null : fileViewProvider.initializePsiFile(languageDialect);
    }

    public DBDataset getDataset() {
        return DBObjectRef.get(datasetRef);
    }

    @Override
    public Icon getIcon() {
        return Icons.DBO_TABLE;
    }


    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return datasetRef.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return Failsafe.nn(datasetRef.resolveConnectionHandler());
    }

    @Nullable
    @Override
    public SchemaId getSchemaId() {
        DBDataset dataset = getDataset();
        return dataset == null ? null : dataset.getSchemaIdentifier();
    }

    @Nullable
    @Override
    public DatabaseSession getDatabaseSession() {
        return getConnectionHandler().getSessionBundle().getMainSession();
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
    public VirtualFile getParent() {
        return null;
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
                DBDatasetFilterVirtualFile.this.modificationTimestamp = modificationTimestamp;
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
        try {
            return contentsToByteArray().length;
        } catch (IOException e) {
            e.printStackTrace();
            assert false;
            return 0;
        }
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

}
