package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.editor.EditorNotificationProvider;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.event.ProjectEvents;
import com.dci.intellij.dbn.common.util.Editors;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorLoadErrorNotificationPanel;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorNotificationPanel;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorReadonlyNotificationPanel;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBDatasetVirtualFile;
import com.dci.intellij.dbn.vfs.file.DBEditableObjectVirtualFile;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatasetEditorNotificationProvider extends EditorNotificationProvider<DatasetEditorNotificationPanel> {
    private static final Key<DatasetEditorNotificationPanel> KEY = Key.create("DBNavigator.DatasetEditorNotificationPanel");

    public DatasetEditorNotificationProvider() {
        ProjectEvents.subscribe(DatasetLoadListener.TOPIC, datasetLoadListener());
        ProjectEvents.subscribe(EnvironmentManagerListener.TOPIC, environmentManagerListener());
    }

    @Deprecated
    public DatasetEditorNotificationProvider(@NotNull Project project) {
        super(project);
        ProjectEvents.subscribe(project, this, DatasetLoadListener.TOPIC, datasetLoadListener());
        ProjectEvents.subscribe(project, this, EnvironmentManagerListener.TOPIC, environmentManagerListener());
    }

    @NotNull
    private static DatasetLoadListener datasetLoadListener() {
        return new DatasetLoadListener() {
            @Override
            public void datasetLoaded(@NotNull DBVirtualFile virtualFile) {
                Project project = virtualFile.getProject();
                EditorNotifications notifications = Editors.getNotifications(project);
                notifications.updateNotifications((VirtualFile) virtualFile);
            }

            @Override
            public void datasetLoading(@NotNull DBVirtualFile virtualFile) {
                datasetLoaded(virtualFile);
            }
        };
    }

    @NotNull
    private EnvironmentManagerListener environmentManagerListener() {
        return new EnvironmentManagerListener() {
            @Override
            public void configurationChanged(Project project) {
                updateEditorNotification(project, null);
            }

            @Override
            public void editModeChanged(Project project, DBContentVirtualFile databaseContentFile) {
                if (databaseContentFile instanceof DBDatasetVirtualFile) {
                    updateEditorNotification(project, databaseContentFile);
                }
            }
        };
    }

    @NotNull
    @Override
    public Key<DatasetEditorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public DatasetEditorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor, @NotNull Project project) {
        if (virtualFile instanceof DBEditableObjectVirtualFile && fileEditor instanceof DatasetEditor) {
            DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
            DatasetEditor datasetEditor = (DatasetEditor) fileEditor;

            DBSchemaObject editableObject = editableObjectFile.getObject();
            if (datasetEditor.isLoaded()) {
                String sourceLoadError = datasetEditor.getDataLoadError();
                if (Strings.isNotEmpty(sourceLoadError)) {
                    return new DatasetEditorLoadErrorNotificationPanel(editableObject, sourceLoadError);

                } else if (editableObject instanceof DBTable && editableObjectFile.getEnvironmentType().isReadonlyData()) {

                    return new DatasetEditorReadonlyNotificationPanel(editableObject);
                }
            }
        }
        return null;
    }
}
