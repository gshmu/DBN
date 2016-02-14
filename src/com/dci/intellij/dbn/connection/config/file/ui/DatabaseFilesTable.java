package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.table.FileBrowserTableCellEditor;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
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
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DatabaseFilesTable extends DBNTable<DatabaseFilesTableModel> {

    public DatabaseFilesTable(DatabaseFiles databaseFiles) {
        super(new DatabaseFilesTableModel(databaseFiles), true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        getSelectionModel().addListSelectionListener(selectionListener);
        setSelectionBackground(UIUtil.getTableBackground());
        setSelectionForeground(UIUtil.getTableForeground());
        setCellSelectionEnabled(true);
        setTableHeader(null);
        setDefaultRenderer(Object.class, new DatabaseFilesTableCellRenderer());
        getColumnModel().getColumn(0).setCellEditor(new FileBrowserTableCellEditor(new FileChooserDescriptor(true, true, false, false, false, false)));

        setFixedWidth(columnModel.getColumn(1), 100);

        addMouseListener(mouseListener);
    }

    public void setFilePaths(DatabaseFiles filesBundle) {
        super.setModel(new DatabaseFilesTableModel(filesBundle));
        setFixedWidth(columnModel.getColumn(1), 100);
    }

    void setFixedWidth(TableColumn tableColumn, int width) {
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {

        }
    };    
    
    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        Object value = getValueAtMouseLocation();
        if (value instanceof Color) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    

    ListSelectionListener selectionListener = new ListSelectionListener() {
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
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

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
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
        DatabaseFilesTableModel model = getModel();
        model.notifyListeners(0, model.getRowCount(), 0);
    }

    public Component prepareEditor(TableCellEditor editor, final int rowIndex, final int columnIndex) {
        Component component = super.prepareEditor(editor, rowIndex, columnIndex);
        if (component instanceof JTextField) {
            final JTextField textField = (JTextField) component;
            textField.setBorder(new EmptyBorder(0,3,0,0));
            new SimpleLaterInvocator() {
                @Override
                protected void execute() {
                    textField.selectAll();
                    textField.grabFocus();
                    //selectCell(rowIndex, columnIndex);

                }
            }.start();
        }
        return component;
    }

    public void insertRow() {
        stopCellEditing();
        int rowIndex = getSelectedRow();
        DatabaseFilesTableModel model = getModel();
        rowIndex = rowIndex == -1 ? 1 : rowIndex + 1;
        model.insertRow(rowIndex);
        getSelectionModel().setSelectionInterval(rowIndex, rowIndex);

        revalidate();
        repaint();
    }


    public void removeRow() {
        stopCellEditing();
        int selectedRow = getSelectedRow();
        DatabaseFilesTableModel model = getModel();
        model.removeRow(selectedRow);

        revalidate();
        repaint();

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
