package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.SourceCodeLoadListener;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.SourceCodeOffsets;
import com.dci.intellij.dbn.language.common.DBLanguage;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguageFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.sql.SQLException;
import java.sql.Timestamp;

public class SourceCodeFile extends DatabaseContentFile implements DatabaseFile, DocumentListener {
    private static final Key<VirtualFile> FILE_KEY = Key.create("FILE_KEY");

    private String originalContent;
    private String lastSavedContent;
    private String content;
    private Timestamp changeTimestamp;
    private String sourceLoadError;
    public int documentHashCode;
    private int hashCode;
    private SourceCodeOffsets offsets;

    public SourceCodeFile(final DatabaseEditableObjectFile databaseFile, DBContentType contentType) {
        super(databaseFile, contentType);
        DBSchemaObject object = getObject();
        if (object != null) {
            hashCode = (object.getQualifiedNameWithConnectionId() + contentType.getDescription()).hashCode();
            updateChangeTimestamp();
            setCharset(databaseFile.getConnectionHandler().getSettings().getDetailSettings().getCharset());
            try {
                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(getProject());
                SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
                content = sourceCodeContent.getSourceCode();
                offsets = sourceCodeContent.getOffsets();
                sourceLoadError = null;
            } catch (SQLException e) {
                content = "";
                sourceLoadError = e.getMessage();
                //MessageUtil.showErrorDialog("Could not load sourcecode for " + object.getQualifiedNameWithType() + " from database.", e);
            }
        } else {
            hashCode = super.hashCode();
            sourceLoadError = "Could not find object in database";
        }
    }

    public SourceCodeOffsets getOffsets() {
        return offsets;
    }

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, DBLanguage language) {
        ConnectionHandler connectionHandler = getConnectionHandler();

        DBSchemaObject underlyingObject = getObject();
        String parseRootId = getParseRootId();
        if (parseRootId != null) {
            DBLanguageDialect languageDialect = connectionHandler.getLanguageDialect(language);
            if (languageDialect != null) {
                DBLanguageFile file = (DBLanguageFile) languageDialect.getParserDefinition().createFile(fileViewProvider);
                file.setParseRootId(parseRootId);
                file.setUnderlyingObject(underlyingObject);
                fileViewProvider.forceCachedPsi(file);
                Document document = DocumentUtil.getDocument(fileViewProvider.getVirtualFile());
                document.putUserData(FILE_KEY, getDatabaseFile());
                PsiDocumentManagerImpl.cachePsi(document, file);
                return file;
            }
        }
        return null;
    }

    public String getParseRootId() {
        DBSchemaObject schemaObject = getObject();
        return schemaObject == null ? null : schemaObject.getCodeParseRootId(contentType);
    }

    public DBLanguageFile getPsiFile() {
        return (DBLanguageFile) PsiUtil.getPsiFile(getProject(), this);
    }

    public void updateChangeTimestamp() {
        DBSchemaObject object = getObject();
        if (object != null) {
            try {
                Timestamp timestamp = object.loadChangeTimestamp(getContentType());
                if (timestamp != null) {
                    changeTimestamp = timestamp;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Timestamp getChangeTimestamp() {
        return changeTimestamp;
    }

    public String getOriginalContent() {
        return originalContent;
    }

    public String getLastSavedContent() {
        return lastSavedContent == null ? originalContent : lastSavedContent;
    }

    public void setContent(String content) {
        if (originalContent == null) {
            originalContent = this.content;
        }
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public boolean reloadFromDatabase() {
        try {
            updateChangeTimestamp();
            originalContent = null;

            DBSchemaObject object = getObject();
            if (object != null) {
                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(getProject());
                SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
                content = sourceCodeContent.getSourceCode();
                offsets = sourceCodeContent.getOffsets();

                getDatabaseFile().updateDDLFiles(getContentType());
                setModified(false);
                sourceLoadError = null;
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            sourceLoadError = e.getMessage();
            DBSchemaObject object = databaseFile.getObject();
            if (object != null) {
                MessageUtil.showErrorDialog("Could not reload sourcecode for " + object.getQualifiedNameWithType() + " from database.", e);
            }
            return false;
        } finally {
            Project project = getProject();
            if (project != null && !project.isDisposed()) {
                EventManager.notify(project, SourceCodeLoadListener.TOPIC).sourceCodeLoaded(databaseFile);
            }
        }
    }

    public void updateToDatabase() throws SQLException {
        DBSchemaObject object = getObject();
        if (object != null) {
            object.executeUpdateDDL(getContentType(), getLastSavedContent(), content);
            updateChangeTimestamp();
            getDatabaseFile().updateDDLFiles(getContentType());
            setModified(false);
            lastSavedContent = content;
        }
    }

    @NotNull
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        return DevNullStreams.OUTPUT_STREAM;
    }

    @NotNull
    public byte[] contentsToByteArray() {
        return content.getBytes(getCharset());
    }

    public String createDDLStatement() {
        DBSchemaObject object = getObject();
        if (object != null) {
            String content = this.content.trim();
            if (content.length() > 0) {
                return object.createDDLStatement(content);
            }
        }
        return "";
    }

    public long getLength() {
        return content.length();
    }

    public int getDocumentHashCode() {
        return documentHashCode;
    }

    public void setDocumentHashCode(int documentHashCode) {
        this.documentHashCode = documentHashCode;
    }

    public String getSourceLoadError() {
        return sourceLoadError;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, T value) {
        if (key == FileDocumentManagerImpl.DOCUMENT_KEY && contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_BODY) ) {
            databaseFile.putUserData(FileDocumentManagerImpl.DOCUMENT_KEY, (Reference<Document>) value);
        }
        super.putUserData(key, value);
    }

    /**
     * ******************************************************
     * DocumentListener                    *
     * *******************************************************
     */
    public void beforeDocumentChange(DocumentEvent event) {

    }

    public void documentChanged(DocumentEvent event) {
        setModified(true);
    }

    public boolean equals(Object obj) {
        if (obj instanceof SourceCodeFile) {
            SourceCodeFile virtualFile = (SourceCodeFile) obj;
            return virtualFile.hashCode() == hashCode;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public void dispose() {
        originalContent = null;
        lastSavedContent = null;
        content = null;
        super.dispose();
    }

    public int getGuardedBlockEndOffset() {
        DBSchemaObject object = getObject();
        if (object != null) {
            String name = object.getName();
            int index = StringUtil.indexOfIgnoreCase(content, name, 0);
            if (index > -1) {
                return index + name.length();
            }
        }
        return 0;
    }
}
