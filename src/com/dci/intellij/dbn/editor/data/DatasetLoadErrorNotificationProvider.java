package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.common.event.EventManager;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.data.ui.DatasetLoadErrorNotificationPanel;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DatabaseEditableObjectFile;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatasetLoadErrorNotificationProvider extends EditorNotifications.Provider<DatasetLoadErrorNotificationPanel> {
    private static final Key<DatasetLoadErrorNotificationPanel> KEY = Key.create("DBNavigator.DatasetLoadErrorNotificationPanel");
    private Project project;

    public DatasetLoadErrorNotificationProvider(final Project project, @NotNull FrameStateManager frameStateManager) {
        this.project = project;

        EventManager.subscribe(project, DatasetLoadListener.TOPIC, datasetLoadListener);

    }

    DatasetLoadListener datasetLoadListener = new DatasetLoadListener() {
        @Override
        public void datasetLoaded(VirtualFile virtualFile) {
            if (virtualFile != null && !project.isDisposed()) {
                EditorNotifications notifications = EditorNotifications.getInstance(project);
                notifications.updateNotifications(virtualFile);
            }
        }
    };

    @Override
    public Key<DatasetLoadErrorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public DatasetLoadErrorNotificationPanel createNotificationPanel(VirtualFile virtualFile, FileEditor fileEditor) {
        if (virtualFile instanceof DatabaseEditableObjectFile) {
            if (fileEditor instanceof DatasetEditor) {
                DatabaseEditableObjectFile editableObjectFile = (DatabaseEditableObjectFile) virtualFile;
                DBSchemaObject editableObject = editableObjectFile.getObject();
                DatasetEditor datasetEditor = (DatasetEditor) fileEditor;
                String sourceLoadError = datasetEditor.getDataLoadError();
                if (StringUtil.isNotEmpty(sourceLoadError)) {
                    return createPanel(editableObject, sourceLoadError);
                }

            }
        }
        return null;
    }

    private DatasetLoadErrorNotificationPanel createPanel(final DBSchemaObject editableObject, String sourceLoadError) {
        DatasetLoadErrorNotificationPanel panel = new DatasetLoadErrorNotificationPanel();
        panel.setText("Could not load data for " + editableObject.getQualifiedNameWithType() + ". Error details: " + sourceLoadError.replace("\n", " "));
        return panel;
    }


}
