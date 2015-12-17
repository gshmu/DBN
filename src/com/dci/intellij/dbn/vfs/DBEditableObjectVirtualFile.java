package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.thread.SimpleTask;
import com.dci.intellij.dbn.common.util.CollectionUtil;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.ddl.options.DDLFileGeneralSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

public class DBEditableObjectVirtualFile extends DBObjectVirtualFile<DBSchemaObject> implements FileConnectionMappingProvider {
    public ThreadLocal<Document> FAKE_DOCUMENT = new ThreadLocal<Document>();
    private static final List<DBContentVirtualFile> EMPTY_CONTENT_FILES = Collections.emptyList();
    private List<DBContentVirtualFile> contentFiles;
    private transient EditorProviderId selectedEditorProviderId;

    public DBEditableObjectVirtualFile(DBSchemaObject object) {
        super(object);
    }

    @Nullable
    public ConnectionHandler getActiveConnection() {
        return getConnectionHandler();
    }

    @Nullable
    public DBSchema getCurrentSchema() {
        return getObject().getSchema();
    }

    public boolean preOpen() {
        final DBSchemaObject object = getObject();
        Project project = object.getProject();
        DBContentType contentType = object.getContentType();
        if (contentType == DBContentType.DATA) {
            DBDataset dataset = (DBDataset) object;
            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
            DatasetFilter filter = filterManager.getActiveFilter(dataset);

            if (filter == null) {
                DataEditorSettings settings = DataEditorSettings.getInstance(project);
                if (settings.getFilterSettings().isPromptFilterDialog()) {
                    int exitCode = filterManager.openFiltersDialog(dataset, true, false, settings.getFilterSettings().getDefaultFilterType());
                    return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                }
            }
        }
        else if (contentType.isOneOf(DBContentType.CODE, DBContentType.CODE_SPEC_AND_BODY)) {

            DDLFileGeneralSettings ddlFileSettings = DDLFileSettings.getInstance(project).getGeneralSettings();
            ConnectionHandler connectionHandler = object.getConnectionHandler();
            boolean ddlFileBinding = connectionHandler.getSettings().getDetailSettings().isEnableDdlFileBinding();
            if (ddlFileBinding && ddlFileSettings.isLookupDDLFilesEnabled()) {
                List<VirtualFile> attachedDDLFiles = getAttachedDDLFiles();
                if (attachedDDLFiles == null || attachedDDLFiles.isEmpty()) {
                    final DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                    List<VirtualFile> virtualFiles = fileAttachmentManager.lookupDetachedDDLFiles(object);
                    if (virtualFiles.size() > 0) {
                        int exitCode = DDLFileAttachmentManager.showFileAttachDialog(object, virtualFiles, true);
                        return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                    } else if (ddlFileSettings.isCreateDDLFilesEnabled()) {
                        MessageUtil.showQuestionDialog(
                                project, "No DDL file found",
                                "Could not find any DDL file for " + object.getQualifiedNameWithType() + ". Do you want to create one? \n" +
                                "(You can disable this check in \"DDL File\" options)", MessageUtil.OPTIONS_YES_NO, 0,
                                new SimpleTask() {
                                    @Override
                                    protected boolean canExecute() {
                                        return getOption() == DialogWrapper.OK_EXIT_CODE;
                                    }

                                    @Override
                                    protected void execute() {
                                        fileAttachmentManager.createDDLFile(object);
                                    }
                                });
                    }
                }
            }
        }

        return true;
    }

    public List<DBContentVirtualFile> getContentFiles() {
        if (contentFiles == null) {
            synchronized (this) {
                if (contentFiles == null) {
                    contentFiles = new ArrayList<DBContentVirtualFile>();
                    DBContentType objectContentType = getObject().getContentType();
                    if (objectContentType.isBundle()) {
                        DBContentType[] contentTypes = objectContentType.getSubContentTypes();
                        for (DBContentType contentType : contentTypes) {
                            DBContentVirtualFile virtualFile =
                                    contentType.isCode() ? new DBSourceCodeVirtualFile(this, contentType) :
                                            contentType.isData() ? new DBDatasetVirtualFile(this, contentType) : null;
                            if (virtualFile != null) {
                                contentFiles.add(virtualFile);
                                Disposer.register(this, virtualFile);
                            }
                        }
                    } else {
                        DBContentVirtualFile virtualFile =
                                objectContentType.isCode() ? new DBSourceCodeVirtualFile(this, objectContentType) :
                                        objectContentType.isData() ? new DBDatasetVirtualFile(this, objectContentType) : null;
                        if (virtualFile != null) {
                            contentFiles.add(virtualFile);
                            Disposer.register(this, virtualFile);
                        }
                    }
                }
            }
        }
        return contentFiles;
    }

    public boolean isContentLoaded() {
        return contentFiles != null;
    }

    public List<DBSourceCodeVirtualFile> getSourceCodeFiles() {
        List<DBSourceCodeVirtualFile> sourceCodeFiles = new ArrayList<DBSourceCodeVirtualFile>();
        List<DBContentVirtualFile> contentFiles = getContentFiles();
        for (DBContentVirtualFile contentFile : contentFiles) {
            if (contentFile instanceof DBSourceCodeVirtualFile) {
                sourceCodeFiles.add((DBSourceCodeVirtualFile) contentFile);
            }
        }
        return sourceCodeFiles;
    }

    @Nullable
    public List<VirtualFile> getAttachedDDLFiles() {
        DBSchemaObject object = getObject();
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(object.getProject());
        if (object.getProperties().is(DBObjectProperty.EDITABLE)) {
            return fileAttachmentManager.getAttachedDDLFiles(object);
        }
        return null;
    }

    @Nullable
    public DBContentVirtualFile getContentFile(DBContentType contentType) {
        for (DBContentVirtualFile contentFile : getContentFiles()) {
            if (contentFile.getContentType() == contentType) {
                return contentFile;
            }
        }
        return null;
    }

    public EditorProviderId getSelectedEditorProviderId() {
        return selectedEditorProviderId;
    }

    public void setSelectedEditorProviderId(EditorProviderId selectedEditorProviderId) {
        this.selectedEditorProviderId = selectedEditorProviderId;
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/
    @NotNull
    public FileType getFileType() {
        DBSchemaObject object = getObject();
        DDLFileType type = object.getDDLFileType(null);
        return type == null ? SQLFileType.INSTANCE : type.getLanguageFileType();
    }

    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        DBContentType mainContentType = getMainContentType();
        if (mainContentType != null) {
            DBContentVirtualFile contentFile = getContentFile(mainContentType);
            return contentFile == null ? new byte[0] : contentFile.contentsToByteArray();
        }
        return new byte[0];
    }

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        if (key == FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY) {
            try {
                DBContentType mainContentType = getMainContentType();
                boolean isCode = mainContentType == DBContentType.CODE || mainContentType == DBContentType.CODE_BODY;
                if (isCode) {
                    if (FAKE_DOCUMENT.get() != null) {
                        return (T) FAKE_DOCUMENT.get();
                    }

                    DBContentVirtualFile mainContentFile = getMainContentFile();
                    if (mainContentFile != null) {
                        Document document = DocumentUtil.getDocument(mainContentFile);
                        return (T) document;
                    }
                }
            } catch (AlreadyDisposedException e) {

            }
        }
        return super.getUserData(key);
    }

    public DBContentType getMainContentType() {
        DBSchemaObject object = getObject();
        DBContentType contentType = object.getContentType();
        return
            contentType == DBContentType.CODE ? DBContentType.CODE :
            contentType == DBContentType.CODE_SPEC_AND_BODY ? DBContentType.CODE_BODY : null;
    }

    public DBContentVirtualFile getMainContentFile() {
        DBContentType mainContentType = getMainContentType();
        return getContentFile(mainContentType);
    }

    @Override
    public String getExtension() {
        return "psql";
    }

    @Override
    public void dispose() {
        super.dispose();
        CollectionUtil.clearCollection(contentFiles);
        contentFiles = EMPTY_CONTENT_FILES;
    }


    public boolean isModified() {
        if (isContentLoaded()) {
            for (DBContentVirtualFile contentVirtualFile : getContentFiles()) {
                if (contentVirtualFile.isModified()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSaving() {
        if (isContentLoaded()) {
            for (DBSourceCodeVirtualFile sourceCodeFile : getSourceCodeFiles()) {
                if (sourceCodeFile.isSaving()) {
                    return true;
                }
            }
        }

        return false;
    }
}

