package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

public class ImportDataAction extends AbstractDataEditorAction {

    public ImportDataAction() {
        super("Import Data", Icons.DATA_IMPORT);
    }

    public void actionPerformed(AnActionEvent e) {
        MessageUtil.showInfoDialog("Data import is not implemented yet.", "Not implemented");
    }

    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Import Data");
        DatasetEditor datasetEditor = getDatasetEditor(e);
        if (datasetEditor == null) {
            presentation.setEnabled(false);
        } else {
            presentation.setVisible(!datasetEditor.isReadonlyData());
            boolean enabled =
                    datasetEditor.getEditorTable() != null &&
                    datasetEditor.getActiveConnection().isConnected() &&
                    !datasetEditor.isReadonly() &&
                    !datasetEditor.isInserting();
            presentation.setEnabled(enabled);
        }
    }
}