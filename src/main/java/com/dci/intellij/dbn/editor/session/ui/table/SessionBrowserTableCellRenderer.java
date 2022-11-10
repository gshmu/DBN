package com.dci.intellij.dbn.editor.session.ui.table;

import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.util.Commons;
import com.dci.intellij.dbn.data.grid.ui.table.basic.BasicTableCellRenderer;
import com.dci.intellij.dbn.editor.session.color.SessionBrowserTextAttributes;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelCell;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import com.intellij.ui.SimpleTextAttributes;

import javax.swing.border.Border;
import java.awt.*;

public class SessionBrowserTableCellRenderer extends BasicTableCellRenderer {

    @Override
    public SessionBrowserTextAttributes getAttributes() {
        return SessionBrowserTextAttributes.get();
    }

    @Override
    protected void customizeCellRenderer(DBNTable table, Object value, boolean isSelected, boolean hasFocus, int rowIndex, int columnIndex) {
        acquireState(table, isSelected, false, rowIndex, columnIndex);
        SessionBrowserModelCell cell = (SessionBrowserModelCell) value;
        SessionBrowserTable sessionBrowserTable = (SessionBrowserTable) table;

        if (Failsafe.check(cell, sessionBrowserTable)) {
            SessionBrowserModelRow row = cell.getRow();
            boolean isLoading = sessionBrowserTable.isLoading();

            boolean isCaretRow = table.getCellSelectionEnabled() && table.getSelectedRow() == rowIndex && table.getSelectedRowCount() == 1;
            boolean isConnected = Failsafe.nn(sessionBrowserTable.getSessionBrowser().getConnection()).isConnected();

            SessionBrowserTextAttributes attributes = getAttributes();
            SimpleTextAttributes textAttributes = attributes.getActiveSession(isCaretRow);

            if (isSelected) {
                textAttributes = attributes.getSelection();
            } else {
                if (isLoading || !isConnected) {
                    textAttributes = attributes.getLoadingData(isCaretRow);
                } else {
                    switch (row.getSessionStatus()) {
                        case ACTIVE: textAttributes = attributes.getActiveSession(isCaretRow); break;
                        case INACTIVE: textAttributes = attributes.getInactiveSession(isCaretRow); break;
                        case CACHED: textAttributes = attributes.getCachedSession(isCaretRow); break;
                        case SNIPED: textAttributes = attributes.getSnipedSession(isCaretRow); break;
                        case KILLED: textAttributes = attributes.getKilledSession(isCaretRow); break;
                    }
                }
            }

            Color background = Commons.nvl(textAttributes.getBgColor(), table.getBackground());
            Color foreground = Commons.nvl(textAttributes.getFgColor(), table.getForeground());


            Border border = Borders.lineBorder(background);

            setBorder(border);
            setBackground(background);
            setForeground(foreground);
            writeUserValue(cell, textAttributes, attributes);
        }
    }

    @Override
    public void setForeground(Color fg) {
        super.setForeground(fg);
    }
}
                                                                