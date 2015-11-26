package com.dci.intellij.dbn.editor.code;

import java.sql.Timestamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.thread.ConditionalLaterInvocator;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
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
import com.intellij.util.text.DateFormatUtil;

public class SourceCodeEditorNotificationProvider extends EditorNotifications.Provider<SourceCodeEditorNotificationPanel> {
    private static final Key<SourceCodeEditorNotificationPanel> KEY = Key.create("DBNavigator.SourceCodeEditorNotificationPanel");
    private Project project;

    public SourceCodeEditorNotificationProvider(final Project project, @NotNull FrameStateManager frameStateManager) {
        this.project = project;

        EventUtil.subscribe(project, project, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
        EventUtil.subscribe(project, project, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
    }

    SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeLoaded(final DBSourceCodeVirtualFile sourceCodeFile, boolean isInitialLoad) {
            updateEditorNotification(sourceCodeFile);
        }
    };

    private FileEditorManagerListener fileEditorManagerListener  =new FileEditorManagerAdapter() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            VirtualFile virtualFile = event.getNewFile();
            if (virtualFile instanceof DBSourceCodeVirtualFile) {
                DBSourceCodeVirtualFile sourceCodeFile = (DBSourceCodeVirtualFile) virtualFile;
                updateEditorNotification(sourceCodeFile);
            }
        }
    };

    void updateEditorNotification(final DBSourceCodeVirtualFile sourceCodeFile) {
        new ConditionalLaterInvocator() {
            @Override
            protected void execute() {
                if (!project.isDisposed()) {
                    EditorNotifications notifications = EditorNotifications.getInstance(project);
                    notifications.updateNotifications(sourceCodeFile.getMainDatabaseFile());
                }
            }
        }.start();
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
                    return createOutdatedCodePanel(editableObject, sourceCodeFile, sourceCodeEditor);
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

    private SourceCodeEditorNotificationPanel createOutdatedCodePanel(final DBSchemaObject editableObject, final DBSourceCodeVirtualFile sourceCodeFile, final SourceCodeEditor sourceCodeEditor) {
        SourceCodeOutdatedNotificationPanel panel = new SourceCodeOutdatedNotificationPanel();
        Timestamp timestamp = sourceCodeFile.getChangedInDatabaseTimestamp();
        panel.setText("Outdated version. The " + editableObject.getQualifiedNameWithType() + " was modified by another user (" + DateFormatUtil.formatPrettyDateTime(timestamp).toLowerCase() + ")");
        panel.createActionLabel("Show Diff", new Runnable() {
            @Override
            public void run() {
                if (!project.isDisposed()) {
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    sourceCodeManager.opedDatabaseDiffWindow(sourceCodeFile);
                }
            }
        });

        if (sourceCodeFile.isModified()) {
            panel.createActionLabel("Merge", new Runnable() {
                @Override
                public void run() {
                    if (!project.isDisposed()) {
                        try {
                            SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                            CharSequence databaseContent = sourceCodeManager.loadSourceCodeFromDatabase(editableObject, sourceCodeEditor.getContentType());
                            sourceCodeManager.openCodeMergeDialog(databaseContent.toString(), sourceCodeFile, sourceCodeEditor, false);
                        }catch (Exception e) {
                            MessageUtil.showErrorDialog(project, "Could not load sources from database.", e);

                        }
                    }
                }
            });
        }

        panel.createActionLabel(sourceCodeFile.isModified() ? "Revert local changes" : "Reload", new Runnable() {
            @Override
            public void run() {
                if (!project.isDisposed()) {
                    SourceCodeManager sourceCodeManager = SourceCodeManager.getInstance(project);
                    sourceCodeManager.loadSourceFromDatabase(sourceCodeFile);
                }
            }
        });

        return panel;
    }


}
