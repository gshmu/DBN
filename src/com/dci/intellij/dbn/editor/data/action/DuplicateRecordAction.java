package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class DuplicateRecordAction extends AbstractDataEditorAction {

    public DuplicateRecordAction() {
        super("Duplicate record", Icons.DATA_EDITOR_DUPLICATE_RECORD);
    }

    public void actionPerformed(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) {
            datasetEditor.duplicateRecord();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Duplicate Record");
        DatasetEditor datasetEditor = getDatasetEditor(e);
        Project project = e.getProject();
        if (project == null || datasetEditor == null ||!datasetEditor.getConnectionHandler().isConnected()) {
            presentation.setEnabled(false);
        } else {
            presentation.setEnabled(true);
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean isEnvironmentReadonlyData = environmentManager.isReadonly(datasetEditor.getDataset(), DBContentType.DATA);

            presentation.setVisible(!isEnvironmentReadonlyData && !datasetEditor.isReadonlyData());
            if (datasetEditor.isInserting() || datasetEditor.isLoading() || datasetEditor.isDirty() || datasetEditor.isReadonly()) {
                presentation.setEnabled(false);
            } else {
                DatasetEditorTable editorTable = datasetEditor.getEditorTable();
                int[] selectedRows = editorTable.getSelectedRows();
                presentation.setEnabled(selectedRows != null && selectedRows.length == 1 && selectedRows[0] < editorTable.getModel().getRowCount());
            }
        }
    }
}