package com.dci.intellij.dbn.editor.data.state.sorting.ui;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.ui.GUIUtil;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.data.sorting.SortingState;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetEditorSortingForm extends DBNFormImpl<DatasetEditorSortingDialog>{
    private JPanel mainPanel;
    private JPanel sortingInstructionsPanel;
    private JPanel actionsPanel;
    private JPanel headerPanel;

    private DBObjectRef<DBDataset> datasetRef;
    private List<DatasetSortingColumnForm> sortingInstructionForms = new ArrayList<DatasetSortingColumnForm>();
    private SortingState sortingState;


    DatasetEditorSortingForm(DatasetEditorSortingDialog parentComponent, DatasetEditor datasetEditor) {
        super(parentComponent);
        DBDataset dataset = datasetEditor.getDataset();
        sortingState = datasetEditor.getEditorState().getSortingState();
        this.datasetRef = DBObjectRef.from(dataset);

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
        DBNHeaderForm headerForm = new DBNHeaderForm(dataset, this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
    }

    private class ColumnSelector extends ValueSelector<DBColumn> {
        ColumnSelector() {
            super(PlatformIcons.ADD_ICON, "Add Sorting Column...", null, ValueSelectorOption.HIDE_DESCRIPTION);
            addListener(new ValueSelectorListener<DBColumn>() {
                @Override
                public void selectionChanged(DBColumn oldValue, DBColumn newValue) {
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
                if (sortingColumnForm.getSortingInstruction().getColumnName().equalsIgnoreCase(value.getName())) {
                    return false;
                }
            }
            return true;
        }
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public DBDataset getDataset() {
        return datasetRef.get();
    }

    public void addSortingColumn(DBColumn column) {
        DBDataset dataset = datasetRef.get();
        if (dataset != null) {
            SortingInstruction datasetSortingInstruction = new SortingInstruction(column.getName(), SortDirection.ASCENDING);
            DatasetSortingColumnForm sortingInstructionForm = new DatasetSortingColumnForm(this, datasetSortingInstruction);
            sortingInstructionForms.add(sortingInstructionForm);
            sortingInstructionsPanel.add(sortingInstructionForm.getComponent());
            updateIndexes();
            GUIUtil.repaint(sortingInstructionsPanel);
        }
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
        DisposerUtil.dispose(sortingInstructionForm);

        GUIUtil.repaint(sortingInstructionsPanel);
    }

    public void applyChanges() {
        sortingState.clear();
        for (DatasetSortingColumnForm sortingColumnForm : sortingInstructionForms) {
            sortingState.addSortingInstruction(sortingColumnForm.getSortingInstruction());
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        for (DatasetSortingColumnForm sortingColumnForm : sortingInstructionForms) {
            sortingColumnForm.dispose();
        }
        sortingInstructionForms.clear();
    }

}
