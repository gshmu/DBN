package com.dci.intellij.dbn.execution.explain.ui;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.dci.intellij.dbn.common.ui.tree.TreeUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.execution.explain.ExplainPlanEntry;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.codeInsight.template.impl.TemplateColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.treetable.TreeTable;
import com.intellij.ui.treeStructure.treetable.TreeTableTree;

public class ExplainPlanTreeTable extends TreeTable{
    private static final int MAX_TREE_COLUMN_WIDTH = 900;
    private static final int MAX_COLUMN_WIDTH = 200;
    private static final int MIN_COLUMN_WIDTH = 10;

    private SimpleTextAttributes operationAttributes;

    public ExplainPlanTreeTable(ExplainPlanTreeTableModel treeTableModel) {
        super(treeTableModel);
        EditorColorsScheme scheme = EditorColorsManager.getInstance().getGlobalScheme();
        TextAttributes attributes = scheme.getAttributes(TemplateColors.TEMPLATE_VARIABLE_ATTRIBUTES);
        operationAttributes = new SimpleTextAttributes(null, attributes.getForegroundColor(), null, SimpleTextAttributes.STYLE_PLAIN);
        setTreeCellRenderer(treeCellRenderer);
        setDefaultRenderer(Object.class, tableCellRenderer);
        setAutoResizeMode(AUTO_RESIZE_OFF);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        final TreeTableTree tree = getTree();
        tree.addTreeExpansionListener(new TreeExpansionListener() {
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
        TreeUtil.expandAll(tree);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                TreePath pathAtMousePosition = TreeUtil.getPathAtMousePosition(tree);
                if (pathAtMousePosition != null) {
                    Object lastPathComponent = pathAtMousePosition.getLastPathComponent();
                }
            }
        });
    }

    private final ColoredTreeCellRenderer treeCellRenderer = new ColoredTreeCellRenderer() {
        @Override
        protected void processMouseMotionEvent(MouseEvent e) {
            super.processMouseMotionEvent(e);
        }

        @Override
        public void customizeCellRenderer(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            ExplainPlanEntry entry = (ExplainPlanEntry) value;

            DBObjectRef objectRef = entry.getObjectRef();
            if (objectRef != null) {
                setIcon(objectRef.getObjectType().getIcon());
                append(objectRef.getPath() + " - ", SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }

            String operation = entry.getOperation();
            String options = entry.getOperationOptions();
            append(operation, selected ? SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES : operationAttributes);
            if (StringUtil.isNotEmpty(options)) {
                if (options.equals("FULL")) {
                    append(" (" + options.toLowerCase() + ")", selected ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.ERROR_ATTRIBUTES);
                } else {
                    append(" (" + options.toLowerCase() + ")", selected ? SimpleTextAttributes.REGULAR_ATTRIBUTES : SimpleTextAttributes.GRAYED_ATTRIBUTES);
                }
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
