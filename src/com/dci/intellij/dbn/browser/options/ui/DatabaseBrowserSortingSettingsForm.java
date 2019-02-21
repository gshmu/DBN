package com.dci.intellij.dbn.browser.options.ui;

import com.dci.intellij.dbn.browser.options.DatabaseBrowserSortingSettings;
import com.dci.intellij.dbn.common.options.ui.ConfigurationEditorForm;
import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.table.DBNEditableTable;
import com.dci.intellij.dbn.common.ui.table.DBNEditableTableModel;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.sorting.DBObjectComparator;
import com.dci.intellij.dbn.object.common.sorting.SortingType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBoxTableRenderer;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.common.ui.GUIUtil.updateBorderTitleForeground;

public class DatabaseBrowserSortingSettingsForm extends ConfigurationEditorForm<DatabaseBrowserSortingSettings> {
    private JPanel mainPanel;
    private JBScrollPane sortingTypesScrollPanel;
    private JTable sortingTypeTable;

    public DatabaseBrowserSortingSettingsForm(DatabaseBrowserSortingSettings settings) {
        super(settings);
        updateBorderTitleForeground(mainPanel);
        Project project = settings.getProject();
        sortingTypeTable = new SortingTypeTable(project, settings.getComparators());
        sortingTypesScrollPanel.setViewportView(sortingTypeTable);
        sortingTypesScrollPanel.getViewport().setBackground(sortingTypeTable.getBackground());
        registerComponent(sortingTypeTable);
    }



    @Override
    public void applyFormChanges() throws ConfigurationException {
        SortingTypeTableModel model = (SortingTypeTableModel) sortingTypeTable.getModel();
        getConfiguration().setComparators(model.comparators);
    }

    @Override
    public void resetFormChanges() {
        sortingTypeTable.setModel(new SortingTypeTableModel(getConfiguration().getComparators()));
    }

    @NotNull
    @Override
    public JComponent getComponent() {
        return mainPanel;
    }

    public class SortingTypeTable extends DBNEditableTable<SortingTypeTableModel> {

        public SortingTypeTable(Project project, List<DBObjectComparator> comparators) {
            super(project, new SortingTypeTableModel(comparators), true);
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            adjustRowHeight(3);

            setDefaultRenderer(DBObjectType.class, new ColoredTableCellRenderer() {
                @Override
                protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
                    DBObjectType objectType = (DBObjectType) value;
                    if (objectType != null) {
                        setIcon(objectType.getIcon());
                        append(objectType.getName().toUpperCase(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    } else {
                        append("");
                    }
                    setBorder(SELECTION_BORDER);
                }
            });

            setDefaultRenderer(SortingType.class, new ColoredTableCellRenderer() {
                @Override
                protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
                    SortingType sortingType = (SortingType) value;
                    append(sortingType.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
                    setBorder(SELECTION_BORDER);
                }
            });

            ComboBoxTableRenderer<SortingType> editor = new ComboBoxTableRenderer<SortingType>(SortingType.values()) {};
            editor.setBorder(Borders.TEXT_FIELD_BORDER);
            setDefaultEditor(SortingType.class, editor);

            getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        //editCellAt(getSelectedRows()[0], getSelectedColumns()[0]);
                    }
                }
            });
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent e) {
            Point mouseLocation = e.getPoint();
            int columnIndex = columnAtPoint(mouseLocation);
            if (columnIndex == 1) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            } else {
                setCursor(Cursor.getDefaultCursor());
            }
            super.processMouseMotionEvent(e);
        }
    }

    public class SortingTypeTableModel extends DBNEditableTableModel {
        private List<DBObjectComparator> comparators = new ArrayList<DBObjectComparator>();

        public SortingTypeTableModel(List<DBObjectComparator> comparators) {
            this.comparators = new ArrayList<DBObjectComparator>(comparators);
        }

        @Override
        public int getRowCount() {
            return comparators.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0: return "Object Type";
                case 1: return "Sorting Type";
            }
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0: return DBObjectType.class;
                case 1: return SortingType.class;
            }
            return null;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0: return false;
                case 1: return true;
            }
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DBObjectComparator comparator = comparators.get(rowIndex);
            switch (columnIndex) {
                case 0: return comparator.getObjectType();
                case 1: return comparator.getSortingType();
            }
            return null;
        }

        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            if (columnIndex == 1) {
                SortingType sortingType = (SortingType) value;
                if (sortingType != null) {
                    DBObjectComparator comparator = comparators.remove(rowIndex);
                    comparators.add(rowIndex, DBObjectComparator.get(comparator.getObjectType(), sortingType));
                }
            }
        }

        @Override
        public void insertRow(int rowIndex) {
            throw new UnsupportedOperationException("Row mutation not supported");
        }

        @Override
        public void removeRow(int rowIndex) {
            throw new UnsupportedOperationException("Row mutation not supported");
        }
    }
}
