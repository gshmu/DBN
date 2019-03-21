package com.dci.intellij.dbn.connection.config.tns.ui;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.connection.config.tns.TnsName;

import javax.swing.event.TableModelListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TnsNamesTableModel extends DisposableBase implements DBNTableModel {
    private TnsName[] tnsNames;
    private Set<TableModelListener> listeners = new HashSet<>();

    public TnsNamesTableModel(TnsName[] tnsNames) {
        super();
        Arrays.sort(tnsNames);
        this.tnsNames = tnsNames;
    }

    public TnsName[] getTnsNames() {
        return tnsNames;
    }

    @Override
    public int getRowCount() {
        return tnsNames.length;
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Name";
            case 1: return "Host";
            case 2: return "Port";
            case 3: return "SID";
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        TnsName tnsName = tnsNames[rowIndex];

        switch (columnIndex) {
            case 0: return tnsName.getName();
            case 1: return tnsName.getHost();
            case 2: return tnsName.getPort();
            case 3: return tnsName.getSid();
            default: return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        listeners.remove(l);
    }


    @Override
    public void disposeInner() {
        super.disposeInner();
        nullify();
    }
}
