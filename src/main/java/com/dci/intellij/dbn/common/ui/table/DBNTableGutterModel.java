package com.dci.intellij.dbn.common.ui.table;

import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposableBase;
import com.dci.intellij.dbn.common.ref.WeakRef;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.util.HashSet;
import java.util.Set;

public class DBNTableGutterModel<T extends DBNTableWithGutterModel> extends StatefulDisposableBase implements ListModel {
    private final WeakRef<T> tableModel;
    private final Set<ListDataListener> listeners = new HashSet<>();

    public DBNTableGutterModel(@NotNull T tableModel) {
        this.tableModel = WeakRef.of(tableModel);

        Disposer.register(tableModel, this);
    }

    @NotNull
    public T getTableModel() {
        return tableModel.ensure();
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

    @Override
    protected void disposeInner() {
        nullify();
    }
}
