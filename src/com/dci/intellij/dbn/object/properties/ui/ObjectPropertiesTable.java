package com.dci.intellij.dbn.object.properties.ui;

import com.dci.intellij.dbn.common.ui.Borders;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.common.ui.MouseUtil;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.object.properties.PresentableProperty;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.pom.Navigatable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;

public class ObjectPropertiesTable extends DBNTable<DBNTableModel> {
    ObjectPropertiesTable(DBNForm parent, DBNTableModel tableModel) {
        super(parent, tableModel, false);
        setDefaultRenderer(String.class, cellRenderer);
        setDefaultRenderer(PresentableProperty.class, cellRenderer);
        adjustRowHeight(3);

        addMouseListener(mouseListener);
        addKeyListener(keyListener);
    }

    private final MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent event) {
            if (event.getButton() == MouseEvent.BUTTON1 && event.getClickCount() > 1) {
                navigateInBrowser();
                event.consume();
            }


            if (MouseUtil.isNavigationEvent(event)) {
                navigateInBrowser();
                event.consume();
            }
        }
    };


    private final KeyListener keyListener = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            if (e.getKeyChar() == 10) {  // ENTER
                navigateInBrowser();
            }
        }
    };


    private void navigateInBrowser() {
        int rowIndex = getSelectedRow();
        int columnIndex = getSelectedColumn();
        if (columnIndex == 1) {
            PresentableProperty presentableProperty = (PresentableProperty) getModel().getValueAt(rowIndex, 1);
            Navigatable navigatable = presentableProperty.getNavigatable();
            if (navigatable != null) navigatable.navigate(true);
        }
    }


    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        if (e.isControlDown() && e.getID() != MouseEvent.MOUSE_DRAGGED && isNavigableCellAtMousePosition()) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
            super.processMouseMotionEvent(e);
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private boolean isNavigableCellAtMousePosition() {
        Object value = getValueAtMouseLocation();
        if (value instanceof PresentableProperty) {
            PresentableProperty property = (PresentableProperty) value;
            return property.getNavigatable() != null;
        }
        return false;
    }

    private final TableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            try {
                Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                PresentableProperty property = (PresentableProperty) value;
                if (property != null) {
                    if (column == 0) {
                        setIcon(null);
                        setText(property.getName());
                        //setFont(GUIUtil.BOLD_FONT);
                    } else if (column == 1) {
                        setText(property.getValue());
                        setIcon(property.getIcon());
                        //setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        //setFont(property.getIcon() == null ? GUIUtil.BOLD_FONT : GUIUtil.REGULAR_FONT);
                    }
                }

                Dimension dimension = getSize();
                dimension.setSize(dimension.getWidth(), 30);
                setSize(dimension);
                setBorder(Borders.TEXT_FIELD_BORDER);

                return component;
            } catch (ProcessCanceledException e) {
                return this;
            }
        }

    };
}
