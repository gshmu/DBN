package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class InsertRecordAction extends AbstractDataEditorAction {

    public InsertRecordAction() {
        super("Insert record", Icons.DATA_EDITOR_INSERT_RECORD);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) {
            Failsafe.nn(datasetEditor).insertRecord();
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Insert record");
        DatasetEditor datasetEditor = getDatasetEditor(e);
        Project project = e.getProject();
        if (project == null || datasetEditor == null) {
            presentation.setEnabled(false);
        } else {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean isEnvironmentReadonlyData = environmentManager.isReadonly(datasetEditor.getDataset(), DBContentType.DATA);
            presentation.setVisible(!isEnvironmentReadonlyData && !datasetEditor.isReadonlyData());
            presentation.setEnabled(
                    datasetEditor.getConnectionHandler().isConnected() &&
                    !datasetEditor.isReadonly() &&
                    !datasetEditor.isInserting() && 
                    !datasetEditor.isLoading() &&
                    !datasetEditor.isDirty());

        }
    }
}