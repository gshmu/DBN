package com.dci.intellij.dbn.editor.data.ui.table.listener;

import com.dci.intellij.dbn.data.model.ColumnInfo;
import com.dci.intellij.dbn.editor.data.ui.table.DatasetEditorTable;
import com.dci.intellij.dbn.language.common.WeakRef;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DatasetEditorHeaderMouseListener extends MouseAdapter {
    private final WeakRef<DatasetEditorTable> table;

    public DatasetEditorHeaderMouseListener(DatasetEditorTable table) {
        this.table = WeakRef.of(table);
    }

    public DatasetEditorTable getTable() {
        return table.ensure();
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON3) {
            DatasetEditorTable table = getTable();
            Point mousePoint = event.getPoint();
            int tableColumnIndex = table.getTableHeader().columnAtPoint(mousePoint);
            if (tableColumnIndex > -1) {
                int modelColumnIndex = table.convertColumnIndexToModel(tableColumnIndex);
                if (modelColumnIndex > -1) {
                    ColumnInfo columnInfo = table.getModel().getColumnInfo(modelColumnIndex);
                    table.showPopupMenu(event, null, columnInfo);
                }
            }
        }
    }
}
