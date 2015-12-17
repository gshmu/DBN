package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.content.BasicSourceCodeContent;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockMarkers;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockType;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.content.SourceCodeOffsets;
import com.dci.intellij.dbn.editor.code.content.TraceableSourceCodeContent;
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

    private SourceCodeContent originalContent = new BasicSourceCodeContent();
    private SourceCodeContent lastSavedContent = new BasicSourceCodeContent();
    private TraceableSourceCodeContent databaseContent = new TraceableSourceCodeContent();
    private TraceableSourceCodeContent content = new TraceableSourceCodeContent();

    private ChangeTimestamp codeMergeTimestamp;

    private String sourceLoadError;
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
            if (!isModified() && !StringUtil.equals(originalContent.getText(), newContent)) {
                setModified(true);
            }
            content.setText(newContent);
        }
    };

    public DocumentListener getDocumentListener() {
        return documentListener;
    }

    @NotNull
    public SourceCodeOffsets getOffsets() {
        return content.getOffsets();
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
        return content.isLoaded();
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

    public void updateContentState() {
        if (isLoaded()) {
            try {
                DBSchemaObject object = getObject();
                boolean checkSources = true;
                if (isChangeTracingSupported()) {
                    checkSources = false;
                    ChangeTimestamp latestTimestamp = object.loadChangeTimestamp(contentType);
                    if (databaseContent.isOlderThan(latestTimestamp)) {
                        checkSources = true;
                        codeMergeTimestamp = null;
                    }
                    databaseContent.setTimestamp(latestTimestamp);
                }

                if (checkSources) {
                    Project project = FailsafeUtil.get(getProject());
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    TraceableSourceCodeContent latestContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
                    ChangeTimestamp latestTimestamp = latestContent.getTimestamp();

                    CharSequence lastSavedContent = getLastSavedContent();

                    if (StringUtil.equals(latestContent.getText(), lastSavedContent)) {
                        content.setTimestamp(latestTimestamp);
                        databaseContent.reset();
                        databaseContent.setTimestamp(latestTimestamp);
                    } else {
                        databaseContent = latestContent;
                    }
                }

                if (databaseContent.isLoaded() && StringUtil.equals(databaseContent.getText(), content.getText())) {
                    databaseContent.reset();
                    content.setTimestamp(databaseContent.getTimestamp());
                    setModified(false);
                }

            } catch (SQLException e) {
                LOGGER.warn("Error updating source content state", e);
            }
        }
    }

    public boolean isChangedInDatabase(boolean reload) {
        if (isLoaded()) {
            if (reload || databaseContent.getTimestamp().isDirty()) {
                updateContentState();
            }
            return content.isOlderThan(databaseContent);
        }
        return false;
    }

    public boolean isMergeRequired() {
        if (isChangedInDatabase(false)) {
            return codeMergeTimestamp == null || codeMergeTimestamp.isBefore(databaseContent.getTimestamp());
        }
        return false;
    }

    public void refreshMergeTimestamp() {
        codeMergeTimestamp = databaseContent.getTimestamp();
    }

    public Timestamp getDatabaseChangeTimestamp() {
        return databaseContent.getTimestamp().value();
    }

    @NotNull
    public CharSequence getOriginalContent() {
        return originalContent.getText();
    }

    @NotNull
    public CharSequence getLastSavedContent() {
        return lastSavedContent.isLoaded() ? lastSavedContent.getText() : originalContent.getText();
    }

    @NotNull
    public CharSequence getContent() {
        return content.getText();
    }

    public CharSequence getDatabaseContent() {
        return databaseContent.getText();
    }

    public void applyContent(TraceableSourceCodeContent sourceCodeContent) {
        content = sourceCodeContent;
        originalContent.setText(content.getText().toString());
        lastSavedContent.reset();

        databaseContent.reset();
        databaseContent.setTimestamp(sourceCodeContent.getTimestamp());
        codeMergeTimestamp = null;

        Document document = DocumentUtil.getDocument(this);
        if (document != null) {
            DocumentUtil.setText(document, content.getText());
            SourceCodeOffsets offsets = content.getOffsets();
            GuardedBlockMarkers guardedBlocks = offsets.getGuardedBlocks();
            if (!guardedBlocks.isEmpty()) {
                DocumentUtil.removeGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION);
                DocumentUtil.createGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION, guardedBlocks, null);
            }
        }
        setModified(false);
        sourceLoadError = null;
    }

    public void saveSourceToDatabase() throws SQLException {
        DBSchemaObject object = getObject();
        String oldContent = getLastSavedContent().toString();
        String newContent = getContent().toString();
        object.executeUpdateDDL(getContentType(), oldContent, newContent);
        setModified(false);
        lastSavedContent.setText(newContent);

        databaseContent.reset();
        codeMergeTimestamp = null;
        if (isChangeTracingSupported()) {
            DBContentType contentType = getContentType();
            ChangeTimestamp timestamp = object.loadChangeTimestamp(contentType);
            content.setTimestamp(timestamp);
            databaseContent.setTimestamp(timestamp);
        }
    }

    boolean isChangeTracingSupported() {
        return DatabaseFeature.OBJECT_CHANGE_TRACING.isSupported(getObject());
    }

    @NotNull
    public OutputStream getOutputStream(Object o, long l, long l1) throws IOException {
        return DevNullStreams.OUTPUT_STREAM;
    }

    @NotNull
    public byte[] contentsToByteArray() {
        return content.getText().toString().getBytes(getCharset());
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
        originalContent = new TraceableSourceCodeContent();
        lastSavedContent = new TraceableSourceCodeContent();
        content = new TraceableSourceCodeContent();
    }
}
