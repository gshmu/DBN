package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.DevNullStreams;
import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.thread.BackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.thread.WriteActionRunner;
import com.dci.intellij.dbn.common.util.ChangeTimestamp;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionProvider;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.code.SourceCodeContent;
import com.dci.intellij.dbn.editor.code.SourceCodeEditor;
import com.dci.intellij.dbn.editor.code.SourceCodeLoadListener;
import com.dci.intellij.dbn.editor.code.SourceCodeManager;
import com.dci.intellij.dbn.editor.code.SourceCodeOffsets;
import com.dci.intellij.dbn.execution.statement.DataDefinitionChangeListener;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.common.DBLanguagePsiFile;
import com.dci.intellij.dbn.language.common.psi.PsiUtil;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.intellij.lang.Language;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiDocumentManagerImpl;

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
        DBSchemaObject object = getObject();
        updateChangeTimestamp();
        setCharset(databaseFile.getConnectionHandler().getSettings().getDetailSettings().getCharset());
        Project project = FailsafeUtil.get(databaseFile.getProject());
        try {
            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
            SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
            content = sourceCodeContent.getText();
            offsets = sourceCodeContent.getOffsets();
            sourceLoadError = null;
        } catch (SQLException e) {
            content = "";
            sourceLoadError = e.getMessage();
            //MessageUtil.showErrorDialog("Could not load sourcecode for " + object.getQualifiedNameWithType() + " from database.", e);
        }
        EventUtil.subscribe(project, this, DataDefinitionChangeListener.TOPIC, dataDefinitionChangeListener);
    }

    private final DataDefinitionChangeListener dataDefinitionChangeListener = new DataDefinitionChangeListener() {
        @Override
        public void dataDefinitionChanged(DBSchema schema, DBObjectType objectType) {
/*
            if (schema.getConnectionHandler() == connectionHandler) {
                DBObjectList childObjectList = schema.getChildObjectList(objectType);
                if (childObjectList != null && childObjectList.isLoaded()) {
                    childObjectList.reload();
                }

                Set<DBObjectType> childObjectTypes = objectType.getChildren();
                for (DBObjectType childObjectType : childObjectTypes) {
                    DBObjectListContainer childObjects = schema.getChildObjects();
                    if (childObjects != null) {
                        childObjectList = childObjects.getHiddenObjectList(childObjectType);
                        if (childObjectList != null && childObjectList.isLoaded()) {
                            childObjectList.reload();
                        }
                    }
                }
            }
*/
        }

        @Override
        public void dataDefinitionChanged(@NotNull final DBSchemaObject schemaObject) {
            if (schemaObject.equals(getObject())) {
                if (isModified()) {
                    MessageUtil.showQuestionDialog(
                            getProject(), "Unsaved changes",
                            "The " + schemaObject.getQualifiedNameWithType() + " has been updated in database. You have unsaved changes in the object editor.\n" +
                            "Do you want to discard the changes and reload the updated database version?",
                            new String[]{"Reload", "Keep changes"}, 0,
                            new SimpleTask() {
                                @Override
                                protected boolean canExecute() {
                                    return getOption() == 0;
                                }

                                @Override
                                protected void execute() {
                                    reloadAndUpdateEditors(false);
                                }
                            });
                } else {
                    reloadAndUpdateEditors(true);
                }
            }
        }
    };

    private void reloadAndUpdateEditors(boolean startInBackground) {
        Project project = getProject();
        if (project != null) {
            new BackgroundTask(project, "Reloading object source code", startInBackground) {
                @Override
                protected void execute(@NotNull ProgressIndicator progressIndicator) throws InterruptedException {
                    boolean reloaded = reloadFromDatabase();
                    if (reloaded) {
                        new WriteActionRunner() {
                            public void run() {
                                FileEditorManager fileEditorManager = FileEditorManager.getInstance(getProject());
                                FileEditor[] allEditors = fileEditorManager.getAllEditors(getMainDatabaseFile());
                                for (FileEditor fileEditor : allEditors) {
                                    if (fileEditor instanceof SourceCodeEditor) {
                                        SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                                        if (sourceCodeEditor.getVirtualFile().equals(DBSourceCodeVirtualFile.this)) {
                                            Editor editor = sourceCodeEditor.getEditor();
                                            editor.getDocument().setText(content);
                                            setModified(false);
                                        }
                                    }
                                }
                            }
                        }.start();
                    }
                }
            }.start();
        }
    }

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
                changeTimestampCheck = timestamp;
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

                return changeTimestamp != null && changeTimestampCheck != null && changeTimestamp.value().before(changeTimestampCheck.value());
            } catch (Exception e) {
                LOGGER.warn("Error loading object timestamp", e);
            }

        }
        return false;
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

    public boolean reloadFromDatabase() {
        Project project = getProject();
        if (project != null) {
            try {
                updateChangeTimestamp();
                originalContent = EMPTY_CONTENT;

                DBSchemaObject object = getObject();
                SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                SourceCodeContent sourceCodeContent = sourceCodeManager.loadSourceFromDatabase(object, contentType);
                content = sourceCodeContent.getText();
                offsets = sourceCodeContent.getOffsets();

                getMainDatabaseFile().updateDDLFiles(getContentType());
                setModified(false);
                sourceLoadError = null;
                return true;
            } catch (SQLException e) {
                sourceLoadError = e.getMessage();
                DBSchemaObject object = mainDatabaseFile.getObject();
                MessageUtil.showErrorDialog(project, "Could not reload sourcecode for " + object.getQualifiedNameWithType() + " from database.", e);
                return false;
            } finally {
                EventUtil.notify(project, SourceCodeLoadListener.TOPIC).sourceCodeLoaded(mainDatabaseFile);
            }
        }
        return false;
    }

    public void updateToDatabase() throws SQLException {
        DBSchemaObject object = getObject();
        CharSequence lastSavedContent = getLastSavedContent();
        object.executeUpdateDDL(getContentType(), lastSavedContent.toString(), content.toString());
        updateChangeTimestamp();
        getMainDatabaseFile().updateDDLFiles(getContentType());
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
