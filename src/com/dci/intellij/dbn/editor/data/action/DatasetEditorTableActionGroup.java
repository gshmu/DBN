package com.dci.intellij.dbn.editor.data.action;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.data.type.DBDataType;
import com.dci.intellij.dbn.data.type.GenericDataType;
import com.dci.intellij.dbn.editor.data.DatasetEditor;
import com.dci.intellij.dbn.editor.data.filter.ConditionJoinType;
import com.dci.intellij.dbn.editor.data.filter.ConditionOperator;
import com.dci.intellij.dbn.editor.data.filter.DatasetBasicFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilter;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterInput;
import com.dci.intellij.dbn.editor.data.filter.DatasetFilterManager;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import com.dci.intellij.dbn.object.action.NavigateToObjectAction;
import com.dci.intellij.dbn.object.action.ObjectNavigationListActionGroup;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAwareAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import static com.dci.intellij.dbn.editor.data.model.RecordStatus.MODIFIED;

public class DatasetEditorTableActionGroup extends DefaultActionGroup {
    private ColumnInfo columnInfo;
    private Object columnValue;
    boolean isHeaderAction;
    private DatasetEditor datasetEditor;
    public DatasetEditorTableActionGroup(DatasetEditor datasetEditor, @Nullable DatasetEditorModelCell cell, ColumnInfo columnInfo) {
        this.datasetEditor = datasetEditor;
        this.columnInfo = columnInfo;
        DatasetEditorTable table = datasetEditor.getEditorTable();

        isHeaderAction = cell == null;
        columnValue = cell == null ? null : cell.getUserValue();

        HideColumnAction hideColumnAction = new HideColumnAction();
        add(hideColumnAction);
        addSeparator();
        if (cell != null && cell.is(MODIFIED) && !cell.isLobValue()) {
            DataRevertAction revertChangesAction = new DataRevertAction(cell);
            add(revertChangesAction);
        }

        DefaultActionGroup filterActionGroup = new DefaultActionGroup("Filter", true);
        filterActionGroup.getTemplatePresentation().setIcon(Icons.DATASET_FILTER_NEW);
        //filterActionGroup.getTemplatePresentation().setIcon(Icons.DATASET_FILTER);
        filterActionGroup.add(new CreateFilterAction(false));
        filterActionGroup.addSeparator();
        if (columnValue != null ) filterActionGroup.add(new CreateFilterAction(true));
        DBDataType dataType = columnInfo.getDataType();
        String text = getClipboardContent((int) dataType.getLength());
        if (text != null) {
            filterActionGroup.add(new CreateClipboardFilterAction(text, false));
            if (dataType.getGenericDataType() == GenericDataType.LITERAL) {
                filterActionGroup.add(new CreateClipboardFilterAction(text, true));
            }
        }

        // show the instructions additional condition action in case the filter is basic,
        // the join is AND, and the column is not already present
        DatasetFilterManager filterManager = DatasetFilterManager.getInstance(table.getDataset().getProject());
        DatasetFilter activeFilter = filterManager.getActiveFilter(table.getDataset());
        if (activeFilter instanceof DatasetBasicFilter) {
            DatasetBasicFilter basicFilter = (DatasetBasicFilter) activeFilter;
            if (basicFilter.getJoinType() == ConditionJoinType.AND &&
                    !basicFilter.containsConditionForColumn(columnInfo.getName())) {
                filterActionGroup.addSeparator();
                filterActionGroup.add(new CreateAdditionalConditionAction());
            }
        }
        add(filterActionGroup);

        if (columnInfo.isSortable()) {
            DefaultActionGroup sortingActionGroup = new DefaultActionGroup("Sort", true);
            //sortingActionGroup.getTemplatePresentation().setIcon(Icons.COMMON_SORTING);
            sortingActionGroup.add(new SortAscendingAction());
            sortingActionGroup.add(new SortDescendingAction());
            add(sortingActionGroup);
/*
            add(new SortAscendingAction());
            add(new SortDescendingAction());
*/
        }

        DBDataset dataset = table.getDataset();
        DBColumn column = Failsafe.nn(dataset.getColumn(columnInfo.getName()));
        if (columnValue != null) {
            if (cell != null && column.isForeignKey()) {
                DatasetFilterInput filterInput = table.getModel().resolveForeignKeyRecord(cell);
                if (filterInput != null) {
                    add(new ReferencedRecordOpenAction(filterInput));
                }
            }
            if (column.isPrimaryKey()) {
                ReferencingRecordsOpenAction action = new ReferencingRecordsOpenAction(column, columnValue);
                action.setPopupLocation(table.getColumnHeaderLocation(column));
                add(action);
            }
        }

        addSeparator();

        DefaultActionGroup columnPropertiesActionGroup = new DefaultActionGroup("Column info", true);
        columnPropertiesActionGroup.add(new NavigateToObjectAction(column));
        for (DBObjectNavigationList navigationList : column.getNavigationLists()) {
            if (!navigationList.isLazy()) {
                add(new ObjectNavigationListActionGroup(column, navigationList, true));
            }
        }
        add(columnPropertiesActionGroup);
        addSeparator();

        add(new DataExportAction());
    }

    private static String getClipboardContent(int maxLength) {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transferable = clipboard.getContents(null);
            if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                String text = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                if (text == null) {
                    return null;
                } else {
                    text = text.trim();
                    if (text.length() == 0 || text.length() > maxLength) {
                        return null;
                    }
                    return text;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class HideColumnAction extends DumbAwareAction {
        private HideColumnAction() {
            super("Hide column");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DatasetEditorTable editorTable = datasetEditor.getEditorTable();
            int columnIndex = columnInfo.getColumnIndex();
            editorTable.hideColumn(columnIndex);
        }
    }

    private class SortAscendingAction extends DumbAwareAction {
        private SortAscendingAction() {
            super("Sort ascending", null, Icons.ACTION_SORT_ASC);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DatasetEditorTable editorTable = datasetEditor.getEditorTable();
            int modelColumnIndex = columnInfo.getColumnIndex();
            int tableColumnIndex = editorTable.convertColumnIndexToView(modelColumnIndex);
            editorTable.sort(tableColumnIndex, SortDirection.ASCENDING, false);
        }
    }

    private class SortDescendingAction extends DumbAwareAction {
        private SortDescendingAction() {
            super("Sort descending", null, Icons.ACTION_SORT_DESC);
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            DatasetEditorTable editorTable = datasetEditor.getEditorTable();
            int modelColumnIndex = columnInfo.getColumnIndex();
            int tableColumnIndex = editorTable.convertColumnIndexToView(modelColumnIndex);
            editorTable.sort(tableColumnIndex, SortDirection.DESCENDING, false);
        }
    }

    private class CreateFilterAction extends DumbAwareAction {
        private boolean filterByValue;
        private CreateFilterAction(boolean filterByValue) {
            super(filterByValue ? "Filter by this value" : "Filter by column...");
            this.filterByValue = filterByValue;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            DBDataset dataset = datasetEditor.getDataset();
            DatasetFilterManager datasetFilterManager = DatasetFilterManager.getInstance(dataset.getProject());
            Object value = filterByValue ? columnValue : null;
            datasetFilterManager.createBasicFilter(dataset, columnInfo.getName(), value, ConditionOperator.EQUAL, !filterByValue);
        }
    }

    private class CreateClipboardFilterAction extends DumbAwareAction {
        private String text;
        private boolean like;
        private CreateClipboardFilterAction(String text, boolean like) {
            super("Filter by clipboard value" + (like ? " (like)" : ""));
            this.text = text;
            this.like = like;
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            DBDataset dataset = datasetEditor.getDataset();
            DatasetFilterManager datasetFilterManager = DatasetFilterManager.getInstance(dataset.getProject());
            String value = like ? '%' + text + '%' : text;
            ConditionOperator operator = like ? ConditionOperator.LIKE : ConditionOperator.EQUAL;
            datasetFilterManager.createBasicFilter(dataset, columnInfo.getName(), value, operator, false);
        }
    }

    private class CreateAdditionalConditionAction extends DumbAwareAction {
        private CreateAdditionalConditionAction() {
            super(columnValue == null ?
                    "Add column to filter..." :
                    "Add this value to filter");
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            DBDataset dataset = datasetEditor.getDataset();
            DatasetFilterManager filterManager = DatasetFilterManager.getInstance(dataset.getProject());
            DatasetBasicFilter basicFilter = (DatasetBasicFilter) filterManager.getActiveFilter(dataset);
            filterManager.addConditionToFilter(basicFilter, dataset, columnInfo, columnValue, isHeaderAction);
        }
    }
}
