package com.dci.intellij.dbn.editor.data.ui.table.renderer;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.data.grid.color.BasicTableTextAttributes;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableCellRenderer;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorColumnInfo;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelCell;
import com.dci.intellij.dbn.editor.data.model.DatasetEditorModelRow;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.border.Border;
import java.awt.*;

import static com.dci.intellij.dbn.editor.data.model.RecordStatus.*;

public class DatasetEditorTableCellRenderer extends BasicTableCellRenderer {

    @Override
    protected void customizeCellRenderer(DBNTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        acquireState(table, isSelected, false, rowIndex, columnIndex);
        DatasetEditorModelCell cell = (DatasetEditorModelCell) value;
        DatasetEditorTable datasetEditorTable = (DatasetEditorTable) table;

        if (Failsafe.check(cell, datasetEditorTable, datasetEditorTable.getProject())) {
            DatasetEditorModelRow row = cell.getRow();
            DatasetEditorColumnInfo columnInfo = cell.getColumnInfo();
            boolean isDirty = datasetEditorTable.getModel().isDirty();
            boolean isLoading = datasetEditorTable.isLoading();
            boolean isInserting = datasetEditorTable.isInserting();

            boolean isDeletedRow = row.is(DELETED);
            boolean isInsertRow = row.is(INSERTING);
            boolean isCaretRow = !isInsertRow && table.getCellSelectionEnabled() && table.getSelectedRow() == rowIndex && table.getSelectedRowCount() == 1;
            boolean isModified = cell.is(MODIFIED);
            boolean isTrackingColumn = columnInfo.isTrackingColumn();
            boolean isConnected = Failsafe.nn(datasetEditorTable.getDatasetEditor().getConnection()).isConnected();

            BasicTableTextAttributes attributes = (BasicTableTextAttributes) getAttributes();
            SimpleTextAttributes textAttributes = attributes.getPlainData(isModified, isCaretRow);

            if (isSelected) {
                textAttributes = attributes.getSelection();
            } else {
                if (isLoading || isDirty || !isConnected) {
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
                } else if (isTrackingColumn) {
                    textAttributes = attributes.getTrackingData(isModified, isCaretRow);
                }
            }

            Color background = Commons.nvl(textAttributes.getBgColor(), table.getBackground());
            Color foreground = Commons.nvl(textAttributes.getFgColor(), table.getForeground());


            Border border = Borders.lineBorder(background);

            if (cell.hasError() && isConnected) {
                border = Borders.lineBorder(SimpleTextAttributes.ERROR_ATTRIBUTES.getFgColor());
                SimpleTextAttributes errorData = attributes.getErrorData();
                background = errorData.getBgColor();
                foreground = errorData.getFgColor();
                textAttributes = textAttributes.derive(errorData.getStyle(), foreground, background, null);
            } else if (isTrackingColumn && !isSelected) {
                SimpleTextAttributes trackingDataAttr = attributes.getTrackingData(isModified, isCaretRow);
                foreground = Commons.nvl(trackingDataAttr.getFgColor(), foreground);
                textAttributes = textAttributes.derive(textAttributes.getStyle(), foreground, background, null);
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
                                                                