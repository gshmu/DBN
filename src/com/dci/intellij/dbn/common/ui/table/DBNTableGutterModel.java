package com.dci.intellij.dbn.common.ui.table;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.HashSet;
import java.util.Set;

import com.dci.intellij.dbn.common.dispose.Disposable;

public class DBNTableGutterModel<T extends DBNTableWithGutterModel> implements ListModel, Disposable{
    private T tableModel;
    private Set<ListDataListener> listeners = new HashSet<ListDataListener>();

    public DBNTableGutterModel(T tableModel) {
        this.tableModel = tableModel;
    }

    public T getTableModel() {
        return tableModel;
    }

    @Override
    public int getSize() {
        return tableModel.getRowCount();
    }

    @Override
    public Object getElementAt(int index) {
        return index + 1;
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    public void notifyListeners(ListDataEvent listDataEvent) {
        for (ListDataListener listDataListener : listeners) {
            listDataListener.contentsChanged(listDataEvent);
        }
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
            listeners.clear();
            disposed = true;
            tableModel = null;
        }
    }
}
