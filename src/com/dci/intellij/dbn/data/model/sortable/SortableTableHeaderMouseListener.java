package com.dci.intellij.dbn.data.model.sortable;

import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;
import com.dci.intellij.dbn.data.sorting.SortDirection;
import com.dci.intellij.dbn.language.common.WeakRef;

import javax.swing.table.JTableHeader;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SortableTableHeaderMouseListener extends MouseAdapter {
    private final WeakRef<SortableTable> table;

    public SortableTableHeaderMouseListener(SortableTable table) {
        this.table = WeakRef.of(table);
    }

    public SortableTable getTable() {
        return table.ensure();
    }

    @Override
    public void mouseClicked(MouseEvent event) {
        SortableTable table = getTable();
        if (event.getButton() == MouseEvent.BUTTON1) {
            Point mousePoint = event.getPoint();
            mousePoint.setLocation(mousePoint.getX() - 4, mousePoint.getX());
            JTableHeader tableHeader = table.getTableHeader();
            int columnIndex = tableHeader.columnAtPoint(mousePoint);
            if (columnIndex > -1) {
                Rectangle colRect = tableHeader.getHeaderRect(columnIndex);
                boolean isEdgeClick = colRect.getMaxX() - 8 < mousePoint.getX();
                if (isEdgeClick) {
                    if (event.getClickCount() == 2) {
                        table.accommodateColumnSize(columnIndex, table.getColumnWidthBuffer());
                    }
                } else {
                    boolean keepExisting = event.isControlDown();
                    table.sort(columnIndex, SortDirection.INDEFINITE, keepExisting);
                }
            }
        }
        table.requestFocus();
        //event.consume();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        super.mouseDragged(e);
    }
}
