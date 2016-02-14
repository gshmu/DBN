package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TableUtil;
import com.intellij.util.ui.UIUtil;

import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;

public class DBNEditableTable<T extends DBNEditableTableModel> extends DBNTableWithGutter<T> {

    public DBNEditableTable(Project project, T model, boolean showHeader) {
        super(project, model, showHeader);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getSelectionModel().addListSelectionListener(selectionListener);
        setSelectionBackground(UIUtil.getTableBackground());
        setSelectionForeground(UIUtil.getTableForeground());
        setCellSelectionEnabled(true);
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        Object value = getValueAtMouseLocation();
        if (value instanceof Color) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    

    private ListSelectionListener selectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting() && getSelectedRowCount() == 1) {
                startCellEditing();
            }
        }
    };

    public void columnSelectionChanged(ListSelectionEvent e) {
        super.columnSelectionChanged(e);
        JTableHeader tableHeader = getTableHeader();
        if (tableHeader != null && tableHeader.getDraggedColumn() == null) {
            if (!e.getValueIsAdjusting()) {
                startCellEditing();
            }
        }
    }

    private void startCellEditing() {
        if (getModel().getRowCount() > 0) {
            int[] selectedRows = getSelectedRows();
            int selectedRow = selectedRows.length > 0 ? selectedRows[0] : 0;

            int[] selectedColumns = getSelectedColumns();
            int selectedColumn = selectedColumns.length > 0 ? selectedColumns[0] : 0;

            editCellAt(selectedRow, selectedColumn);
        }
    }

    @Override
    public void editingStopped(ChangeEvent e) {
        super.editingStopped(e);
        T model = getModel();
        model.notifyListeners(0, model.getRowCount(), 0);
    }

    public Component prepareEditor(TableCellEditor editor, int rowIndex, int columnIndex) {
        final Component component = super.prepareEditor(editor, rowIndex, columnIndex);
        if (component instanceof JTextField) {
            final JTextField textField = (JTextField) component;
            textField.setBorder(new EmptyBorder(0,3,0,0));

            //selectCell(rowIndex, columnIndex);

            new SimpleLaterInvocator() {
                protected void execute() {
                    component.requestFocus();
                    textField.selectAll();
                }
            }.start();
        }
        return component;
    }

    public void insertRow() {
        stopCellEditing();
        int rowIndex = getSelectedRow();
        T model = getModel();
        rowIndex = model.getRowCount() == 0 ? 0 : rowIndex + 1;
        model.insertRow(rowIndex);
        resizeAndRepaint();
        getSelectionModel().setSelectionInterval(rowIndex, rowIndex);
    }


    public void removeRow() {
        stopCellEditing();
        int selectedRow = getSelectedRow();
        T model = getModel();
        model.removeRow(selectedRow);
        resizeAndRepaint();

        if (model.getRowCount() == selectedRow && selectedRow > 0) {
            getSelectionModel().setSelectionInterval(selectedRow -1, selectedRow -1);
        }
    }

    public void moveRowUp() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();

        TableUtil.moveSelectedItemsUp(this);
        selectCell(selectedRow -1, selectedColumn);
    }

    public void moveRowDown() {
        int selectedRow = getSelectedRow();
        int selectedColumn = getSelectedColumn();

        TableUtil.moveSelectedItemsDown(this);
        selectCell(selectedRow + 1, selectedColumn);
    }
}
