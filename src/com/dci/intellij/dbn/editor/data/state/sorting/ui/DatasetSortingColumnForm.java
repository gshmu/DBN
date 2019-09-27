package com.dci.intellij.dbn.editor.data.state.sorting.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.DBNComboBox;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.ValueSelector;
import com.dci.intellij.dbn.common.ui.ValueSelectorListener;
import com.dci.intellij.dbn.common.ui.ValueSelectorOption;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.sorting.SortingInstruction;
import com.dci.intellij.dbn.editor.data.state.sorting.action.ChangeSortingDirectionAction;
import com.dci.intellij.dbn.editor.data.state.sorting.action.DeleteSortingCriteriaAction;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DatasetSortingColumnForm extends DBNFormImpl<DatasetEditorSortingForm> {
    private JPanel actionsPanel;
    private JPanel mainPanel;
    private JLabel indexLabel;
    private JLabel dataTypeLabel;
    private DBNComboBox<DBColumn> columnComboBox;

    private SortingInstruction sortingInstruction;

    DatasetSortingColumnForm(DatasetEditorSortingForm parent, @NotNull SortingInstruction sortingInstruction) {
        super(parent);
        this.sortingInstruction = sortingInstruction;

        DBDataset dataset = parent.getDataset();
        DBColumn column = sortingInstruction.getColumn(dataset);
        columnComboBox.setValues(dataset.getColumns());
        columnComboBox.setSelectedValue(column);
        columnComboBox.set(ValueSelectorOption.HIDE_DESCRIPTION, true);
        columnComboBox.setBackground(UIUtil.getTextFieldBackground());
        dataTypeLabel.setText(column.getDataType().getQualifiedName());
        dataTypeLabel.setForeground(UIUtil.getInactiveTextColor());

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.DataEditor.Sorting.Instruction", true,
                new ChangeSortingDirectionAction(this),
                new DeleteSortingCriteriaAction(this));
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.CENTER);
    }

    private class ColumnSelector extends ValueSelector<DBColumn>{
        ColumnSelector(DBColumn selectedColumn) {
            super(Icons.DBO_COLUMN_HIDDEN, "Select column...", selectedColumn, ValueSelectorOption.HIDE_DESCRIPTION);
            addListener(new ValueSelectorListener<DBColumn>() {
                @Override
                public void selectionChanged(DBColumn oldValue, DBColumn newValue) {
                    sortingInstruction.setColumnName(newValue.getName());
                    dataTypeLabel.setText(newValue.getDataType().getQualifiedName());
                }
            });
        }

        @Override
        public List<DBColumn> loadValues() {
            DBDataset dataset = ensureParentComponent().getDataset();
            List<DBColumn> columns = new ArrayList<DBColumn>(dataset.getColumns());
            Collections.sort(columns);
            return columns;
        }

        @Override
        public boolean isVisible(DBColumn value) {
            List<DatasetSortingColumnForm> sortingInstructionForms = ensureParentComponent().getSortingInstructionForms();
            for (DatasetSortingColumnForm sortingColumnForm : sortingInstructionForms) {
                String columnName = sortingColumnForm.getSortingInstruction().getColumnName();
                if (StringUtil.equalsIgnoreCase(columnName, value.getName())) {
                    return false;
                }
            }
            return true;

        }
    }

    public void setIndex(int index) {
        sortingInstruction.setIndex(index);
        indexLabel.setText(Integer.toString(index));
    }

    @NotNull
    @Override
    public JPanel ensureComponent() {
        return mainPanel;
    }

    public SortingInstruction getSortingInstruction() {
        return sortingInstruction;
    }

    public void remove() {
        ensureParentComponent().removeSortingColumn(this);
    }

    public DBDataset getDataset() {
        return ensureParentComponent().getDataset();
    }
}
