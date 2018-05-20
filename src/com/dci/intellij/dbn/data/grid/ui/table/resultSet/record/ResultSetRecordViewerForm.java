package com.dci.intellij.dbn.data.grid.ui.table.resultSet.record;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.dispose.FailsafeUtil;
import com.dci.intellij.dbn.common.ui.DBNFormImpl;
import com.dci.intellij.dbn.common.ui.DBNHeaderForm;
import com.dci.intellij.dbn.common.util.ActionUtil;
import com.dci.intellij.dbn.data.grid.ui.table.resultSet.ResultSetTable;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModel;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelCell;
import com.dci.intellij.dbn.data.model.resultSet.ResultSetDataModelRow;
import com.dci.intellij.dbn.data.record.ColumnSortingType;
import com.dci.intellij.dbn.data.record.RecordViewInfo;
import com.dci.intellij.dbn.editor.data.DatasetEditorManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ResultSetRecordViewerForm extends DBNFormImpl<ResultSetRecordViewerDialog> {
    private JPanel actionsPanel;
    private JPanel columnsPanel;
    private JPanel mainPanel;
    private JScrollPane columnsPanelScrollPane;
    private JPanel headerPanel;

    private List<ResultSetRecordViewerColumnForm> columnForms = new ArrayList<ResultSetRecordViewerColumnForm>();

    private ResultSetTable table;
    private ResultSetDataModelRow row;

    public ResultSetRecordViewerForm(ResultSetRecordViewerDialog parentComponent, ResultSetTable<? extends ResultSetDataModel> table, boolean showDataTypes) {
        super(parentComponent);
        this.table = table;
        ResultSetDataModel model = table.getModel();
        row = (ResultSetDataModelRow) model.getRowAtIndex(table.getSelectedRow());
        RecordViewInfo recordViewInfo = table.getRecordViewInfo();

        // HEADER
        String headerTitle = recordViewInfo.getTitle();
        Icon headerIcon = recordViewInfo.getIcon();
        Color headerBackground = UIUtil.getPanelBackground();
        Project project = getProject();
        if (getEnvironmentSettings(project).getVisibilitySettings().getDialogHeaders().value()) {
            headerBackground = model.getConnectionHandler().getEnvironmentType().getColor();
        }
        DBNHeaderForm headerForm = new DBNHeaderForm(
                headerTitle,
                headerIcon,
                headerBackground,
                this);
        headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);

        ActionToolbar actionToolbar = ActionUtil.createActionToolbar(
                "DBNavigator.Place.DataEditor.TextAreaPopup", true,
                new SortAlphabeticallyAction(),
                ActionUtil.SEPARATOR,
                new FirstRecordAction(),
                new PreviousRecordAction(),
                new NextRecordAction(),
                new LastRecordAction());
        actionsPanel.add(actionToolbar.getComponent(), BorderLayout.WEST);


        columnsPanel.setLayout(new BoxLayout(columnsPanel, BoxLayout.Y_AXIS));

        ResultSetDataModelRow row = getRow();
        for (Object cell: row.getCells()) {
            ResultSetRecordViewerColumnForm columnForm = new ResultSetRecordViewerColumnForm(this, (ResultSetDataModelCell) cell, showDataTypes);
            columnForms.add(columnForm);
        }
        ColumnSortingType columnSortingType = DatasetEditorManager.getInstance(project).getRecordViewColumnSortingType();
        sortColumns(columnSortingType);

        int[] metrics = new int[]{0, 0};
        for (ResultSetRecordViewerColumnForm columnForm : columnForms) {
            metrics = columnForm.getMetrics(metrics);
        }

        for (ResultSetRecordViewerColumnForm columnForm : columnForms) {
            columnForm.adjustMetrics(metrics);
        }

        Dimension preferredSize = mainPanel.getPreferredSize();
        int width = (int) preferredSize.getWidth() + 24;
        int height = (int) Math.min(preferredSize.getHeight(), 380);
        mainPanel.setPreferredSize(new Dimension(width, height));


        int scrollUnitIncrement = (int) columnForms.get(0).getComponent().getPreferredSize().getHeight();
        columnsPanelScrollPane.getVerticalScrollBar().setUnitIncrement(scrollUnitIncrement);
    }

    public JComponent getPreferredFocusedComponent() {
        return null;//columnForms.get(0).getViewComponent();
    }

    public JPanel getComponent() {
        return mainPanel;
    }

    public JComponent getColumnsPanel() {
        return columnsPanel;
    }

    public void setRow(ResultSetDataModelRow row) {
        this.row = row;
        for (Object o : row.getCells()) {
            ResultSetDataModelCell cell = (ResultSetDataModelCell) o;
            ResultSetRecordViewerColumnForm columnForm = getColumnPanel(cell.getColumnInfo());
            if (columnForm != null) {
                columnForm.setCell(cell);
            }
        }
    }

    private ResultSetRecordViewerColumnForm getColumnPanel(ColumnInfo columnInfo) {
        for (ResultSetRecordViewerColumnForm columnForm : columnForms) {
            if (columnForm.getCell().getColumnInfo() == columnInfo) {
                return columnForm;
            }
        }
        return null;
    }

    /*********************************************************
     *                   Column sorting                      *
     *********************************************************/
    private void sortColumns(ColumnSortingType sortingType) {
        Comparator<ResultSetRecordViewerColumnForm> comparator =
                sortingType == ColumnSortingType.ALPHABETICAL ? alphabeticComparator :
                sortingType == ColumnSortingType.BY_INDEX ? indexedComparator : null;

        if (comparator != null) {
            Collections.sort(columnForms, comparator);
            columnsPanel.removeAll();
            for (ResultSetRecordViewerColumnForm columnForm : columnForms) {
                columnsPanel.add(columnForm.getComponent());
            }

            columnsPanel.revalidate();
            columnsPanel.repaint();
        }
    }

    private static Comparator<ResultSetRecordViewerColumnForm> alphabeticComparator = new Comparator<ResultSetRecordViewerColumnForm>() {
        public int compare(ResultSetRecordViewerColumnForm columnPanel1, ResultSetRecordViewerColumnForm columnPanel2) {
            String name1 = columnPanel1.getCell().getColumnInfo().getName();
            String name2 = columnPanel2.getCell().getColumnInfo().getName();
            return name1.compareTo(name2);
        }
    };

    private static Comparator<ResultSetRecordViewerColumnForm> indexedComparator = new Comparator<ResultSetRecordViewerColumnForm>() {
        public int compare(ResultSetRecordViewerColumnForm columnPanel1, ResultSetRecordViewerColumnForm columnPanel2) {
            int index1 = columnPanel1.getCell().getColumnInfo().getColumnIndex();
            int index2 = columnPanel2.getCell().getColumnInfo().getColumnIndex();
            return index1-index2;
        }
    };

    public void focusNextColumnPanel(ResultSetRecordViewerColumnForm source) {
        int index = columnForms.indexOf(source);
        if (index < columnForms.size() - 1) {
            ResultSetRecordViewerColumnForm columnForm = columnForms.get(index + 1);
            columnForm.getViewComponent().requestFocus();
        }
    }

    public void focusPreviousColumnPanel(ResultSetRecordViewerColumnForm source) {
        int index = columnForms.indexOf(source);
        if (index > 0) {
            ResultSetRecordViewerColumnForm columnForm = columnForms.get(index - 1);
            columnForm.getViewComponent().requestFocus();
        }
    }

    /*********************************************************      
     *                       Actions                         *
     *********************************************************/
    private class SortAlphabeticallyAction extends ToggleAction {
        private SortAlphabeticallyAction() {
            super("Sort columns alphabetically", null, Icons.ACTION_SORT_ALPHA);
        }

        public boolean isSelected(AnActionEvent anActionEvent) {
            Project project = getProject();
            ColumnSortingType sortingType = DatasetEditorManager.getInstance(project).getRecordViewColumnSortingType();
            return sortingType == ColumnSortingType.ALPHABETICAL;
        }

        public void setSelected(AnActionEvent anActionEvent, boolean selected) {
            ColumnSortingType sortingType = selected ? ColumnSortingType.ALPHABETICAL : ColumnSortingType.BY_INDEX;
            Project project = getProject();
            DatasetEditorManager.getInstance(project).setRecordViewColumnSortingType(sortingType);
            sortColumns(sortingType);
        }
    }

    private class FirstRecordAction extends AnAction {
        private FirstRecordAction() {
            super("First Record", null, Icons.DATA_EDITOR_FIRST_RECORD);
        }

        public void actionPerformed(AnActionEvent e) {
            ResultSetDataModelRow row = getRow();
            ResultSetDataModelRow firstRow = (ResultSetDataModelRow) row.getModel().getRowAtIndex(0);
            setRow(firstRow);
            table.selectRow(0);
        }

        @Override
        public void update(AnActionEvent anactionevent) {
            ResultSetDataModelRow row = getRow();
            anactionevent.getPresentation().setEnabled(row.getIndex() > 0);
        }
    }

    private class PreviousRecordAction extends AnAction {
        private PreviousRecordAction() {
            super("Previous Record", null, Icons.DATA_EDITOR_PREVIOUS_RECORD);
        }

        public void actionPerformed(AnActionEvent e) {
            ResultSetDataModelRow row = getRow();
            int index = row.getIndex();
            if (index > 0) {
                index--;
                ResultSetDataModelRow previousRow = (ResultSetDataModelRow) row.getModel().getRowAtIndex(index);
                if (previousRow != null) {
                    setRow(previousRow);
                    table.selectRow(index);
                }
            }
        }

        @Override
        public void update(AnActionEvent anactionevent) {
            ResultSetDataModelRow row = getRow();
            anactionevent.getPresentation().setEnabled(row.getIndex() > 0);
        }
    }

    private class NextRecordAction extends AnAction {
        private NextRecordAction() {
            super("Next record", null, Icons.DATA_EDITOR_NEXT_RECORD);
        }

        public void actionPerformed(AnActionEvent e) {
            ResultSetDataModelRow row = getRow();
            ResultSetDataModel model = row.getModel();
            if (row.getIndex() < model.getRowCount() -1) {
                int index = row.getIndex() + 1;
                ResultSetDataModelRow nextRow = (ResultSetDataModelRow) model.getRowAtIndex(index);
                setRow(nextRow);
                table.selectRow(index);
            }
        }

        @Override
        public void update(AnActionEvent anactionevent) {
            ResultSetDataModelRow row = getRow();
            anactionevent.getPresentation().setEnabled(row.getIndex() < row.getModel().getRowCount() -1);
        }
    }

    private class LastRecordAction extends AnAction {
        private LastRecordAction() {
            super("Last record", null, Icons.DATA_EDITOR_LAST_RECORD);
        }

        public void actionPerformed(AnActionEvent e) {
            ResultSetDataModel model = getRow().getModel();
            int index = model.getRowCount() - 1 ;
            ResultSetDataModelRow lastRow = (ResultSetDataModelRow) model.getRowAtIndex(index);
            setRow(lastRow);
            table.selectRow(index);
        }

        @Override
        public void update(AnActionEvent anactionevent) {
            ResultSetDataModelRow row = getRow();
            anactionevent.getPresentation().setEnabled(row.getIndex() < row.getModel().getRowCount() -1);
        }
    }

    @NotNull
    public ResultSetDataModelRow getRow() {
        return FailsafeUtil.get(row);
    }

    public void dispose() {
        super.dispose();
        DisposerUtil.dispose(columnForms);
        columnForms = null;
        table = null;
        row = null;
    }
}
