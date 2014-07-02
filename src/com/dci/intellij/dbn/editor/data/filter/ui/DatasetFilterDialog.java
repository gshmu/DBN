package com.dci.intellij.dbn.editor.data.filter.ui;

import com.dci.intellij.dbn.common.ui.dialog.DBNDialog;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetBasicFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterGroup;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterType;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import java.awt.event.ActionEvent;

public class DatasetFilterDialog extends DBNDialog {
    private boolean isAutomaticPrompt;
    private DBObjectRef<DBDataset> datasetRef;
    private DatasetFilterForm mainForm;
    private DatasetFilterGroup filterGroup;

    public DatasetFilterDialog(DBDataset dataset, boolean isAutomaticPrompt, boolean createNewFilter, DatasetFilterType defaultFilterType) {
        super(dataset.getProject(), "Data Filters", true);
        construct(dataset, isAutomaticPrompt);
        if ((createNewFilter || filterGroup.getFilters().isEmpty()) && defaultFilterType != DatasetFilterType.NONE) {
            DatasetFilter filter =
                    defaultFilterType == DatasetFilterType.BASIC ? filterGroup.createBasicFilter(true) :
                    defaultFilterType == DatasetFilterType.CUSTOM ? filterGroup.createCustomFilter(true) : null;

            mainForm.getFilterList().setSelectedValue(filter, true);
        }
        init();
    }

    private DBDataset getDataset() {
        return DBObjectRef.get(datasetRef);
    }

    protected String getDimensionServiceKey() {
        return "DBNavigator.DatasetFilter";
    }

    public DatasetFilterDialog(DBDataset dataset, DatasetBasicFilter basicFilter) {
        super(dataset.getProject(), "Data filters", true);
        construct(dataset, false);
        mainForm.getFilterList().setSelectedValue(basicFilter, true);
        init();
    }

    private void construct(DBDataset dataset, boolean isAutomaticPrompt) {
        this.datasetRef = DBObjectRef.from(dataset);
        this.isAutomaticPrompt = isAutomaticPrompt;
        setModal(true);
        setResizable(true);
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
        filterGroup = filterManager.getFilterGroup(dataset);
        mainForm = filterGroup.createConfigurationEditor();
    }

    public DatasetFilterGroup getFilterGroup() {
        return filterGroup;
    }

    @NotNull
    protected final Action[] createActions() {
        if (isAutomaticPrompt) {
            return new Action[]{
                    getOKAction(),
                    new NoFilterAction(),
                    getCancelAction(),
                    getHelpAction()
            };
        } else {
            return new Action[]{
                    getOKAction(),
                    getCancelAction(),
                    getHelpAction()
            };
        }
    }

    private class NoFilterAction extends AbstractAction {
        public NoFilterAction() {
            super("No Filter");
            //putValue(DEFAULT_ACTION, Boolean.FALSE);
        }

        public void actionPerformed(ActionEvent e) {
            doNoFilterAction();
        }
    }

    @Nullable
    protected JComponent createCenterPanel() {
        return mainForm.getComponent();
    }

    public void doOKAction() {
        Project project = getProject();
        DBDataset dataset = getDataset();
        try {
            mainForm.applyChanges();
            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
            DatasetFilter activeFilter = mainForm.getSelectedFilter();
            if (activeFilter == null) {
                activeFilter = DatasetFilterManager.EMPTY_FILTER;
            }
            filterManager.setActiveFilter(dataset, activeFilter);
            mainForm.dispose();
        } catch (ConfigurationException e) {
            e.printStackTrace(); 
        }
        super.doOKAction();
        if (!isAutomaticPrompt) DatasetEditorManager.getInstance(project).reloadEditorData(dataset);
    }

    public void doCancelAction() {
        mainForm.resetChanges();
        super.doCancelAction();
    }

    public void doNoFilterAction() {
        mainForm.resetChanges();
        mainForm.dispose();
        DBDataset dataset = getDataset();
        Project project = getProject();
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter activeFilter = filterManager.getActiveFilter(dataset);
        if (activeFilter == null) {
            activeFilter = DatasetFilterManager.EMPTY_FILTER;
            filterManager.setActiveFilter(dataset, activeFilter);
        }
        close(OK_EXIT_CODE);
    }

    @Override
    public void dispose() {
        if (!isDisposed()) {
            super.dispose();
            mainForm.dispose();
        }
    }
}
