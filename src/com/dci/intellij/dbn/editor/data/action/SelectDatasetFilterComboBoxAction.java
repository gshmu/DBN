package com.dci.intellij.dbn.editor.data.action;

import javax.swing.JComponent;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNComboBoxAction;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterGroup;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.Presentation;

public class SelectDatasetFilterComboBoxAction extends DBNComboBoxAction {
    public SelectDatasetFilterComboBoxAction() {
        Presentation presentation = getTemplatePresentation();
        presentation.setText("No Filter");
        presentation.setIcon(Icons.DATASET_FILTER_EMPTY);
    }

    @Override
    public JComponent createCustomComponent(Presentation presentation) {
        return super.createCustomComponent(presentation);
    }



    @NotNull
    protected DefaultActionGroup createPopupActionGroup(JComponent button) {
        DataContext dataContext = DataManager.getInstance().getDataContext(button);
        DatasetEditor datasetEditor = AbstractDataEditorAction.getDatasetEditor(dataContext);

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        if (datasetEditor != null) {
            DBDataset dataset = datasetEditor.getDataset();
            OpenFilterSettingsAction openFilterSettingsAction = new OpenFilterSettingsAction(datasetEditor);
            openFilterSettingsAction.setInjectedContext(true);
            actionGroup.add(openFilterSettingsAction);
            actionGroup.addSeparator();
            actionGroup.add(new SelectDatasetFilterAction(dataset, DatasetFilterManager.EMPTY_FILTER));
            actionGroup.addSeparator();

            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
            DatasetFilterGroup filterGroup = filterManager.getFilterGroup(dataset);
            for (DatasetFilter filter : filterGroup.getFilters()) {
                actionGroup.add(new SelectDatasetFilterAction(dataset, filter));
            }
        }
        return actionGroup;
    }

    @Override
    public void update(AnActionEvent e) {
        DatasetEditor datasetEditor = AbstractDataEditorAction.getDatasetEditor(e);

        Presentation presentation = e.getPresentation();
        boolean enabled =
                datasetEditor != null &&
                !datasetEditor.isInserting() &&
                !datasetEditor.isLoading();
        if (datasetEditor != null) {
            DBDataset dataset = datasetEditor.getDataset();

            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
            DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);

            if (activeFilter == null) {
                presentation.setText("No Filter");
                presentation.setIcon(Icons.DATASET_FILTER_EMPTY);
            } else {
                //e.getPresentation().setText(activeFilter.getName());
                presentation.setText(activeFilter.getName(), false);
                presentation.setIcon(activeFilter.getIcon());
            }
        }

        //if (!enabled) presentation.setIcon(null);
        presentation.setEnabled(enabled);
    }
}
