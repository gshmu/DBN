package com.dci.intellij.dbn.common.ui.table;

import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.intellij.util.ui.UIUtil;

public abstract class DBNTableGutter<T extends DBNTableWithGutter> extends JList implements Disposable {
    private boolean disposed;
    private T table;

    public DBNTableGutter(T table) {
        super(table.getModel().getListModel());
        this.table = table;
        int rowHeight = table.getRowHeight();
        if (rowHeight != 0) setFixedCellHeight(rowHeight);
        setBackground(UIUtil.getPanelBackground());

        setCellRenderer(createCellRenderer());
    }

    protected abstract ListCellRenderer createCellRenderer();



    @Override
    public ListModel getModel() {
        ListModel cachedModel = super.getModel();
        if (table == null) {
            return cachedModel;
        } else {
            ListModel listModel = table.getModel().getListModel();
            if (listModel != cachedModel) {
                setModel(listModel);
            }
            return listModel;
        }
    }

    public T getTable() {
        return table;
    }


    @Override
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        disposed = true;
        table = null;
    }
}
