package com.dci.intellij.dbn.common.editor;

import com.dci.intellij.dbn.common.compatibility.LegacyEditorNotificationsProvider;
import com.dci.intellij.dbn.common.thread.Dispatch;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EditorNotificationProvider<T extends EditorNotificationPanel> extends LegacyEditorNotificationsProvider<T> {

    public EditorNotificationProvider() {}

    @Deprecated
    public EditorNotificationProvider(Project project) {
        super(project);
    }

    public void updateEditorNotification(@NotNull Project project, @Nullable DBContentVirtualFile databaseContentFile) {
        Dispatch.run(() -> {
            EditorNotifications notifications = EditorNotifications.getInstance(project);
            if (databaseContentFile ==  null) {
                notifications.updateAllNotifications();
            } else {
                notifications.updateNotifications(databaseContentFile.getMainDatabaseFile());
            }
        });
    }
}
