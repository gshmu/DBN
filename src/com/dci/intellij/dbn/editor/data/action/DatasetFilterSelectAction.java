package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class DatasetFilterSelectAction extends DumbAwareAction {
    private DBDataset dataset;
    private DatasetFilter filter;

    protected DatasetFilterSelectAction(DBDataset dataset, DatasetFilter filter) {
        this.dataset = dataset;
        this.filter = filter;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = dataset.getProject();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter != filter) {
            filterManager.setActiveFilter(dataset, filter);
            DatasetEditorManager.getInstance(project).reloadEditorData(dataset);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setIcon(filter.getIcon());
        presentation.setText(filter.getName(), false);
        //presentation.setEnabled(dataset.getCache().isConnected());
        //e.getPresentation().setText(filter.getName());
    }
}
