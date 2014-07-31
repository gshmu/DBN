package com.dci.intellij.dbn.data.grid.ui.table.basic;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Rectangle;
import java.awt.event.FocusEvent;

import com.dci.intellij.dbn.common.ui.table.DBNTable;
import com.dci.intellij.dbn.data.model.basic.BasicDataModel;
import com.intellij.openapi.Disposable;
import com.intellij.util.ui.UIUtil;

public class BasicTableGutter extends JList implements Disposable {
    private BasicTable table;

    public BasicTableGutter(BasicTable table) {
        super(table.getModel());
        this.table = table;
        this.table.getSelectionModel().addListSelectionListener(tableSelectionListener);
        setCellRenderer(createCellRenderer());
        addListSelectionListener(gutterSelectionListener);
        int rowHeight = table.getRowHeight();
        if (rowHeight != 0) setFixedCellHeight(rowHeight);
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        if (getModel().getSize() == 0) {
            setFixedCellWidth(10);
        }
        setBackground(UIUtil.getPanelBackground());
    }

    protected ListCellRenderer createCellRenderer() {
        return new BasicTableGutterCellRenderer();
    }

    public DBNTable getTable() {
        return table;
    }

    @Override
    public BasicDataModel getModel() {
        return (BasicDataModel) super.getModel();
    }

    public void scrollRectToVisible(Rectangle rect) {
        super.scrollRectToVisible(rect);
        Rectangle tableRect = table.getVisibleRect();

        tableRect.y = rect.y;
        tableRect.height = rect.height;
        table.scrollRectToVisible(tableRect);
    }

    boolean justGainedFocus = false;

    @Override
    protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        if (e.getComponent() == this) {
            justGainedFocus = e.getID() == FocusEvent.FOCUS_GAINED;
        }
    }

    /*********************************************************
     *                ListSelectionListener                  *
     *********************************************************/
    private ListSelectionListener gutterSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (hasFocus()) {
                int lastColumnIndex = table.getColumnCount() - 1;
                if (justGainedFocus) {
                    justGainedFocus = false;
                    if (table.isEditing()) table.getCellEditor().cancelCellEditing();
                    table.clearSelection();
                    table.setColumnSelectionInterval(0, lastColumnIndex);
                }

                for (int i = e.getFirstIndex(); i <= e.getLastIndex(); i++) {
                    if (isSelectedIndex(i))
                        table.getSelectionModel().addSelectionInterval(i, i); else
                        table.getSelectionModel().removeSelectionInterval(i, i);
                }
            }
        }
    };

    private ListSelectionListener tableSelectionListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            repaint();
        }
    };

    @Override
    public void dispose() {
        table = null;
    }
}
