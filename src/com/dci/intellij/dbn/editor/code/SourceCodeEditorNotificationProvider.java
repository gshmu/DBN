package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.EditorNotificationProvider;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.util.EventUtil;
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
import com.intellij.ide.FrameStateManager;
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

    public SourceCodeEditorNotificationProvider(@NotNull Project project) {
        this(project, FrameStateManager.getInstance());
    }

    public SourceCodeEditorNotificationProvider(@NotNull Project project, @NotNull FrameStateManager frameStateManager) {
        super(project);
        EventUtil.subscribe(project, project, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
        EventUtil.subscribe(project, project, SourceCodeDifManagerListener.TOPIC, sourceCodeDifManagerListener);
        EventUtil.subscribe(project, project, EnvironmentManagerListener.TOPIC, environmentManagerListener);
        EventUtil.subscribe(project, project, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        EventUtil.subscribe(project, project, ScriptExecutionListener.TOPIC, scriptExecutionListener);
    }

    private ScriptExecutionListener scriptExecutionListener = new ScriptExecutionListener() {
        @Override
        public void scriptExecuted(VirtualFile virtualFile) {
            updateEditorNotification(null);
        }
    };

    private SourceCodeManagerListener sourceCodeManagerListener = new SourceCodeManagerAdapter() {
        @Override
        public void sourceCodeLoaded(final DBSourceCodeVirtualFile sourceCodeFile, boolean initialLoad) {
            updateEditorNotification(sourceCodeFile);
        }

        @Override
        public void sourceCodeSaved(DBSourceCodeVirtualFile sourceCodeFile, @Nullable SourceCodeEditor fileEditor) {
            updateEditorNotification(sourceCodeFile);
        }
    };

    private SourceCodeDifManagerListener sourceCodeDifManagerListener = new SourceCodeDifManagerListener() {
        @Override
        public void contentMerged(DBSourceCodeVirtualFile sourceCodeFile, MergeAction mergeAction) {
            updateEditorNotification(sourceCodeFile);
        }
    };

    private EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerListener() {
        @Override
        public void configurationChanged() {
            updateEditorNotification(null);
        }

        @Override
        public void editModeChanged(DBContentVirtualFile databaseContentFile) {
            if (databaseContentFile instanceof DBSourceCodeVirtualFile) {
                updateEditorNotification(databaseContentFile);
            }
        }
    };

    private FileEditorManagerListener fileEditorManagerListener = new FileEditorManagerListener() {
        @Override
        public void selectionChanged(@NotNull FileEditorManagerEvent event) {
            VirtualFile virtualFile = event.getNewFile();
            if (virtualFile instanceof DBEditableObjectVirtualFile) {
                DBEditableObjectVirtualFile databaseFile = (DBEditableObjectVirtualFile) virtualFile;
                for (DBSourceCodeVirtualFile sourceCodeFile : databaseFile.getSourceCodeFiles()) {
                    updateEditorNotification(sourceCodeFile);
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
    public SourceCodeEditorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
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
