package com.dci.intellij.dbn.common.environment.options.ui;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.dci.intellij.dbn.common.environment.EnvironmentTypeBundle;
import com.dci.intellij.dbn.common.ui.table.DBNEditableTable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.BooleanTableCellEditor;
import com.intellij.ui.BooleanTableCellRenderer;
import com.intellij.ui.ColorChooser;
import com.intellij.util.ui.UIUtil;

public class EnvironmentTypesEditorTable extends DBNEditableTable<EnvironmentTypesTableModel> {

    public EnvironmentTypesEditorTable(Project project, EnvironmentTypeBundle environmentTypes) {
        super(project, new EnvironmentTypesTableModel(project, environmentTypes), true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setSelectionBackground(UIUtil.getTableBackground());
        setSelectionForeground(UIUtil.getTableForeground());
        setCellSelectionEnabled(true);
        setDefaultRenderer(String.class, new EnvironmentTypesTableCellRenderer());
        setDefaultRenderer(Color.class, new EnvironmentTypesTableCellRenderer());
        setDefaultRenderer(Boolean.class, new BooleanTableCellRenderer());
        setDefaultEditor(Boolean.class, new BooleanTableCellEditor());

        setFixedWidth(columnModel.getColumn(2), 100);
        setFixedWidth(columnModel.getColumn(3), 100);
        setFixedWidth(columnModel.getColumn(4), 60);

        addMouseListener(mouseListener);
    }

    public void setEnvironmentTypes(EnvironmentTypeBundle environmentTypes) {
        super.setModel(new EnvironmentTypesTableModel(getProject(), environmentTypes));
        setFixedWidth(columnModel.getColumn(2), 100);
        setFixedWidth(columnModel.getColumn(3), 100);
        setFixedWidth(columnModel.getColumn(4), 60);
    }

    void setFixedWidth(TableColumn tableColumn, int width) {
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }

    MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                Point point = e.getPoint();
                int columnIndex = columnAtPoint(point);
                if (columnIndex == 4) {
                    int rowIndex = rowAtPoint(point);
                    Color color = (Color) getValueAt(rowIndex, columnIndex);
                    color = ColorChooser.chooseColor(EnvironmentTypesEditorTable.this, "Select Environment Color", color);
                    if (color != null) {
                        setValueAt(color, rowIndex, columnIndex);
                    }
                }
            }
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

    @Override
    public boolean isCellEditable(int row, int column) {
        return column < 4;
    }
}
