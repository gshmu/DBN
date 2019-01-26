package com.dci.intellij.dbn.editor.session.ui.table;

import com.dci.intellij.dbn.editor.session.model.SessionBrowserColumnInfo;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SessionBrowserTableHeaderMouseListener extends MouseAdapter {
    private SessionBrowserTable table;

    public SessionBrowserTableHeaderMouseListener(SessionBrowserTable table) {
        this.table = table;
    }

    @Override
    public void mouseReleased(final MouseEvent event) {
        if (event.getButton() == MouseEvent.BUTTON3) {
            Point mousePoint = event.getPoint();
            int tableColumnIndex = table.getTableHeader().columnAtPoint(mousePoint);
            if (tableColumnIndex > -1) {
                int modelColumnIndex = table.convertColumnIndexToModel(tableColumnIndex);
                if (modelColumnIndex > -1) {
                    SessionBrowserColumnInfo columnInfo = (SessionBrowserColumnInfo) table.getModel().getColumnInfo(modelColumnIndex);
                    table.showPopupMenu(event, null, columnInfo);
                }
            }
        }
    }
}
