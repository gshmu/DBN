package com.dci.intellij.dbn.common.ui.table;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;

import com.dci.intellij.dbn.common.dispose.Disposable;

public class DBNTableGutterModel implements ListModel, Disposable{
    private DBNTableWithGutterModel tableModel;

    public DBNTableGutterModel(DBNTableWithGutterModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public int getSize() {
        return tableModel.getRowCount();
    }

    @Override
    public Object getElementAt(int index) {
        return index;
    }

    @Override
    public void addListDataListener(ListDataListener l) {

    }

    @Override
    public void removeListDataListener(ListDataListener l) {

    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    private boolean disposed;

    @Override
    public boolean isDisposed() {
        return disposed;
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            tableModel = null;
        }
    }
}
