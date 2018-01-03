package com.dci.intellij.dbn.editor.session;

import javax.swing.Icon;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBParseableVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.LocalTimeCounter;

public class SessionBrowserStatementVirtualFile extends DBVirtualFileImpl implements DBParseableVirtualFile {
    private long modificationTimestamp = LocalTimeCounter.currentTime();
    private CharSequence content = "";
    private SessionBrowser sessionBrowser;
    private DBObjectRef<DBSchema> schemaRef;


    public SessionBrowserStatementVirtualFile(SessionBrowser sessionBrowser, String content) {
        super(sessionBrowser.getProject());
        this.sessionBrowser = sessionBrowser;
        this.content = content;
        ConnectionHandler connectionHandler = FailsafeUtil.get(sessionBrowser.getConnectionHandler());
        name = connectionHandler.getName();
        setCharset(connectionHandler.getSettings().getDetailSettings().getCharset());
        //putUserData(PARSE_ROOT_ID_KEY, "subquery");
    }

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = FailsafeUtil.get(getConnectionHandler());
        DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
        return languageDialect == null ? null : fileViewProvider.initializePsiFile(languageDialect);
    }

    public SessionBrowser getSessionBrowser() {
        return sessionBrowser;
    }

    @NotNull
    @Override
    protected String createPath() {
        return DatabaseFileSystem.createPath(getConnectionHandler()) + " SESSION_BROWSER_STATEMENT";

    }

    @NotNull
    @Override
    protected String createUrl() {
        return DatabaseFileSystem.createUrl(getConnectionHandler()) + "#SESSION_BROWSER_STATEMENT";
    }

    public Icon getIcon() {
        return Icons.FILE_SQL;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        ConnectionHandler connectionHandler = sessionBrowser == null ? null : sessionBrowser.getConnectionHandler();
        return FailsafeUtil.get(connectionHandler);
    }

    @Nullable
    @Override
    public DBSchema getDatabaseSchema() {
        return DBObjectRef.get(schemaRef);
    }

    @Nullable
    @Override
    public DatabaseSession getDatabaseSession() {
        return getConnectionHandler().getSessionBundle().getPoolSession();
    }

    public void setDatabaseSchema(DBSchema schema) {
        this.schemaRef = DBObjectRef.from(schema);
    }

    @NotNull
    @Override
    public String getName() {
        return name;
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

    @NotNull
    public OutputStream getOutputStream(Object requestor, final long modificationTimestamp, long newTimeStamp) throws IOException {
        return new ByteArrayOutputStream() {
            public void close() {
                SessionBrowserStatementVirtualFile.this.modificationTimestamp = modificationTimestamp;
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
        try {
            return contentsToByteArray().length;
        } catch (IOException e) {
            return 0;
        }
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
    public void dispose() {
        super.dispose();
        sessionBrowser = null;
    }
}
