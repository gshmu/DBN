package com.dci.intellij.dbn.editor.data.ui.table.renderer;

import javax.swing.JTable;
import javax.swing.border.Border;
import java.awt.Color;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.data.grid.color.DataGridTextAttributes;
import com.dci.intellij.dbn.data.grid.options.DataGridTrackingColumnSettings;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableCellRenderer;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorColumnInfo;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.ui.SimpleTextAttributes;

public class DatasetEditorTableCellRenderer extends BasicTableCellRenderer {

    protected void customizeCellRenderer(JTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        acquireState(table, isSelected, false, rowIndex, columnIndex);
        DatasetEditorModelCell cell = (DatasetEditorModelCell) value;
        DatasetEditorTable datasetEditorTable = (DatasetEditorTable) table;

        if (cell != null && !cell.isDisposed() && !datasetEditorTable.isDisposed() && !datasetEditorTable.getProject().isDisposed()) {
            DatasetEditorModelRow row = cell.getRow();
            DatasetEditorColumnInfo columnInfo = cell.getColumnInfo();
            boolean isLoading = datasetEditorTable.isLoading();
            boolean isInserting = datasetEditorTable.isInserting();

            boolean isDeletedRow = row.isDeleted();
            boolean isInsertRow = row.isInsert();
            boolean isCaretRow = !isInsertRow && table.getCellSelectionEnabled() && table.getSelectedRow() == rowIndex && table.getSelectedRowCount() == 1;
            boolean isModified = cell.isModified();

            DataGridTextAttributes attributes = getAttributes();
            SimpleTextAttributes textAttributes = attributes.getPlainData(isModified, isCaretRow);

            if (isSelected) {
                textAttributes = attributes.getSelection();
            } else if (isLoading || !datasetEditorTable.getDatasetEditor().getConnectionHandler().isConnected()) {
                textAttributes = attributes.getLoadingData(isCaretRow);
            } else if (isDeletedRow) {
                textAttributes = attributes.getDeletedData();
            } else if ((isInserting && !isInsertRow)) {
                textAttributes = attributes.getReadonlyData(isModified, isCaretRow);
            } else if (columnInfo.isPrimaryKey()) {
                textAttributes = attributes.getPrimaryKey(isModified, isCaretRow);
            } else if (columnInfo.isForeignKey()) {
                textAttributes = attributes.getForeignKey(isModified, isCaretRow);
            } else if (cell.isLobValue() || cell.isArrayValue()) {
                textAttributes = attributes.getReadonlyData(isModified, isCaretRow);
            } else {
                DataGridTrackingColumnSettings trackingColumnSettings = datasetEditorTable.getDataGridSettings().getTrackingColumnSettings();
                boolean trackingColumn = trackingColumnSettings.isTrackingColumn(columnInfo.getName());
                if (trackingColumn) {
                    textAttributes = attributes.getTrackingData(isModified, isCaretRow);
                }
            }

            Color background = CommonUtil.nvl(textAttributes.getBgColor(), table.getBackground());
            Color foreground = CommonUtil.nvl(textAttributes.getFgColor(), table.getForeground());


            Border border = getLineBorder(background);

            if (cell.hasError()) {
                border = getLineBorder(SimpleTextAttributes.ERROR_ATTRIBUTES.getFgColor());
                SimpleTextAttributes errorData = attributes.getErrorData();
                background = errorData.getBgColor();
                foreground = errorData.getFgColor();
                textAttributes = textAttributes.derive(errorData.getStyle(), foreground, background, null);
            }

            setBorder(border);
            setBackground(background);
            setForeground(foreground);
            writeUserValue(cell, textAttributes, attributes);
        }
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
    }
}
                                                                