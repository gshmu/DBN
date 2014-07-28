package com.dci.intellij.dbn.common.ui.list;

import javax.swing.JTable;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.dci.intellij.dbn.common.ui.table.DBNEditableTable;
import com.dci.intellij.dbn.common.ui.table.DBNEditableTableModel;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.ui.UIUtil;

public class EditableStringList extends DBNEditableTable<EditableStringList.EditableListModel> {
    private Collection<String> stringValues;

    public EditableStringList(Project project, List<String> elements) {
        super(project, new EditableListModel(elements), false);
        setTableHeader(null);

/*
        getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (getSelectedRowCount() == 1 && !e.getValueIsAdjusting()) {
                    new SimpleLaterInvocator() {
                        @Override
                        public void execute() {
                            editCellAt(getSelectedRow(), 0);
                        }
                    }.start();

                }
            }
        });
*/

        setDefaultRenderer(String.class, new ColoredTableCellRenderer() {
            @Override
            protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
                acquireState(table, false, false, row, column);
                Color background = table.getBackground();
                Color foreground = table.getForeground();
                SimpleTextAttributes attributes = SimpleTextAttributes.SIMPLE_CELL_ATTRIBUTES;
                if (selected && !table.isEditing()) {
                    background = UIUtil.getListSelectionBackground();
                    foreground = UIUtil.getListSelectionForeground();
                    attributes = SimpleTextAttributes.SELECTED_SIMPLE_CELL_ATTRIBUTES;

                }
                setBorder(new LineBorder(background, 2));
                setBackground(background);
                setForeground(foreground);
                append((String) value, attributes);
            }
        });
    }

    public List<String> getStringValues() {
        return getModel().data;
    }

    public void setStringValues(Collection<String> stringValues) {
        List<String> data = getModel().data;
        data.clear();
        data.addAll(stringValues);
        Collections.sort(data);
    }


    public static class EditableListModel extends DBNEditableTableModel {
        private List<String> data;

        public EditableListModel(List<String> data) {
            this.data = data;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return "DATA";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return data.get(rowIndex);
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            data.set(rowIndex, (String) value);
            notifyListeners(rowIndex, rowIndex, columnIndex);
        }

        @Override
        public void insertRow(int rowIndex) {
            data.add(rowIndex, "");
            notifyListeners(rowIndex, data.size() + 1, -1);
        }

        @Override
        public void removeRow(int rowIndex) {
            data.remove(rowIndex);
            notifyListeners(rowIndex, data.size() + 1, -1);
        }

        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int index) {
            return data.get(index);
        }
    }
}
