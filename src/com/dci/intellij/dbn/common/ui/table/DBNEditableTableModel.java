package com.dci.intellij.dbn.common.ui.table;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.util.HashSet;
import java.util.Set;

import com.dci.intellij.dbn.common.dispose.DisposerUtil;
import com.dci.intellij.dbn.common.util.LazyValue;
import com.intellij.openapi.Disposable;

public abstract class DBNEditableTableModel implements DBNTableWithGutterModel {
    private Set<TableModelListener> tableModelListeners = new HashSet<TableModelListener>();
    private LazyValue<GutterListModel> listModel = new LazyValue<GutterListModel>(this) {
        @Override
        protected GutterListModel load() {
            return new GutterListModel();
        }
    };

    public void addTableModelListener(TableModelListener listener) {
        tableModelListeners.add(listener);
    }

    public void removeTableModelListener(TableModelListener listener) {
        tableModelListeners.remove(listener);
    }

    @Override
    public synchronized ListModel getListModel() {
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
            for (ListDataListener listDataListener : listModel.get().listeners) {
                listDataListener.contentsChanged(listDataEvent);
            }
        }
    }

    private class GutterListModel implements ListModel, Disposable {
        private Set<ListDataListener> listeners = new HashSet<ListDataListener>();
        @Override
        public int getSize() {
            return getRowCount();
        }

        @Override
        public Object getElementAt(int index) {
            return index;
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }

        @Override
        public void dispose() {
            listeners.clear();
        }

        public Set<ListDataListener> getListeners() {
            return listeners;
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
            DisposerUtil.dispose(listModel);
            tableModelListeners.clear();
        }
    }
}
