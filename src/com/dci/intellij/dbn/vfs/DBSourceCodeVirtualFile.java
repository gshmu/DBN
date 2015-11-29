package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.util.ChangeTimestamp;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.GuardedBlockMarkers;
import com.dci.intellij.dbn.editor.code.GuardedBlockType;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.SourceCodeOffsets;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBSourceCodeVirtualFile extends DBContentVirtualFile implements DBParseableVirtualFile, DocumentListener, ConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.createLogger();
    private static final String EMPTY_CONTENT = "";

    private CharSequence originalContent = EMPTY_CONTENT;
    private CharSequence lastSavedContent = EMPTY_CONTENT;
    private CharSequence content = EMPTY_CONTENT;
    private ChangeTimestamp changeTimestamp;
    private ChangeTimestamp changeTimestampCheck;
    private String sourceLoadError;
    private SourceCodeOffsets offsets;

    public DBSourceCodeVirtualFile(final DBEditableObjectVirtualFile databaseFile, DBContentType contentType) {
        super(databaseFile, contentType);
        setCharset(databaseFile.getConnectionHandler().getSettings().getDetailSettings().getCharset());
    }

    @Nullable
    public SourceCodeOffsets getOffsets() {
        return offsets;
    }

    public void applyContentToDocument(Document document) {
        DocumentUtil.setText(document, content);
    }

    public void applyGuardedBlocksToDocument(Document document) {
        if (offsets != null) {
            GuardedBlockMarkers guardedBlocks = offsets.getGuardedBlocks();
            DocumentUtil.createGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION, guardedBlocks, null);
        }
    }

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        String parseRootId = getParseRootId();
        if (parseRootId != null) {
            DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
            if (languageDialect != null) {
                DBSchemaObject underlyingObject = getObject();
                fileViewProvider.getVirtualFile().putUserData(PARSE_ROOT_ID_KEY, getParseRootId());

                DBLanguagePsiFile file = (DBLanguagePsiFile) languageDialect.getParserDefinition().createFile(fileViewProvider);
                file.setUnderlyingObject(underlyingObject);
                fileViewProvider.forceCachedPsi(file);
                Document document = DocumentUtil.getDocument(fileViewProvider.getVirtualFile());
                if (document != null) {
                    PsiDocumentManagerImpl.cachePsi(document, file);
                }
                return file;
            }
        }
        return null;
    }

    public boolean isLoaded() {
        return content != EMPTY_CONTENT;
    }

    public String getParseRootId() {
        return getObject().getCodeParseRootId(contentType);
    }

    @Nullable
    public DBLanguagePsiFile getPsiFile() {
        Project project = getProject();
        if (project != null) {
            return (DBLanguagePsiFile) PsiUtil.getPsiFile(project, this);
        }
        return null;
    }

    public void updateChangeTimestamp() {
        DBSchemaObject object = getObject();
        try {
            ChangeTimestamp timestamp = object.loadChangeTimestamp(getContentType());
            if (timestamp != null) {
                changeTimestamp = timestamp;
                changeTimestampCheck = null;
            }
        } catch (Exception e) {
            LOGGER.warn("Error loading object timestamp", e);
        }
    }

    public boolean isChangedInDatabase(boolean reload) {
        DBSchemaObject object = getObject();
        if (DatabaseFeature.OBJECT_CHANGE_TRACING.isSupported(object)) {
            try {
                if (changeTimestampCheck == null || changeTimestampCheck.isDirty() || reload) {
                    changeTimestampCheck = object.loadChangeTimestamp(getContentType());
                }

                return changeTimestamp != null && changeTimestampCheck != null && changeTimestamp.before(changeTimestampCheck);
            } catch (Exception e) {
                LOGGER.warn("Error loading object timestamp", e);
            }

        }
        return false;
    }

    public Timestamp getChangedInDatabaseTimestamp() {
        return changeTimestampCheck == null ? new Timestamp(System.currentTimeMillis() - 100) : changeTimestampCheck.value();
    }

    @NotNull
    public CharSequence getOriginalContent() {
        return originalContent;
    }

    @NotNull
    public CharSequence getLastSavedContent() {
        return lastSavedContent == EMPTY_CONTENT ? originalContent : lastSavedContent;
    }

    public void setContent(String content) {
        if (originalContent == EMPTY_CONTENT) {
            originalContent = this.content;
        }
        this.content = content;
    }

    @NotNull
    public CharSequence getContent() {
        return content;
    }

    public void loadSourceFromDatabase() throws SQLException {
        try {
            Project project = FailsafeUtil.get(getProject());
            originalContent = EMPTY_CONTENT;

            DBSchemaObject object = getObject();
            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
            SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
            content = sourceCodeContent.getText();
            offsets = sourceCodeContent.getOffsets();

            setModified(false);
            sourceLoadError = null;
        } catch (SQLException e) {
            sourceLoadError = e.getMessage();
            throw e;
        }
    }

    public void saveSourceToDatabase() throws SQLException {
        DBSchemaObject object = getObject();
        CharSequence lastSavedContent = getLastSavedContent();
        object.executeUpdateDDL(getContentType(), lastSavedContent.toString(), content.toString());
        setModified(false);
        this.lastSavedContent = content;
    }

    @NotNull
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        return DevNullStreams.OUTPUT_STREAM;
    }

    @NotNull
    public byte[] contentsToByteArray() {
        return content.toString().getBytes(getCharset());
    }

    public long getLength() {
        return content.length();
    }

    public String getSourceLoadError() {
        return sourceLoadError;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, T value) {
        if (key == FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY && contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_BODY) ) {
            mainDatabaseFile.putUserData(FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY, (Document) value);
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
        CharSequence newContent = event.getDocument().getCharsSequence();
        if (!StringUtil.equals(newContent, content)){
            setModified(true);
            setContent(newContent.toString());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        originalContent = EMPTY_CONTENT;
        lastSavedContent = EMPTY_CONTENT;
        content = EMPTY_CONTENT;
    }
}
