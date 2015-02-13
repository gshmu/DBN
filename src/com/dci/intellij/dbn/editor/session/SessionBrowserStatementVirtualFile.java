package com.dci.intellij.dbn.editor.session;

import javax.swing.Icon;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.vfs.DBParseableVirtualFile;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.util.LocalTimeCounter;

public class SessionBrowserStatementVirtualFile extends VirtualFile implements DBParseableVirtualFile {
    private long modificationTimestamp = LocalTimeCounter.currentTime();
    private CharSequence content = "";
    protected String name;
    protected String path;
    protected String url;
    ConnectionHandlerRef connectionHandlerRef;


    public SessionBrowserStatementVirtualFile(ConnectionHandler connectionHandler, String content) {
        this.connectionHandlerRef = connectionHandler.getRef();
        this.content = content;
        name = connectionHandler.getName();
        path = DatabaseFileSystem.createPath(connectionHandler) + " SESSION_BROWSER_STATEMENT";
        url = DatabaseFileSystem.createUrl(connectionHandler) + "#SESSION_BROWSER_STATEMENT";
        setCharset(getConnectionHandler().getSettings().getDetailSettings().getCharset());
        //putUserData(PARSE_ROOT_ID_KEY, "subquery");
    }

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);

            if (languageDialect != null) {
                DBLanguagePsiFile file = (DBLanguagePsiFile) languageDialect.getParserDefinition().createFile(fileViewProvider);
                fileViewProvider.forceCachedPsi(file);
                Document document = DocumentUtil.getDocument(fileViewProvider.getVirtualFile());
                document.putUserData(FILE_KEY, this);
                PsiDocumentManagerImpl.cachePsi(document, file);
                return file;
            }
        }
        return null;
    }

    public Icon getIcon() {
        return Icons.FILE_SQL;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.get();
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    @NotNull
    @Override
    public VirtualFileSystem getFileSystem() {
        return DatabaseFileSystem.getInstance();
    }

    @NotNull
    @Override
    public String getPath() {
        return path;
    }

    @NotNull
    @Override
    public String getUrl() {
        return url;
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
    public boolean isValid() {
        return true;
    }

    @Override
    public boolean isInLocalFileSystem() {
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
            e.printStackTrace();
            assert false;
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

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
    }
}
