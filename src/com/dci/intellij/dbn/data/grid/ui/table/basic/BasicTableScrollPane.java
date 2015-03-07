package com.dci.intellij.dbn.data.grid.ui.table.basic;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseWheelEvent;

import com.intellij.ui.components.JBScrollPane;

public class BasicTableScrollPane extends JBScrollPane{
    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        if (e.isControlDown()) {
            Component view = getViewport().getView();
            assert view instanceof BasicTable;

            BasicTable resultTable = (BasicTable) view;
            Font font = resultTable.getFont();
            int size = font.getSize() + e.getWheelRotation();
            if (size > 8 && size < 20) {
                Font newFont = font.deriveFont((float) size);
                resultTable.setFont(newFont);
            }
        } else{
            super.processMouseWheelEvent(e);
        }
    }
}
