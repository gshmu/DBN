package com.dci.intellij.dbn.connection.mapping;

import com.dci.intellij.dbn.common.editor.EditorNotificationProvider;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.connection.mapping.ui.FileConnectionMappingNotificationPanel;
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
                notificationPanel = new FileConnectionMappingNotificationPanel(project, virtualFile, connectionMapping);
            }
        }
        return notificationPanel;
    }

    public static final FileConnectionMappingListener mappingListener = new FileConnectionMappingListener() {
        @Override
        public void mappingChanged(Project project, VirtualFile file) {
            if (file instanceof VirtualFileWindow) {
                VirtualFileWindow fileWindow = (VirtualFileWindow) file;
                file = fileWindow.getDelegate();
            }
            EditorNotifications notifications = EditorNotifications.getInstance(project);
            notifications.updateNotifications(file);
        }
    };
}
