package com.dci.intellij.dbn.editor.data.filter;

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
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBParseableVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import com.intellij.util.LocalTimeCounter;

public class DatasetFilterVirtualFile extends DBVirtualFileImpl implements DBParseableVirtualFile, FileConnectionMappingProvider {
    private long modificationTimestamp = LocalTimeCounter.currentTime();
    private CharSequence content = "";
    private DBObjectRef<DBDataset> datasetRef;

    public DatasetFilterVirtualFile(DBDataset dataset, String content) {
        this.datasetRef = DBObjectRef.from(dataset);
        this.content = content;
        name = dataset.getName();
        ConnectionHandler connectionHandler = FailsafeUtil.get(getConnectionHandler());
        setCharset(connectionHandler.getSettings().getDetailSettings().getCharset());
        putUserData(PARSE_ROOT_ID_KEY, "subquery");
    }

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = FailsafeUtil.get(getConnectionHandler());
        DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);

        if (languageDialect != null) {
            DBLanguagePsiFile file = (DBLanguagePsiFile) languageDialect.getParserDefinition().createFile(fileViewProvider);
            fileViewProvider.forceCachedPsi(file);
            Document document = DocumentUtil.getDocument(fileViewProvider.getVirtualFile());
            PsiDocumentManagerImpl.cachePsi(document, file);
            return file;
        }
        return null;
    }

    @NotNull
    @Override
    protected Project getProject() {
        return getDataset().getProject();
    }

    public DBDataset getDataset() {
        return DBObjectRef.get(datasetRef);
    }

    public Icon getIcon() {
        return Icons.DBO_TABLE;
    }

    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return datasetRef.lookupConnectionHandler();
    }

    @Nullable
    @Override
    public ConnectionHandler getActiveConnection() {
        return getConnectionHandler();
    }

    @Nullable
    @Override
    public DBSchema getCurrentSchema() {
        DBDataset dataset = getDataset();
        return dataset == null ? null : dataset.getSchema();
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
    protected String createPath() {
        return DatabaseFileSystem.createPath(datasetRef) + " FILTER";
    }

    @NotNull
    @Override
    protected String createUrl() {
        return DatabaseFileSystem.createUrl(datasetRef) + "#FILTER";
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
                DatasetFilterVirtualFile.this.modificationTimestamp = modificationTimestamp;
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

}
