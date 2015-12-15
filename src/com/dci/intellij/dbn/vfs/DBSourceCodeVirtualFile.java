package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.LoggerFactory;
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
import com.dci.intellij.dbn.editor.code.SourceCodeOffsets;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;

public class DBSourceCodeVirtualFile extends DBContentVirtualFile implements DBParseableVirtualFile, ConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.createLogger();
    private static final String EMPTY_CONTENT = "";

    private CharSequence originalContent = EMPTY_CONTENT;
    private CharSequence lastSavedContent = EMPTY_CONTENT;
    private CharSequence content = EMPTY_CONTENT;
    private ChangeTimestamp changeTimestamp;
    private ChangeTimestamp changeTimestampCheck;
    private String sourceLoadError;
    private SourceCodeOffsets offsets;
    private boolean loading;
    private boolean saving;

    public DBSourceCodeVirtualFile(final DBEditableObjectVirtualFile databaseFile, DBContentType contentType) {
        super(databaseFile, contentType);
        setCharset(databaseFile.getConnectionHandler().getSettings().getDetailSettings().getCharset());
    }

    private DocumentListener documentListener = new DocumentAdapter() {
        @Override
        public void documentChanged(DocumentEvent e) {
            CharSequence newContent = e.getDocument().getCharsSequence();
            if (!StringUtil.equals(newContent, content)){
                setModified(true);
                if (originalContent == EMPTY_CONTENT) {
                    originalContent = content;
                }
                content = newContent.toString();
            }
        }
    };

    public DocumentListener getDocumentListener() {
        return documentListener;
    }

    @Nullable
    public SourceCodeOffsets getOffsets() {
        return offsets;
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

    public synchronized boolean isLoaded() {
        return content != EMPTY_CONTENT;
    }

    public synchronized boolean isLoading() {
        return loading;
    }

    public synchronized void setLoading(boolean loading) {
        this.loading = loading;
    }

    public synchronized boolean isSaving() {
        return saving;
    }

    public synchronized void setSaving(boolean saving) {
        this.saving = saving;
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
            DBContentType contentType = getContentType();
            ChangeTimestamp timestamp = object.loadChangeTimestamp(contentType);
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
                    DBContentType contentType = getContentType();
                    changeTimestampCheck = object.loadChangeTimestamp(contentType);
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

    @NotNull
    public CharSequence getContent() {
        return content;
    }

    public void applyContent(SourceCodeContent sourceCodeContent) {
        originalContent = EMPTY_CONTENT;
        content = sourceCodeContent.getText();
        offsets = sourceCodeContent.getOffsets();
        Document document = DocumentUtil.getDocument(this);
        if (document != null) {
            DocumentUtil.setText(document, content);
            if (offsets != null) {
                GuardedBlockMarkers guardedBlocks = offsets.getGuardedBlocks();
                DocumentUtil.createGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION, guardedBlocks, null);
            }
        }
        setModified(false);
        sourceLoadError = null;
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

    public void setSourceLoadError(String sourceLoadError) {
        this.sourceLoadError = sourceLoadError;
    }

    @Override
    public <T> void putUserData(@NotNull Key<T> key, T value) {
        if (key == FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY && contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_BODY) ) {
            mainDatabaseFile.putUserData(FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY, (Document) value);
        }
        super.putUserData(key, value);
    }

    @Override
    public void dispose() {
        super.dispose();
        originalContent = EMPTY_CONTENT;
        lastSavedContent = EMPTY_CONTENT;
        content = EMPTY_CONTENT;
    }
}
