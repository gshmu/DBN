package com.dci.intellij.dbn.vfs;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Constants;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMappingProvider;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.ddl.ObjectToDDLContentSynchronizer;
import com.dci.intellij.dbn.ddl.options.DDLFileGeneralSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;

public class DatabaseEditableObjectVirtualFile extends DatabaseObjectVirtualFile<DBSchemaObject> implements FileConnectionMappingProvider {
    public ThreadLocal<Document> FAKE_DOCUMENT = new ThreadLocal<Document>();
    private List<DatabaseContentVirtualFile> contentFiles;

    public DatabaseEditableObjectVirtualFile(DBSchemaObject object) {
        super(object);
    }

    public ConnectionHandler getActiveConnection() {
        return getConnectionHandler();
    }

    public DBSchema getCurrentSchema() {
        return getObject().getSchema();
    }

    public boolean preOpen() {
        DBSchemaObject object = getObject();
        if (object != null) {
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
                boolean ddlFileBinding = connectionHandler.getSettings().getDetailSettings().isDdlFileBinding();
                if (ddlFileBinding && ddlFileSettings.getLookupDDLFilesEnabled().value()) {
                    List<VirtualFile> attachedDDLFiles = getAttachedDDLFiles();
                    if (attachedDDLFiles == null || attachedDDLFiles.isEmpty()) {
                        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(project);
                        List<VirtualFile> virtualFiles = fileAttachmentManager.lookupDetachedDDLFiles(object);
                        if (virtualFiles.size() > 0) {
                            int exitCode = fileAttachmentManager.showFileAttachDialog(object, virtualFiles, true);
                            return exitCode != DialogWrapper.CANCEL_EXIT_CODE;
                        } else if (ddlFileSettings.getCreateDDLFilesEnabled().value()) {
                            int exitCode = Messages.showYesNoDialog(
                                    "Could not find any DDL file for " + object.getQualifiedNameWithType() + ". Do you want to create one? \n" +
                                            "(You can disable this check in \"DDL File\" options)",
                                    Constants.DBN_TITLE_PREFIX + "No DDL file found", Messages.getQuestionIcon());
                            if (exitCode == DialogWrapper.OK_EXIT_CODE) {
                                fileAttachmentManager.createDDLFile(object);
                            }
                        }
                    }
                }
            }
        } else {
            return false;
        }

        return true;
    }

    public synchronized List<DatabaseContentVirtualFile> getContentFiles() {
        if (contentFiles == null) {
            contentFiles = new ArrayList<DatabaseContentVirtualFile>();
            DBContentType objectContentType = getObject().getContentType();
            if (objectContentType.isBundle()) {
                DBContentType[] contentTypes = objectContentType.getSubContentTypes();
                for (DBContentType contentType : contentTypes) {
                    DatabaseContentVirtualFile virtualFile =
                            contentType.isCode() ? new SourceCodeVirtualFile(this, contentType) :
                            contentType.isData() ? new DatasetVirtualFile(this, contentType) : null;
                    contentFiles.add(virtualFile);
                }
            } else {
                DatabaseContentVirtualFile virtualFile =
                        objectContentType.isCode() ? new SourceCodeVirtualFile(this, objectContentType) :
                        objectContentType.isData() ? new DatasetVirtualFile(this, objectContentType) : null;
                contentFiles.add(virtualFile);
            }
        }
        return contentFiles;
    }

    @Nullable
    public List<VirtualFile> getAttachedDDLFiles() {
        DBSchemaObject object = getObject();
        if (object != null) {
            DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(object.getProject());
            if (object.getProperties().is(DBObjectProperty.EDITABLE)) {
                return fileAttachmentManager.getAttachedDDLFiles(object);
            }
        }
        return null;
    }

    public void updateDDLFiles() {
        for (DatabaseContentVirtualFile contentFile : getContentFiles()) {
            updateDDLFiles(contentFile.getContentType());
        }
    }

    public void updateDDLFiles(final DBContentType sourceContentType) {
        new ConditionalLaterInvocator() {
            public void execute() {
                ObjectToDDLContentSynchronizer synchronizer = new ObjectToDDLContentSynchronizer(sourceContentType, DatabaseEditableObjectVirtualFile.this);
                ApplicationManager.getApplication().runWriteAction(synchronizer);
            }
        }.start();
    }

    public DatabaseContentVirtualFile getContentFile(DBContentType contentType) {
        for (DatabaseContentVirtualFile contentFile : getContentFiles()) {
            if (contentFile.getContentType() == contentType) {
                return contentFile;
            }
        }
        return null;
    }

    /*********************************************************
     *                     VirtualFile                       *
     *********************************************************/
    @NotNull
    public FileType getFileType() {
        DBSchemaObject object = getObject();
        DDLFileType type = object == null ? null : object.getDDLFileType(null);
        return type == null ? SQLFileType.INSTANCE : type.getLanguageFileType();
    }

    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public DatabaseContentVirtualFile getDebuggableContentFile(){
        DBContentType contentType = getObject().getContentType();
        if (contentType == DBContentType.CODE) {
            return getContentFile(DBContentType.CODE);
        }

        if (contentType == DBContentType.CODE_SPEC_AND_BODY) {
            return getContentFile(DBContentType.CODE_BODY);
        }
        return null;
    }

    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        DBContentType mainContentType = getMainContentType();
        if (mainContentType != null) {
            return getContentFile(mainContentType).contentsToByteArray();
        }
        return new byte[0];
    }

    @Override
    public <T> T getUserData(@NotNull Key<T> key) {
        if (key == FileDocumentManagerImpl.DOCUMENT_KEY) {
            DBContentType mainContentType = getMainContentType();
            boolean isCode = mainContentType == DBContentType.CODE || mainContentType == DBContentType.CODE_BODY;
            if (isCode) {
                if (FAKE_DOCUMENT.get() != null) {
                    return (T) new WeakReference<Document>(FAKE_DOCUMENT.get());
                }

                DatabaseContentVirtualFile mainContentFile = getMainContentFile();
                Document document = DocumentUtil.getDocument(mainContentFile);
                return (T) new WeakReference<Document>(document);
            }
        }
        return super.getUserData(key);
    }

    public DBContentType getMainContentType() {
        DBContentType contentType = getObject().getContentType();
        return
            contentType == DBContentType.CODE ? DBContentType.CODE :
            contentType == DBContentType.CODE_SPEC_AND_BODY ? DBContentType.CODE_BODY : null;
    }

    public DatabaseContentVirtualFile getMainContentFile() {
        DBContentType mainContentType = getMainContentType();
        return getContentFile(mainContentType);
    }

    @Override
    public String getExtension() {
        return "psql";
    }

    @Override
    public void dispose() {
        DisposerUtil.dispose(contentFiles);
        super.dispose();
    }


}

