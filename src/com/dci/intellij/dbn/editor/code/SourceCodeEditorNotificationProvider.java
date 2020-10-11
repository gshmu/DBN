package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.EditorNotificationProvider;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.code.diff.MergeAction;
import com.dci.intellij.dbn.editor.code.diff.SourceCodeDifManagerListener;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeEditorNotificationPanel;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeLoadErrorNotificationPanel;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeOutdatedNotificationPanel;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeReadonlyNotificationPanel;
import com.dci.intellij.dbn.execution.script.ScriptExecutionListener;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBSourceCodeVirtualFile;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SourceCodeEditorNotificationProvider extends EditorNotificationProvider<SourceCodeEditorNotificationPanel> {
    private static final Key<SourceCodeEditorNotificationPanel> KEY = Key.create("DBNavigator.SourceCodeEditorNotificationPanel");

    public SourceCodeEditorNotificationProvider() {
        this(null);
    }

    public SourceCodeEditorNotificationProvider(@Nullable Project project) {
        super(project);
        subscribe(SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
        subscribe(SourceCodeDifManagerListener.TOPIC, sourceCodeDifManagerListener);
        subscribe(EnvironmentManagerListener.TOPIC, environmentManagerListener);
        subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        subscribe(ScriptExecutionListener.TOPIC, scriptExecutionListener);
    }

    private final ScriptExecutionListener scriptExecutionListener = new ScriptExecutionListener() {
        @Override
        public void scriptExecuted(Project project, VirtualFile virtualFile) {
            updateEditorNotification(project, null);
        }
    };

    private final SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeLoaded(@NotNull DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad) {
            updateEditorNotification(sourceCodeFile.getProject(), sourceCodeFile);
        }

        @Override
        public void sourceCodeSaved(@NotNull DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {
            updateEditorNotification(sourceCodeFile.getProject(), sourceCodeFile);
        }
    };

    private final SourceCodeDifManagerListener sourceCodeDifManagerListener = new SourceCodeDifManagerListener() {
        @Override
        public void contentMerged(DBSourceCodeVirtualFile sourceCodeFile, MergeAction mergeAction) {
            updateEditorNotification(sourceCodeFile.getProject(), sourceCodeFile);
        }
    };

    private final EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerListener() {
        @Override
        public void configurationChanged(Project project) {
            updateEditorNotification(project, null);
        }

        @Override
        public void editModeChanged(Project project, DBContentVirtualFile databaseContentFile) {
            if (databaseContentFile instanceof DBSourceCodeVirtualFile) {
                updateEditorNotification(project, databaseContentFile);
            }
        }
    };

    private final FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            VirtualFile virtualFile = event.getNewFile();
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
                for (DBSourceCodeVirtualFile sourceCodeFile : databaseFile.getSourceCodeFiles()) {
                    updateEditorNotification(sourceCodeFile.getProject(), sourceCodeFile);
                }
            }
        }
    };

    @NotNull
    @Override
    public Key<SourceCodeEditorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public SourceCodeEditorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor, @NotNull Project project) {
        SourceCodeEditorNotificationPanel notificationPanel = null;
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            if (fileEditor instanceof SourceCodeEditor && Failsafe.check(fileEditor)) {
                DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
                DBSchemaObject editableObject = editableObjectFile.getObject();
                SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                DBSourceCodeVirtualFile sourceCodeFile = sourceCodeEditor.getVirtualFile();
                String sourceLoadError = sourceCodeFile.getSourceLoadError();
                if (StringUtil.isNotEmpty(sourceLoadError)) {
                    notificationPanel = new SourceCodeLoadErrorNotificationPanel(editableObject, sourceLoadError);

                } else if (sourceCodeFile.isChangedInDatabase(false)) {
                    notificationPanel = new SourceCodeOutdatedNotificationPanel(sourceCodeFile, sourceCodeEditor);

                } else if (sourceCodeFile.getEnvironmentType().isReadonlyCode()) {
                    notificationPanel = new SourceCodeReadonlyNotificationPanel(editableObject, sourceCodeEditor);

                }

            }
        }
        return notificationPanel;
    }
}
