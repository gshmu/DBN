package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterType;
import com.dci.intellij.dbn.editor.data.options.DataEditorSettings;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CreateEditDatasetFilterAction extends AbstractDataEditorAction {
    public CreateEditDatasetFilterAction() {
        super("Create / Edit Filter", Icons.DATASET_FILTER_NEW);
    }

    @Override
    protected void actionPerformed(@NotNull AnActionEvent e, @NotNull Project project, @NotNull DatasetEditor datasetEditor) {
        DBDataset dataset = datasetEditor.getDataset();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter == null || activeFilter.getFilterType() == DatasetFilterType.NONE) {
            DataEditorSettings settings = DataEditorSettings.getInstance(dataset.getProject());
            DatasetFilterType filterType = settings.getFilterSettings().getDefaultFilterType();
            if (filterType == null || filterType == DatasetFilterType.NONE) {
                filterType = DatasetFilterType.BASIC;
            }


            filterManager.openFiltersDialog(dataset, false, true, filterType);
        }
        else {
            filterManager.openFiltersDialog(dataset, false, false,DatasetFilterType.NONE);
        }
    }

    @Override
    protected void update(@NotNull AnActionEvent e, @NotNull Project project, @Nullable DatasetEditor datasetEditor) {
        Presentation presentation = e.getPresentation();

        if (Failsafe.check(datasetEditor) && datasetEditor.getConnectionHandler().isConnected()) {
            DBDataset dataset = datasetEditor.getDataset();
            boolean enabled = !datasetEditor.isInserting() && !datasetEditor.isLoading();

            presentation.setEnabled(enabled);

            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
            DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
            if (activeFilter == null || activeFilter.getFilterType() == DatasetFilterType.NONE) {
                presentation.setText("Create Filter");
                presentation.setIcon(Icons.DATASET_FILTER_NEW);
            } else {
                presentation.setText("Edit Filter");
                presentation.setIcon(Icons.DATASET_FILTER_EDIT);
            }
        } else {
            presentation.setEnabled(false);
        }
    }
}
