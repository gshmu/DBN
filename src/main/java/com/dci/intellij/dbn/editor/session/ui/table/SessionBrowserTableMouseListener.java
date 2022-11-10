package com.dci.intellij.dbn.editor.session.ui.table;

import com.dci.intellij.dbn.editor.session.model.SessionBrowserColumnInfo;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelCell;
import com.dci.intellij.dbn.language.common.WeakRef;
import org.jetbrains.annotations.NotNull;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SessionBrowserTableMouseListener extends MouseAdapter {
    private final WeakRef<SessionBrowserTable> table;

    public SessionBrowserTableMouseListener(SessionBrowserTable table) {
        this.table = WeakRef.of(table);
    }

    @NotNull
    public SessionBrowserTable getTable() {
        return table.ensure();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseReleased(final MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            SessionBrowserTable table = getTable();
            Point mousePoint = e.getPoint();
            SessionBrowserModelCell cell = (SessionBrowserModelCell) table.getCellAtLocation(mousePoint);
            if (cell != null) {
                int rowIndex = table.rowAtPoint(mousePoint);
                int columnIndex = table.columnAtPoint(mousePoint);
                int[] selectedRows = table.getSelectedRows();
                int[] selectedColumns = table.getSelectedColumns();

                boolean selectCell = true;
                for (int selectedRow : selectedRows) {
                    if (selectedRow == rowIndex) {
                        for (int selectedColumn : selectedColumns) {
                            if (selectedColumn == columnIndex ) {
                                selectCell = false;
                                break;
                            }
                        }
                        break;
                    }
                }


                if (selectCell) {
                    table.selectCell(rowIndex, columnIndex);
                }
                SessionBrowserColumnInfo columnInfo = (SessionBrowserColumnInfo) table.getModel().getColumnInfo(columnIndex);
                table.showPopupMenu(e, cell, columnInfo);
            }
        }
    }
}