package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.common.util.Documents;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.connection.session.DatabaseSessionBundle;
import com.dci.intellij.dbn.ddl.DDLFileAttachmentManager;
import com.dci.intellij.dbn.ddl.DDLFileManager;
import com.dci.intellij.dbn.ddl.DDLFileType;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.impl.FileDocumentManagerImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.dci.intellij.dbn.common.dispose.Failsafe.guarded;
import static com.dci.intellij.dbn.vfs.VirtualFileStatus.MODIFIED;
import static com.dci.intellij.dbn.vfs.VirtualFileStatus.SAVING;

public class DBEditableObjectVirtualFile extends DBObjectVirtualFile<DBSchemaObject>/* implements VirtualFileWindow*/ {
    private static final List<DBContentVirtualFile> EMPTY_CONTENT_FILES = Collections.emptyList();
    private final Latent<List<DBContentVirtualFile>> contentFiles = Latent.basic(() -> computeContentFiles());
    private transient EditorProviderId selectedEditorProviderId;
    private SessionId databaseSessionId;

    public DBEditableObjectVirtualFile(Project project, DBObjectRef object) {
        super(project, object);
        if (object.getObjectType() == DBObjectType.TABLE) {
            databaseSessionId = SessionId.MAIN;
        }
    }

    @Override
    public DatabaseSession getSession() {
        if (databaseSessionId != null) {
            DatabaseSessionBundle sessionBundle = getConnection().getSessionBundle();
            return sessionBundle.getSession(databaseSessionId);
        }
        return super.getSession();
    }

    public SessionId getDatabaseSessionId() {
        return databaseSessionId;
    }

    public void setDatabaseSessionId(SessionId databaseSessionId) {
        this.databaseSessionId = databaseSessionId;
    }

    public List<DBContentVirtualFile> getContentFiles() {
        return contentFiles.get();
    }

    private List<DBContentVirtualFile> computeContentFiles() {
        List<DBContentVirtualFile> contentFiles = new ArrayList<>();
        DBContentType objectContentType = getContentType();
        if (objectContentType.isBundle()) {
            DBContentType[] contentTypes = objectContentType.getSubContentTypes();
            for (DBContentType contentType : contentTypes) {
                DBContentVirtualFile virtualFile =
                        contentType.isCode() ? new DBSourceCodeVirtualFile(this, contentType) :
                        contentType.isData() ? new DBDatasetVirtualFile(this, contentType) : null;
                if (virtualFile != null) {
                    contentFiles.add(virtualFile);
                }
            }
        } else {
            DBContentVirtualFile virtualFile =
                    objectContentType.isCode() ? new DBSourceCodeVirtualFile(this, objectContentType) :
                    objectContentType.isData() ? new DBDatasetVirtualFile(this, objectContentType) : null;
            if (virtualFile != null) {
                contentFiles.add(virtualFile);
            }
        }
        return contentFiles;
    }

    public boolean isContentLoaded() {
        return contentFiles != null;
    }

    public List<DBSourceCodeVirtualFile> getSourceCodeFiles() {
        List<DBSourceCodeVirtualFile> sourceCodeFiles = new ArrayList<>();
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
        DDLFileAttachmentManager fileAttachmentManager = DDLFileAttachmentManager.getInstance(getProject());
        return fileAttachmentManager.getAttachedDDLFiles(getObjectRef());
    }

    @Nullable
    public <T extends DBContentVirtualFile> T getContentFile(DBContentType contentType) {
        for (DBContentVirtualFile contentFile : getContentFiles()) {
            if (contentFile.getContentType() == contentType) {
                return (T) contentFile;
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
    @Override
    @NotNull
    public FileType getFileType() {
        return guarded(SQLFileType.INSTANCE, () -> {
            DBSchemaObject object = getObject();
            DDLFileManager ddlFileManager = DDLFileManager.getInstance(object.getProject());
            DDLFileType type =  ddlFileManager.getDDLFileType(object.getObjectType(), getMainContentType());
            return type == null ? SQLFileType.INSTANCE : type.getLanguageFileType();
        });
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
        T userData = super.getUserData(key);
        if (key == FileDocumentManagerImpl.HARD_REF_TO_DOCUMENT_KEY) {
            return guarded(userData, () -> {
                DBContentType mainContentType = getMainContentType();
                boolean isCode = mainContentType == DBContentType.CODE || mainContentType == DBContentType.CODE_BODY;
                if (isCode) {
                    DBContentVirtualFile mainContentFile = getMainContentFile();
                    if (mainContentFile != null) {
                        Document document = Documents.getDocument(mainContentFile);
                        return (T) document;
                    }
                }
                return userData;
            });
        }
        return userData;
    }

    public DBContentType getMainContentType() {
        DBObjectRef<DBSchemaObject> objectRef = getObjectRef();
        DBObjectType objectType = objectRef.getObjectType();
        DBContentType contentType = objectType.getContentType();
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
    public void invalidate() {
        List<DBContentVirtualFile> contentVirtualFiles = contentFiles.value();
        if (contentVirtualFiles != null) {
            for (DBContentVirtualFile virtualFile : contentVirtualFiles) {
                virtualFile.invalidate();
            }
        }

        contentFiles.set(EMPTY_CONTENT_FILES);
        super.invalidate();
    }


    public boolean isModified() {
        if (isContentLoaded()) {
            for (DBContentVirtualFile contentVirtualFile : getContentFiles()) {
                if (contentVirtualFile.is(MODIFIED)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isSaving() {
        if (isContentLoaded()) {
            for (DBSourceCodeVirtualFile sourceCodeFile : getSourceCodeFiles()) {
                if (sourceCodeFile.is(SAVING)) {
                    return true;
                }
            }
        }

        return false;
    }

    @NotNull
    public DBContentType getContentType() {
        return object.getObjectType().getContentType();
    }
}

