package com.dci.intellij.dbn.editor.data.filter.ui;

import com.dci.intellij.dbn.common.dispose.DisposableContainer;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterGroup;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterImpl;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.filter.action.CreateFilterAction;
import com.dci.intellij.dbn.editor.data.filter.action.DeleteFilterAction;
import com.dci.intellij.dbn.editor.data.filter.action.MoveFilterDownAction;
import com.dci.intellij.dbn.editor.data.filter.action.MoveFilterUpAction;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.GuiUtils;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class DatasetFilterForm extends ConfigurationEditorForm<DatasetFilterGroup> implements ListSelectionListener {
    private final Map<String, ConfigurationEditorForm> filterDetailPanels = DisposableContainer.map(this);
    private static final String BLANK_PANEL_ID = "BLANK_PANEL";

    private JPanel mainPanel;
    private JList filtersList;
    private JPanel filterDetailsPanel;
    private JPanel actionsPanel;
    private JPanel headerPanel;

    public DatasetFilterForm(DatasetFilterGroup filterGroup, @NotNull DBDataset dataset) {
        super(filterGroup);
        filtersList.setModel(filterGroup);
        filtersList.setFont(UIUtil.getLabelFont());
        Project project = dataset.getProject();

        DBNHeaderForm headerForm = new DBNHeaderForm(this, dataset);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        DatasetFilterList filters = getFilterList();
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.DataEditor.FiltersList", true,
                new CreateFilterAction(filters),
                new DeleteFilterAction(filters),
                new MoveFilterUpAction(filters),
                new MoveFilterDownAction(filters));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
        filterDetailsPanel.add(new JPanel(), BLANK_PANEL_ID);

        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(project);
        DatasetFilter filter = filterManager.getActiveFilter(dataset);
        if (filter != null) {
            filtersList.setSelectedValue(filter, true);
        }
        valueChanged(null);
        GuiUtils.replaceJSplitPaneWithIDEASplitter(mainPanel);
        filtersList.addListSelectionListener(this);
    }

    public DatasetFilterList getFilterList() {
        return (DatasetFilterList) filtersList;
    }

    public DatasetFilter getSelectedFilter() {
        return (DatasetFilter) filtersList.getSelectedValue();
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @Override
    public void applyFormChanges() throws ConfigurationException {
        getFilterList().getFilterGroup().apply();
    }

    @Override
    public void resetFormChanges() {
        getFilterList().getFilterGroup().reset();
    }

    private void createUIComponents() {
        filtersList = new DatasetFilterList();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        DatasetFilterGroup configuration = getConfiguration();
        if (e == null || !e.getValueIsAdjusting()) {
            int[] indices = filtersList.getSelectedIndices();
            List<DatasetFilter> filters = configuration.getFilters();
            DatasetFilterImpl filter = null;
            int filtersCount = filters.size();
            if (filtersCount > 0 && indices.length == 1) {
                if (filtersCount > indices[0]) {
                    filter = (DatasetFilterImpl) filters.get(indices[0]);
                }
            }

            CardLayout cardLayout = (CardLayout) filterDetailsPanel.getLayout();
            if (filter == null) {
                cardLayout.show(filterDetailsPanel, BLANK_PANEL_ID);
            } else {
                String id = filter.getId();
                ConfigurationEditorForm configurationEditorForm = filterDetailPanels.get(id);
                if (configurationEditorForm == null) {
                    JComponent component = filter.createComponent();
                    filterDetailsPanel.add(component, id);

                    configurationEditorForm = filter.ensureSettingsEditor();
                    filterDetailPanels.put(id, configurationEditorForm);

                    Disposer.register(this, configurationEditorForm);
                }
                cardLayout.show(filterDetailsPanel, id);
                configurationEditorForm.focus();
            }
        }
    }
}
