package com.dci.intellij.dbn.common.ui.table;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.HashSet;
import java.util.Set;

import com.dci.intellij.dbn.common.util.DisposableLazyValue;
import com.dci.intellij.dbn.common.util.LazyValue;

public abstract class DBNEditableTableModel implements DBNTableWithGutterModel {
    private Set<TableModelListener> tableModelListeners = new HashSet<TableModelListener>();
    private LazyValue<DBNTableGutterModel> listModel = new DisposableLazyValue<DBNTableGutterModel>(this) {
        @Override
        protected DBNTableGutterModel load() {
            return new DBNTableGutterModel(DBNEditableTableModel.this);
        }
    };

    public void addTableModelListener(TableModelListener listener) {
        tableModelListeners.add(listener);
    }

    public void removeTableModelListener(TableModelListener listener) {
        tableModelListeners.remove(listener);
    }

    @Override
    public ListModel getListModel() {
        return listModel.get();
    }

    public abstract void insertRow(int rowIndex);

    public abstract void removeRow(int rowIndex);

    public void notifyListeners(int firstRowIndex, int lastRowIndex, int columnIndex) {
        TableModelEvent modelEvent = new TableModelEvent(this, firstRowIndex, lastRowIndex, columnIndex);
        for (TableModelListener listener : tableModelListeners) {
            listener.tableChanged(modelEvent);
        }

        if (listModel.isLoaded()) {
            ListDataEvent listDataEvent = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, firstRowIndex, lastRowIndex);
            listModel.get().notifyListeners(listDataEvent);
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
            disposed = true;
            tableModelListeners.clear();
        }
    }
}
