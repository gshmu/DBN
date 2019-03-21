package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.export.ui.ExportDataDialog;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class ExportDataAction extends AbstractDataEditorAction {

    ExportDataAction() {
        super("Export Data", Icons.DATA_EXPORT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);

        if (datasetEditor != null) {
            DBDataset dataset = datasetEditor.getDataset();
            ExportDataDialog dialog = new ExportDataDialog(datasetEditor.getEditorTable(), dataset);
            dialog.show();
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        DatasetEditor datasetEditor = getDatasetEditor(e);

        Presentation presentation = e.getPresentation();
        presentation.setText("Export Data");

        boolean enabled =
                datasetEditor != null &&
                !datasetEditor.isInserting();
        presentation.setEnabled(enabled);

    }
}
