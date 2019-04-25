package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.data.export.ui.ExportDataDialog;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExportDataAction extends AbstractDataEditorAction {

    ExportDataAction() {
        super("Export Data", Icons.DATA_EXPORT);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        DBDataset dataset = datasetEditor.getDataset();
        ExportDataDialog dialog = new ExportDataDialog(datasetEditor.getEditorTable(), dataset);
        dialog.show();
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Export Data");

        boolean enabled =
                datasetEditor != null &&
                !datasetEditor.isInserting();
        presentation.setEnabled(enabled);

    }
}
