package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.dispose.Disposable;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.HashSet;
import java.util.Set;

@Nullifiable
public class DBNTableGutterModel<T extends DBNTableWithGutterModel> extends DisposableBase implements ListModel, Disposable{
    private T tableModel;
    private Set<ListDataListener> listeners = new HashSet<ListDataListener>();

    public DBNTableGutterModel(T tableModel) {
        this.tableModel = tableModel;
    }

    @NotNull
    public T getTableModel() {
        return Failsafe.get(tableModel);
    }

    @Override
    public int getSize() {
        return getTableModel().getRowCount();
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
}
