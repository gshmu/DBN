package com.dci.intellij.dbn.editor.data.state.sorting.ui;

import com.dci.intellij.dbn.common.dispose.DisposableContainer;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetEditorSortingForm extends DBNFormImpl{
    private JPanel mainPanel;
    private JPanel sortingInstructionsPanel;
    private JPanel actionsPanel;
    private JPanel headerPanel;

    private final DBObjectRef<DBDataset> dataset;
    private final List<DatasetSortingColumnForm> sortingInstructionForms = DisposableContainer.concurrentList(this);
    private final SortingState sortingState;


    DatasetEditorSortingForm(DatasetEditorSortingDialog parentComponent, DatasetEditor datasetEditor) {
        super(parentComponent);
        DBDataset dataset = datasetEditor.getDataset();
        sortingState = datasetEditor.getEditorState().getSortingState();
        this.dataset = DBObjectRef.of(dataset);

        BoxLayout sortingInstructionsPanelLayout = new BoxLayout(sortingInstructionsPanel, BoxLayout.Y_AXIS);
        sortingInstructionsPanel.setLayout(sortingInstructionsPanelLayout);

        for (SortingInstruction sortingInstruction : sortingState.getSortingInstructions()) {
            DBColumn column = sortingInstruction.getColumn(dataset);
            if (column != null) {
                DatasetSortingColumnForm sortingInstructionForm = new DatasetSortingColumnForm(this, sortingInstruction.clone());
                sortingInstructionForms.add(sortingInstructionForm);
                sortingInstructionsPanel.add(sortingInstructionForm.getComponent());
            }
        }
        updateIndexes();

        actionsPanel.add(new ColumnSelector(), BorderLayout.CENTER);
        createHeaderForm(dataset);

/*
        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.DataEditor.Sorting.Add", true,
                new AddSortingColumnAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.EAST);
*/
    }

    List<DatasetSortingColumnForm> getSortingInstructionForms() {
        return sortingInstructionForms;
    }

    private void createHeaderForm(DBDataset dataset) {
        DBNHeaderForm headerForm = new DBNHeaderForm(this, dataset);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
    }

    private class ColumnSelector extends ValueSelector<DBColumn> {
        ColumnSelector() {
            super(PlatformIcons.ADD_ICON, "Add Sorting Column...", null, ValueSelectorOption.HIDE_DESCRIPTION);
            addListener((oldValue, newValue) -> {
                if (newValue != null) {
                    addSortingColumn(newValue);
                    resetValues();
                }
            });
        }

        @Override
        public List<DBColumn> loadValues() {
            DBDataset dataset = getDataset();
            List<DBColumn> columns = new ArrayList<DBColumn>(dataset.getColumns());
            Collections.sort(columns);
            return columns;
        }

        @Override
        public boolean isVisible(DBColumn value) {
            for (DatasetSortingColumnForm sortingColumnForm : sortingInstructionForms) {
                String columnName = sortingColumnForm.getSortingInstruction().getColumnName();
                if (Strings.equalsIgnoreCase(columnName, value.getName())) {
                    return false;
                }
            }
            return true;
        }
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    @NotNull
    public DBDataset getDataset() {
        return dataset.ensure();
    }

    public void addSortingColumn(DBColumn column) {
        SortingInstruction datasetSortingInstruction = new SortingInstruction(column.getName(), SortDirection.ASCENDING);
        DatasetSortingColumnForm sortingInstructionForm = new DatasetSortingColumnForm(this, datasetSortingInstruction);
        sortingInstructionForms.add(sortingInstructionForm);
        sortingInstructionsPanel.add(sortingInstructionForm.getComponent());
        updateIndexes();
        GUIUtil.repaint(sortingInstructionsPanel);
    }

    private void updateIndexes() {
        for (int i=0; i<sortingInstructionForms.size(); i++) {
            sortingInstructionForms.get(i).setIndex(i + 1);
        }
    }


    public void removeSortingColumn(DatasetSortingColumnForm sortingInstructionForm) {
        sortingInstructionsPanel.remove(sortingInstructionForm.getComponent());
        sortingInstructionForms.remove(sortingInstructionForm);
        updateIndexes();
        Disposer.dispose(sortingInstructionForm);

        GUIUtil.repaint(sortingInstructionsPanel);
    }

    public void applyChanges() {
        sortingState.clear();
        for (DatasetSortingColumnForm sortingColumnForm : sortingInstructionForms) {
            sortingState.addSortingInstruction(sortingColumnForm.getSortingInstruction());
        }
    }
}
