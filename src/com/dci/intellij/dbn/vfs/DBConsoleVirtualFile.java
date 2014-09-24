package com.dci.intellij.dbn.vfs;

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
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.util.LocalTimeCounter;

public class DBConsoleVirtualFile extends VirtualFile implements DBParseableVirtualFile, Comparable<DBConsoleVirtualFile> {
    private long modificationTimestamp = LocalTimeCounter.currentTime();
    private CharSequence content = "";
    private ConnectionHandler connectionHandler;
    private DBObjectRef<DBSchema> currentSchemaRef;
    protected String name;
    protected String path;
    protected String url;


    public DBConsoleVirtualFile(ConnectionHandler connectionHandler, String name) {
        this.connectionHandler = connectionHandler;
        this.currentSchemaRef = DBObjectRef.from(connectionHandler.getUserSchema());
        setName(name);
        setCharset(connectionHandler.getSettings().getDetailSettings().getCharset());
    }

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        if (connectionHandler != null) {
            DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
            if (languageDialect != null) {
                DBLanguagePsiFile file = (DBLanguagePsiFile) languageDialect.getParserDefinition().createFile(fileViewProvider);
                fileViewProvider.forceCachedPsi(file);
                Document document = DocumentUtil.getDocument(fileViewProvider.getVirtualFile());
                PsiDocumentManagerImpl.cachePsi(document, file);
                return file;
            }
        }
        return null;
    }

    public void setName(String name) {
        this.name = name;
        path = DatabaseFileSystem.createPath(connectionHandler) + " CONSOLE - " + name;
        url = DatabaseFileSystem.createUrl(connectionHandler) + "/console#" + name;
    }

    public Icon getIcon() {
        return Icons.FILE_SQL_CONSOLE;
    }

    public ConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    @Override
    public void dispose() {
        connectionHandler = null;
        currentSchemaRef = null;
    }

    public Project getProject() {
        return connectionHandler == null ? null : connectionHandler.getProject();
    }

    public void setCurrentSchema(DBSchema currentSchema) {
        this.currentSchemaRef = DBObjectRef.from(currentSchema);
    }

    public void setCurrentSchemaName(String currentSchemaName) {
        this.currentSchemaRef = new DBObjectRef<DBSchema>(connectionHandler.getId(), DBObjectType.SCHEMA, currentSchemaName);
    }

    public DBSchema getCurrentSchema() {
        return DBObjectRef.get(currentSchemaRef);
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

    public boolean isDefault() {return name.equals(connectionHandler.getName());}

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
                DBConsoleVirtualFile.this.modificationTimestamp = modificationTimestamp;
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

    @Override
    public int compareTo(DBConsoleVirtualFile o) {
        return getName().compareTo(o.getName());
    }
}
