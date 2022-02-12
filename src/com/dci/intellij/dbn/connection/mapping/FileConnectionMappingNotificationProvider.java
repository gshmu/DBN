package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.editor.EditorNotificationProvider;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.mapping.ui.FileConnectionMappingNotificationPanel;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.language.psql.PSQLFileType;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.intellij.injected.editor.VirtualFileWindow;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileConnectionMappingNotificationProvider extends EditorNotificationProvider<FileConnectionMappingNotificationPanel> {
    private static final Key<FileConnectionMappingNotificationPanel> KEY = Key.create("DBNavigator.FileConnectionMappingNotificationPanel");
    public FileConnectionMappingNotificationProvider() {
        ProjectEvents.subscribe(FileConnectionMappingListener.TOPIC, mappingListener);
    }

    @Deprecated
    public FileConnectionMappingNotificationProvider(@NotNull Project project) {
        super(project);
        ProjectEvents.subscribe(project, this, FileConnectionMappingListener.TOPIC, mappingListener);
    }

    @NotNull
    @Override
    public Key<FileConnectionMappingNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public FileConnectionMappingNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor, @NotNull Project project) {
        FileConnectionMappingNotificationPanel notificationPanel = null;

        FileType fileType = virtualFile.getFileType();
        if (fileType != SQLFileType.INSTANCE && fileType != PSQLFileType.INSTANCE) {
            FileConnectionMappingManager mappingManager = FileConnectionMappingManager.getInstance(project);
            FileConnectionMapping connectionMapping = mappingManager.getMapping(virtualFile);
            if (connectionMapping != null) {
                notificationPanel = new FileConnectionMappingNotificationPanel(virtualFile, connectionMapping);
            }
        }

        /*
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            if (fileEditor instanceof SourceCodeEditor && Failsafe.check(fileEditor)) {
                DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
                DBSchemaObject editableObject = editableObjectFile.getObject();
                SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                DBSourceCodeVirtualFile sourceCodeFile = sourceCodeEditor.getVirtualFile();
                String sourceLoadError = sourceCodeFile.getSourceLoadError();
                if (Strings.isNotEmpty(sourceLoadError)) {
                    notificationPanel = new FileConnectionMappingNotificationPanel(editableObject, sourceLoadError);

                } else if (sourceCodeFile.isChangedInDatabase(false)) {
                    notificationPanel = new FileConnectionMappingNotificationPanel(sourceCodeFile, sourceCodeEditor);

                } else if (sourceCodeFile.getEnvironmentType().isReadonlyCode()) {
                    notificationPanel = new FileConnectionMappingNotificationPanel(editableObject, sourceCodeEditor);

                }
            }
        }
*/
        return notificationPanel;
    }

    public static final FileConnectionMappingListener mappingListener = new FileConnectionMappingListener() {
        @Override
        public void connectionChanged(Project project, VirtualFile file, ConnectionHandler connection) {
            updateNotifications(project, file);
        }

        @Override
        public void schemaChanged(Project project, VirtualFile file, SchemaId schema) {
            updateNotifications(project, file);
        }

        @Override
        public void sessionChanged(Project project, VirtualFile file, DatabaseSession session) {
            updateNotifications(project, file);
        }
    };

    private static void updateNotifications(Project project, VirtualFile file) {
        if (file instanceof VirtualFileWindow) {
            VirtualFileWindow fileWindow = (VirtualFileWindow) file;
            file = fileWindow.getDelegate();
        }
        EditorNotifications notifications = EditorNotifications.getInstance(project);
        notifications.updateNotifications(file);
    }
}
