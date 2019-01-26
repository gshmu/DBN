package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.environment.EnvironmentManager;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.common.util.MessageUtil;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ImportDataAction extends AbstractDataEditorAction {

    public ImportDataAction() {
        super("Import Data", Icons.DATA_IMPORT);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = ActionUtil.ensureProject(e);
        MessageUtil.showInfoDialog(project, "Not implemented", "Data import is not implemented yet.");
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setText("Import Data");
        DatasetEditor datasetEditor = getDatasetEditor(e);
        Project project = e.getProject();
        if (project == null || datasetEditor == null) {
            presentation.setEnabled(false);
        } else {
            EnvironmentManager environmentManager = EnvironmentManager.getInstance(project);
            boolean isEnvironmentReadonlyData = environmentManager.isReadonly(datasetEditor.getDataset(), DBContentType.DATA);
            presentation.setVisible(!isEnvironmentReadonlyData && !datasetEditor.isReadonlyData());
            boolean enabled =
                    datasetEditor.getConnectionHandler().isConnected() &&
                    !datasetEditor.isReadonly() &&
                    !datasetEditor.isInserting();
            presentation.setEnabled(enabled);
        }
    }
}