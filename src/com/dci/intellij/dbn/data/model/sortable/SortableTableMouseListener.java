package com.dci.intellij.dbn.data.model.sortable;

import com.dci.intellij.dbn.data.grid.ui.table.sortable.SortableTable;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SortableTableMouseListener extends MouseAdapter{
    private SortableTable table;

    public SortableTableMouseListener(SortableTable table) {
        this.table = table;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        super.mouseClicked(e);
    }
}
