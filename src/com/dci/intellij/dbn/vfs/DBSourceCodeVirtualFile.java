package com.dci.intellij.dbn.vfs;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.thread.SynchronizedTask;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.ChangeTimestamp;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockMarkers;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockType;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.content.SourceCodeOffsets;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.sql.Timestamp;

public class DBSourceCodeVirtualFile extends DBContentVirtualFile implements DBParseableVirtualFile, ConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.createLogger();

    private SourceCodeContent originalContent = new SourceCodeContent();
    private SourceCodeContent localContent = new SourceCodeContent();
    private SourceCodeContent databaseContent = null;

    private ChangeTimestamp databaseTimestamp = new ChangeTimestamp();

    private String sourceLoadError;
    private boolean loading;
    private boolean saving;
    private boolean refreshing;

    private Status status = Status.OK;

    private enum Status {
        OK,
        MERGED,
        OUTDATED
    }

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
            localContent.setText(newContent);
        }
    };

    public DocumentListener getDocumentListener() {
        return documentListener;
    }

    @NotNull
    public SourceCodeOffsets getOffsets() {
        return localContent.getOffsets();
    }

    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        String parseRootId = getParseRootId();
        if (parseRootId != null) {
            DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
            if (languageDialect != null) {
                fileViewProvider.getVirtualFile().putUserData(PARSE_ROOT_ID_KEY, getParseRootId());
                DBLanguagePsiFile file = fileViewProvider.createPsiFile(languageDialect);
                file.setUnderlyingObject(getObject());
                return file;
            }
        }
        return null;
    }

    public synchronized boolean isLoaded() {
        return localContent.isLoaded();
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

    public void refreshContentState() {
        new SynchronizedTask() {
            @Override
            protected String getSyncKey() {
                return "REFRESH_STATE:" + getUrl();
            }

            @Override
            protected boolean canExecute() {
                return !refreshing && isLoaded();
            }

            @Override
            protected void execute() {
                try {
                    refreshing = true;
                    DBSchemaObject object = getObject();

                    if (status == Status.OK || status == Status.MERGED) {
                        boolean checkSources = true;

                        ChangeTimestamp latestTimestamp = new ChangeTimestamp();
                        if (isChangeTracingSupported()) {
                            latestTimestamp = object.loadChangeTimestamp(contentType);
                            checkSources = databaseTimestamp.isOlderThan(latestTimestamp);
                            databaseTimestamp = latestTimestamp;
                        }

                        databaseTimestamp = latestTimestamp;

                        if (checkSources) {
                            Project project = object.getProject();
                            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                            SourceCodeContent latestContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);

                            if (status == Status.OK && !latestContent.matches(originalContent, true)) {
                                status = Status.OUTDATED;
                                databaseContent = latestContent;
                            }

                            if (status == Status.MERGED && !latestContent.matches(databaseContent, true)) {
                                status = Status.OUTDATED;
                                databaseContent = latestContent;
                            }
                        }
                    }

                } catch (SQLException e) {
                    LOGGER.warn("Error refreshing source content state", e);
                } finally {
                    refreshing = false;
                }
            }
        }.start();
    }

    public boolean isChangedInDatabase(boolean reload) {
        if (!refreshing && isLoaded()) {
            if (reload || databaseTimestamp.isDirty()) {
                refreshContentState();
            }
            return !refreshing && (status == Status.OUTDATED || status == Status.MERGED);
        }
        return false;
    }

    public boolean isMergeRequired() {
        return isModified() && status == Status.OUTDATED;
    }

    public void markAsMerged() {
        status = Status.MERGED;
    }

    @NotNull
    public Timestamp getDatabaseChangeTimestamp() {
        return databaseTimestamp.value();
    }

    @NotNull
    public CharSequence getOriginalContent() {
        return originalContent.getText();
    }

    @NotNull
    public CharSequence getContent() {
        return localContent.getText();
    }

    public void loadSourceFromDatabase() throws SQLException {
        DBSchemaObject object = getObject();
        Project project = object.getProject();
        SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
        localContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
        originalContent.setText(localContent.getText().toString());
        databaseContent = null;
        databaseTimestamp = object.loadChangeTimestamp(contentType);

        updateOffsets();
        setModified(false);
        sourceLoadError = null;
        status = Status.OK;
    }

    public void saveSourceToDatabase() throws SQLException {
        DBSchemaObject object = getObject();
        String oldContent = getOriginalContent().toString();
        String newContent = getContent().toString();
        object.executeUpdateDDL(contentType, oldContent, newContent);
        originalContent.setText(newContent);
        databaseContent = null;
        databaseTimestamp = object.loadChangeTimestamp(contentType);

        setModified(false);
        sourceLoadError = null;
        status = Status.OK;
    }

    void updateOffsets() {
        final Document document = DocumentUtil.getDocument(this);
        if (document != null) {
            new WriteActionRunner() {
                @Override
                public void run() {
                    DocumentUtil.setText(document, localContent.getText());
                    SourceCodeOffsets offsets = localContent.getOffsets();
                    GuardedBlockMarkers guardedBlocks = offsets.getGuardedBlocks();
                    if (!guardedBlocks.isEmpty()) {
                        DocumentUtil.removeGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION);
                        DocumentUtil.createGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION, guardedBlocks, null);
                    }
                }
            }.start();
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
        return localContent.getText().toString().getBytes(getCharset());
    }

    public long getLength() {
        return localContent.length();
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
        originalContent = new SourceCodeContent();
        localContent = new SourceCodeContent();
    }
}
