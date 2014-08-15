package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.jetbrains.annotations.NotNull;

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
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;

public class SourceCodeVirtualFile extends DatabaseContentVirtualFile implements DatabaseFile, DocumentListener {

    private String originalContent;
    private String lastSavedContent;
    private String content;
    private Timestamp changeTimestamp;
    private String sourceLoadError;
    public int documentHashCode;
    private int hashCode;
    private SourceCodeOffsets offsets;

    public SourceCodeVirtualFile(final DatabaseEditableObjectVirtualFile databaseFile, DBContentType contentType) {
        super(databaseFile, contentType);
        DBSchemaObject object = getObject();
        if (object != null) {
            hashCode = (
                    object.getConnectionHandler().getId() + "#" +
                    object.getObjectType() + "#" +
                    object.getQualifiedName() + "#" +
                    object.getOverload() + "#" +
                    contentType).hashCode();

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

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        String parseRootId = getParseRootId();
        if (connectionHandler != null && parseRootId != null) {
            DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
            if (languageDialect != null) {
                DBSchemaObject underlyingObject = getObject();
                fileViewProvider.getVirtualFile().putUserData(PARSE_ROOT_ID_KEY, getParseRootId());

                DBLanguagePsiFile file = (DBLanguagePsiFile) languageDialect.getParserDefinition().createFile(fileViewProvider);
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

    public DBLanguagePsiFile getPsiFile() {
        return (DBLanguagePsiFile) PsiUtil.getPsiFile(getProject(), this);
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
        if (obj instanceof SourceCodeVirtualFile) {
            SourceCodeVirtualFile virtualFile = (SourceCodeVirtualFile) obj;
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
