package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;

public class DeleteRecordAction extends AbstractDataEditorAction {

    public DeleteRecordAction() {
        super("Delete records", Icons.DATA_EDITOR_DELETE_RECORD);
    }

    public void actionPerformed(AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor != null) {
            datasetEditor.deleteRecords();
        }
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Delete Records");
        DatasetEditor datasetEditor = getDatasetEditor(e);
        Project project = e.getProject();
        if (project == null || datasetEditor == null || !datasetEditor.getConnectionHandler().isConnected()) {
            presentation.setEnabled(false);
        } else {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean isEnvironmentReadonlyData = environmentManager.isReadonly(datasetEditor.getDataset(), DBContentType.DATA);
            presentation.setVisible(!isEnvironmentReadonlyData && !datasetEditor.isReadonlyData());
            presentation.setEnabled(true);
            if (datasetEditor.isInserting() || datasetEditor.isLoading() || datasetEditor.isDirty() || datasetEditor.isReadonly()) {
                presentation.setEnabled(false);
            } else {
                DatasetEditorTable editorTable = datasetEditor.getEditorTable();
                if (editorTable.getSelectedRows() != null && editorTable.getSelectedRows().length > 0) {
                    for (int selectedRow : editorTable.getSelectedRows()) {
                        if (selectedRow < editorTable.getModel().getRowCount()) {
                            DatasetEditorModelRow row = editorTable.getModel().getRowAtIndex(selectedRow);
                            if (!row.isDeleted()) {
                                presentation.setEnabled(true);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}