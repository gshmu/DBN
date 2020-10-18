package com.dci.intellij.dbn.common.compatibility;

import com.dci.intellij.dbn.common.event.ProjectManagerEventAdapter;
import com.dci.intellij.dbn.common.project.ProjectRef;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public abstract class LegacyEditorNotificationsProvider<T extends JComponent> extends EditorNotifications.Provider<T> implements ProjectManagerEventAdapter {
    private final ProjectRef projectRef;

    public LegacyEditorNotificationsProvider() {
        this(null);
    }

    @Deprecated // constructor injection
    public LegacyEditorNotificationsProvider(Project project) {
        this.projectRef = ProjectRef.of(project);
    }


    @Nullable
    @Override
    public final T createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
        return createNotificationPanel(virtualFile, fileEditor, getProject());
    }

    public abstract T createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor, @NotNull Project project);


    @NotNull
    @Deprecated // constructor injection
    protected final Project getProject() {
        return projectRef.ensure();
    }
}
