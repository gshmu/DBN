package com.dci.intellij.dbn.editor.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.message.MessageType;
import com.dci.intellij.dbn.common.util.EventUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.editor.data.ui.DatasetEditorNotificationPanel;
import com.dci.intellij.dbn.object.common.DBSchemaObject;
import com.dci.intellij.dbn.vfs.DBEditableObjectVirtualFile;
import com.intellij.ide.FrameStateManager;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotifications;

public class DatasetEditorNotificationProvider extends EditorNotifications.Provider<DatasetEditorNotificationPanel> {
    private static final Key<DatasetEditorNotificationPanel> KEY = Key.create("DBNavigator.DatasetEditorNotificationPanel");
    private Project project;

    public DatasetEditorNotificationProvider(final Project project, @NotNull FrameStateManager frameStateManager) {
        this.project = project;

        EventUtil.subscribe(project, project, DatasetLoadListener.TOPIC, datasetLoadListener);

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

    @NotNull
    @Override
    public Key<DatasetEditorNotificationPanel> getKey() {
        return KEY;
    }

    @Nullable
    @Override
    public DatasetEditorNotificationPanel createNotificationPanel(@NotNull VirtualFile virtualFile, @NotNull FileEditor fileEditor) {
        if (virtualFile instanceof DBEditableObjectVirtualFile) {
            if (fileEditor instanceof DatasetEditor) {
                DBEditableObjectVirtualFile editableObjectFile = (DBEditableObjectVirtualFile) virtualFile;
                DBSchemaObject editableObject = editableObjectFile.getObject();
                DatasetEditor datasetEditor = (DatasetEditor) fileEditor;

                if (!datasetEditor.isLoaded() && !datasetEditor.isLoading()) {
                    //return createNotLoadedPanel(datasetEditor);
                } else {
                    String sourceLoadError = datasetEditor.getDataLoadError();
                    if (StringUtil.isNotEmpty(sourceLoadError)) {
                        return createLoadErrorPanel(editableObject, sourceLoadError);
                    }
                }
            }
        }
        return null;
    }

    private DatasetEditorNotificationPanel createNotLoadedPanel(final DatasetEditor datasetEditor) {
        DatasetEditorNotificationPanel panel = new DatasetEditorNotificationPanel(MessageType.INFO);
        panel.setText("View data is not loaded automatically. To enable data load when the editor is opened, please change the \"Load view data..\" option in Data Editor settings");

        panel.createActionLabel("Load Data", new Runnable() {
            @Override
            public void run() {
                datasetEditor.loadData(DatasetEditorManager.INITIAL_LOAD_INSTRUCTIONS);
            }
        });
        return panel;
    }

    private DatasetEditorNotificationPanel createLoadErrorPanel(final DBSchemaObject editableObject, String sourceLoadError) {
        DatasetEditorNotificationPanel panel = new DatasetEditorNotificationPanel(MessageType.ERROR);
        panel.setText("Could not load data for " + editableObject.getQualifiedNameWithType() + ". Error details: " + sourceLoadError.replace("\n", " "));
        return panel;
    }
}
