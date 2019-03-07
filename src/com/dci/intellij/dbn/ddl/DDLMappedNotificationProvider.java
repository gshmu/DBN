package com.dci.intellij.dbn.ddl;

import com.dci.intellij.dbn.common.ProjectRef;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.ddl.options.DDLFileGeneralSettings;
import com.dci.intellij.dbn.ddl.options.DDLFileSettings;
import com.dci.intellij.dbn.ddl.options.listener.DDLFileSettingsChangeListener;
import com.dci.intellij.dbn.ddl.ui.DDLMappedNotificationPanel;
import com.dci.intellij.dbn.editor.ddl.DDLFileEditor;
import com.dci.intellij.dbn.language.common.DBLanguageFileType;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DatabaseFileSystem;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DDLMappedNotificationProvider extends EditorNotifications.Provider<DDLMappedNotificationPanel> {
    private static final Key<DDLMappedNotificationPanel> KEY = Key.create("DBNavigator.DDLMappedNotificationPanel");
    private ProjectRef projectRef;
    public DDLMappedNotificationProvider(final Project project, @NotNull FrameStateManager frameStateManager) {
        this.projectRef = ProjectRef.from(project);

        EventUtil.subscribe(project, project, DDLFileAttachmentManagerListener.TOPIC, ddlFileAttachmentManagerListener);
        EventUtil.subscribe(project, project, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        EventUtil.subscribe(project, project, DDLFileSettingsChangeListener.TOPIC, ddlFileSettingsChangeListener);
    }

    private DDLFileAttachmentManagerListener ddlFileAttachmentManagerListener = new DDLFileAttachmentManagerListener() {
        @Override
        public void ddlFileDetached(VirtualFile virtualFile) {
            Project project = getProject();
            if (!project.isDisposed()) {
                EditorNotifications notifications = EditorNotifications.getInstance(project);
                notifications.updateNotifications(virtualFile);
            }
        }

        @Override
        public void ddlFileAttached(VirtualFile virtualFile) {
            Project project = getProject();
            if (!project.isDisposed()) {
                EditorNotifications notifications = EditorNotifications.getInstance(project);
                notifications.updateNotifications(virtualFile);
            }
        }
    };

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            updateDdlFileHeaders(file);
        }

        @Override
        public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
            updateDdlFileHeaders(file);
        }

        private void updateDdlFileHeaders(VirtualFile file) {
            Project project = getProject();
            if (!project.isDisposed() && file instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) file;
                if (!editableObjectFile.isDisposed()) {
                    DBObjectRef<DBSchemaObject> objectRef = editableObjectFile.getObjectRef();
                    DDLFileAttachmentManager attachmentManager = DDLFileAttachmentManager.getInstance(project);
                    List<VirtualFile> attachedDDLFiles = attachmentManager.getAttachedDDLFiles(objectRef);
                    if (attachedDDLFiles != null) {
                        EditorNotifications notifications = EditorNotifications.getInstance(project);
                        for (VirtualFile virtualFile : attachedDDLFiles) {
                            notifications.updateNotifications(virtualFile);
                        }
                    }
                }
            }
        }
    };

    private final DDLFileSettingsChangeListener ddlFileSettingsChangeListener = new DDLFileSettingsChangeListener() {
        @Override
        public void settingsChanged() {
            Project project = getProject();
            EditorNotifications notifications = EditorNotifications.getInstance(project);
            notifications.updateAllNotifications();
        }
    };

    @NotNull
    @Override
    public Key<DDLMappedNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public DDLMappedNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
        Project project = getProject();
        DDLFileGeneralSettings generalSettings = DDLFileSettings.getInstance(project).getGeneralSettings();
        if (generalSettings.isSynchronizeDDLFilesEnabled() && Failsafe.check(fileEditor)) {
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                if (fileEditor instanceof DDLFileEditor) {
                    DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
                    DBSchemaObject editableObject = editableObjectFile.getObject();
                    DDLFileEditor ddlFileEditor = (DDLFileEditor) fileEditor;
                    VirtualFile ddlVirtualFile = Failsafe.get(ddlFileEditor.getVirtualFile());
                    return createPanel(ddlVirtualFile, editableObject);
                }
                return null;
            } else {
                if (virtualFile.getFileType() instanceof DBLanguageFileType) {
                    DDLFileAttachmentManager attachmentManager = DDLFileAttachmentManager.getInstance(project);
                    DBSchemaObject editableObject = attachmentManager.getEditableObject(virtualFile);
                    if (editableObject != null) {
                        if (DatabaseFileSystem.isFileOpened(editableObject))
                            return createPanel(virtualFile, editableObject);
                    }
                }
            }
        }

        return null;
    }

    private DDLMappedNotificationPanel createPanel(@NotNull final VirtualFile virtualFile, final DBSchemaObject editableObject) {
        return new DDLMappedNotificationPanel(virtualFile, editableObject);
    }

    @NotNull
    public Project getProject() {
        return projectRef.getnn();
    }
}
