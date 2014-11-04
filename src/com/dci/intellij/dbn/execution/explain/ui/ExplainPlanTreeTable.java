package com.dci.intellij.dbn.execution.explain.ui;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.Component;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.execution.explain.ExplainPlanEntry;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.treetable.TreeTable;

public class ExplainPlanTreeTable extends TreeTable{
    private static final int MAX_TREE_COLUMN_WIDTH = 600;
    private static final int MAX_COLUMN_WIDTH = 250;
    private static final int MIN_COLUMN_WIDTH = 10;

    public ExplainPlanTreeTable(ExplainPlanTreeTableModel treeTableModel) {
        super(treeTableModel);
        setTreeCellRenderer(treeCellRenderer);
        setDefaultRenderer(Object.class, tableCellRenderer);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        getTree().addTreeExpansionListener(new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                accommodateColumnsSize();
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                accommodateColumnsSize();
            }
        });
        accommodateColumnsSize();
    }

    private final ColoredTreeCellRenderer treeCellRenderer = new ColoredTreeCellRenderer() {
        @Override
        public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            ExplainPlanEntry entry = (ExplainPlanEntry) value;
            String operation = entry.getOperation();
            String options = entry.getOperationOptions();
            append(operation, SimpleTextAttributes.REGULAR_ATTRIBUTES);
            if (StringUtil.isNotEmpty(options)) {
                if (options.equals("FULL")) {
                    append(" (" + options.toLowerCase() + ")", SimpleTextAttributes.ERROR_ATTRIBUTES);
                } else {
                    append(" (" + options.toLowerCase() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
                }
            }
            DBObjectRef objectRef = entry.getObjectRef();
            if (objectRef != null) {
                setIcon(objectRef.getObjectType().getIcon());
            }

        }
    };

    private final ColoredTableCellRenderer tableCellRenderer = new ColoredTableCellRenderer() {
        @Override
        protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
            if (value instanceof DBObjectRef) {
                DBObjectRef objectRef = (DBObjectRef) value;
                append(objectRef.getPath(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            } else if (value instanceof String){
                append((String) value);
            }
        }
    };

    public void accommodateColumnsSize() {
        for (int columnIndex = 0; columnIndex < getColumnCount(); columnIndex++){
            accommodateColumnSize(columnIndex, 22);
        }
    }

    public void accommodateColumnSize(int colIndex, int span) {
        TableColumn column = getColumnModel().getColumn(colIndex);
        int columnIndex = column.getModelIndex();
        int preferredWidth = 0;

        // header
        JTableHeader tableHeader = getTableHeader();
        if (tableHeader != null) {
            Object headerValue = column.getHeaderValue();
            TableCellRenderer headerCellRenderer = column.getHeaderRenderer();
            if (headerCellRenderer == null) headerCellRenderer = tableHeader.getDefaultRenderer();
            Component headerComponent = headerCellRenderer.getTableCellRendererComponent(this, headerValue, false, false, 0, columnIndex);
            if (headerComponent.getPreferredSize().width > preferredWidth)
                preferredWidth = headerComponent.getPreferredSize().width;
        }

        // rows
        int maxColumnWidth = columnIndex == 0 ? MAX_TREE_COLUMN_WIDTH : MAX_COLUMN_WIDTH;
        for (int rowIndex =0; rowIndex < getModel().getRowCount(); rowIndex++) {
            if (preferredWidth > maxColumnWidth) {
                break;
            }
            Object value = getModel().getValueAt(rowIndex, columnIndex);
            TableCellRenderer renderer = getCellRenderer(rowIndex, columnIndex);

            if (renderer != null) {
                Component component = renderer.getTableCellRendererComponent(this, value, false, false, rowIndex, columnIndex);
                if (component.getPreferredSize().width > preferredWidth) {
                    preferredWidth = component.getPreferredSize().width;
                }
            }
        }

        if (preferredWidth > maxColumnWidth) {
            preferredWidth = maxColumnWidth;
        }

        if (preferredWidth < MIN_COLUMN_WIDTH) {
            preferredWidth = MIN_COLUMN_WIDTH;
        }

        preferredWidth = preferredWidth + span;

        if (column.getPreferredWidth() != preferredWidth)  {
            column.setPreferredWidth(preferredWidth);
        }

    }
}
