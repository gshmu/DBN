package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.editor.EditorNotificationProvider;
import com.dci.intellij.dbn.common.environment.options.listener.EnvironmentManagerListener;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorLoadErrorNotificationPanel;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorNotificationPanel;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorReadonlyNotificationPanel;
import com.dci.intellij.dbn.object.DBTable;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBContentVirtualFile;
import com.dci.intellij.dbn.vfs.DBDatasetVirtualFile;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatasetEditorNotificationProvider extends EditorNotificationProvider<DatasetEditorNotificationPanel> {
    private static final Key<DatasetEditorNotificationPanel> KEY = Key.create("DBNavigator.DatasetEditorNotificationPanel");

    public DatasetEditorNotificationProvider(final Project project, @NotNull FrameStateManager frameStateManager) {
        super(project);

        EventUtil.subscribe(project, project, DatasetLoadListener.TOPIC, datasetLoadListener);
        EventUtil.subscribe(project, project, EnvironmentManagerListener.TOPIC, environmentManagerListener);

    }

    DatasetLoadListener datasetLoadListener = new DatasetLoadListener() {
        @Override
        public void datasetLoaded(VirtualFile virtualFile) {
            if (virtualFile != null && !project.isDisposed()) {
                EditorNotifications notifications = EditorNotifications.getInstance(project);
                notifications.updateNotifications(virtualFile);
            }
        }

        @Override
        public void datasetLoading(VirtualFile virtualFile) {
            datasetLoaded(virtualFile);
        }
    };

    private EnvironmentManagerListener environmentManagerListener = new EnvironmentManagerListener() {
        @Override
        public void configurationChanged() {
            updateEditorNotification(null);
        }

        @Override
        public void editModeChanged(DBContentVirtualFile databaseContentFile) {
            if (databaseContentFile instanceof DBDatasetVirtualFile) {
                updateEditorNotification(databaseContentFile);
            }
        }
    };

    @NotNull
    @Override
    public Key<DatasetEditorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public DatasetEditorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
        DatasetEditorNotificationPanel notificationPanel = null;
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            if (fileEditor instanceof DatasetEditor) {
                DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
                DBSchemaObject editableObject = editableObjectFile.getObject();
                DatasetEditor datasetEditor = (DatasetEditor) fileEditor;

                if (datasetEditor.isLoaded()) {
                    String sourceLoadError = datasetEditor.getDataLoadError();
                    if (StringUtil.isNotEmpty(sourceLoadError)) {
                        notificationPanel = new DatasetEditorLoadErrorNotificationPanel(editableObject, sourceLoadError);
                    } else if (editableObject instanceof DBTable && editableObjectFile.getEnvironmentType().isReadonlyData()) {
                        notificationPanel = new DatasetEditorReadonlyNotificationPanel(editableObject);
                    }
                }
            }
        }
        return notificationPanel;
    }
}
