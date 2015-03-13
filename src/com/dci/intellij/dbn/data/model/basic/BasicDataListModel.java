package com.dci.intellij.dbn.data.model.basic;

import javax.swing.ListModel;
import javax.swing.event.ListDataListener;
import java.util.HashSet;
import java.util.Set;

import com.dci.intellij.dbn.data.model.DataModelRow;
import com.intellij.openapi.Disposable;

public class BasicDataListModel<T extends DataModelRow> implements ListModel, Disposable {
    private BasicDataModel<T> dataModel;
    private Set<ListDataListener> listeners = new HashSet<ListDataListener>();

    public BasicDataListModel(BasicDataModel<T> dataModel) {
        this.dataModel = dataModel;
    }

    @Override
    public int getSize() {
        return dataModel.getRowCount();
    }

    @Override
    public Object getElementAt(int index) {
        return dataModel.getRowAtIndex(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listeners.remove(l);
    }

    public Set<ListDataListener> getListeners() {
        return listeners;
    }

    @Override
    public void dispose() {
        listeners.clear();
        dataModel = null;
    }
}
