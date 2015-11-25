package com.dci.intellij.dbn.editor.code;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeEditorNotificationPanel;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeLoadErrorNotificationPanel;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeOutdatedNotificationPanel;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.DBSourceCodeVirtualFile;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerAdapter;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;

public class SourceCodeEditorNotificationProvider extends EditorNotifications.Provider<SourceCodeEditorNotificationPanel> {
    private static final Key<SourceCodeEditorNotificationPanel> KEY = Key.create("DBNavigator.SourceCodeEditorNotificationPanel");
    private Project project;

    public SourceCodeEditorNotificationProvider(final Project project, @NotNull FrameStateManager frameStateManager) {
        this.project = project;

        EventUtil.subscribe(project, project, SourceCodeLoadListener.TOPIC, sourceCodeLoadListener);
        EventUtil.subscribe(project, project, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    SourceCodeLoadListener sourceCodeLoadListener = new SourceCodeLoadListener() {
        @Override
        public void sourceCodeLoaded(final VirtualFile virtualFile) {
            updateEditorNotification(virtualFile);
        }
    };

    private FileEditorManagerListener fileEditorManagerListener  =new FileEditorManagerAdapter() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            VirtualFile virtualFile = event.getNewFile();
            updateEditorNotification(virtualFile);
        }
    };

    void updateEditorNotification(final VirtualFile virtualFile) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            new ConditionalLaterInvocator() {
                @Override
                protected void execute() {
                    if (!project.isDisposed()) {
                        EditorNotifications notifications = EditorNotifications.getInstance(project);
                        notifications.updateNotifications(virtualFile);
                    }
                }
            }.start();
        }
    }

    @NotNull
    @Override
    public Key<SourceCodeEditorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public SourceCodeEditorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            if (fileEditor instanceof SourceCodeEditor) {
                DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
                DBSchemaObject editableObject = editableObjectFile.getObject();
                SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                DBSourceCodeVirtualFile sourceCodeFile = sourceCodeEditor.getVirtualFile();
                String sourceLoadError = sourceCodeFile.getSourceLoadError();
                if (StringUtil.isNotEmpty(sourceLoadError)) {
                    return createLoadErrorPanel(editableObject, sourceLoadError);
                } else if (sourceCodeFile.isChangedInDatabase(false)) {
                    return createOutdatedCodePanel(editableObject);
                }

            }
        }
        return null;
    }

    private static SourceCodeEditorNotificationPanel createLoadErrorPanel(final DBSchemaObject editableObject, String sourceLoadError) {
        SourceCodeLoadErrorNotificationPanel panel = new SourceCodeLoadErrorNotificationPanel();
        panel.setText("Could not load source for " + editableObject.getQualifiedNameWithType() + ". Error details: " + sourceLoadError.replace("\n", " "));
        return panel;
    }

    private static SourceCodeEditorNotificationPanel createOutdatedCodePanel(final DBSchemaObject editableObject) {
        SourceCodeOutdatedNotificationPanel panel = new SourceCodeOutdatedNotificationPanel();
        panel.setText("Outdated version. The " + editableObject.getQualifiedNameWithType() + " has been changed by another user.");
        return panel;
    }


}
