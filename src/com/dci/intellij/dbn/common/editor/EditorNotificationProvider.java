package com.dci.intellij.dbn.common.editor;

import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorNotifications;

public abstract class EditorNotificationProvider<T extends EditorNotificationPanel> extends EditorNotifications.Provider<T> {
    protected Project project;

    public EditorNotificationProvider(final Project project) {
        this.project = project;
    }

    public void updateEditorNotification(@Nullable final DBContentVirtualFile databaseContentFile) {
        new SimpleLaterInvocator() {
            @Override
            protected void execute() {
                if (!project.isDisposed()) {
                    EditorNotifications notifications = EditorNotifications.getInstance(project);
                    if (databaseContentFile ==  null) {
                        notifications.updateAllNotifications();
                    } else {
                        notifications.updateNotifications(databaseContentFile.getMainDatabaseFile());
                    }
                }
            }
        }.start();
    }
}
