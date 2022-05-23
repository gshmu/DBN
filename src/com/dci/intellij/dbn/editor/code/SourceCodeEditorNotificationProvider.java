package com.dci.intellij.dbn.editor.code;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.editor.EditorNotificationProvider;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.editor.code.diff.MergeAction;
import com.dci.intellij.dbn.editor.code.diff.SourceCodeDifManagerListener;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeEditorNotificationPanel;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeLoadErrorNotificationPanel;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeOutdatedNotificationPanel;
import com.dci.intellij.dbn.editor.code.ui.SourceCodeReadonlyNotificationPanel;
import com.dci.intellij.dbn.execution.script.ScriptExecutionListener;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
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
        ProjectEvents.subscribe(SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
        ProjectEvents.subscribe(SourceCodeDifManagerListener.TOPIC, sourceCodeDifManagerListener);
        ProjectEvents.subscribe(EnvironmentManagerListener.TOPIC, environmentManagerListener);
        ProjectEvents.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        ProjectEvents.subscribe(ScriptExecutionListener.TOPIC, scriptExecutionListener);

    }

    @Deprecated
    public SourceCodeEditorNotificationProvider(@NotNull Project project) {
        super(project);
        ProjectEvents.subscribe(project, this, SourceCodeManagerListener.TOPIC, sourceCodeManagerListener);
        ProjectEvents.subscribe(project, this, SourceCodeDifManagerListener.TOPIC, sourceCodeDifManagerListener);
        ProjectEvents.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener);
        ProjectEvents.subscribe(project, this, FileEditorManagerListener.FILE_EDITOR_MANAGER, fileEditorManagerListener);
        ProjectEvents.subscribe(project, this, ScriptExecutionListener.TOPIC, scriptExecutionListener);
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
        if (virtualFile instanceof DBVirtualFile) {
            if (fileEditor instanceof SourceCodeEditor && Failsafe.check(fileEditor)) {
                DBVirtualFile databaseFile = (DBVirtualFile) virtualFile;

                DBObject object = databaseFile.getObject();
                if (object instanceof DBSchemaObject) {
                    DBSchemaObject schemaObject = (DBSchemaObject) object;
                    SourceCodeEditor sourceCodeEditor = (SourceCodeEditor) fileEditor;
                    DBSourceCodeVirtualFile sourceCodeFile = sourceCodeEditor.getVirtualFile();
                    String sourceLoadError = sourceCodeFile.getSourceLoadError();
                    if (Strings.isNotEmpty(sourceLoadError)) {
                        return new SourceCodeLoadErrorNotificationPanel(schemaObject, sourceLoadError);

                    } else if (sourceCodeFile.isChangedInDatabase(false)) {
                        return new SourceCodeOutdatedNotificationPanel(sourceCodeFile, sourceCodeEditor);

                    } else if (sourceCodeFile.getEnvironmentType().isReadonlyCode()) {
                        return new SourceCodeReadonlyNotificationPanel(schemaObject, sourceCodeEditor);
                    }
                }
            }
        }
        return null;
    }
}
