package com.dci.intellij.dbn.connection.config.tns.ui;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.ui.table.DBNReadonlyTableModel;
import com.dci.intellij.dbn.connection.config.tns.TnsName;

import java.util.Collections;
import java.util.List;

public class TnsNamesTableModel extends StatefulDisposable.Base implements DBNReadonlyTableModel<TnsName> {
    private final List<TnsName> tnsNames;

    TnsNamesTableModel(List<TnsName> tnsNames) {
        super();
        Collections.sort(tnsNames);
        this.tnsNames = tnsNames;
    }

    List<TnsName> getTnsNames() {
        return tnsNames;
    }

    @Override
    public int getRowCount() {
        return tnsNames.size();
    }

    @Override
    public int getColumnCount() {
        return 10;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Name";
            case 1: return "Protocol";
            case 2: return "Host";
            case 3: return "Port";
            case 4: return "SID";
            case 5: return "Service Name";
            case 6: return "Global Name";
            case 7: return "Failover";
            case 8: return "Type";
            case 9: return "Method";
            default: return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return TnsName.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return tnsNames.get(rowIndex);
        //return getColumnValue(tnsName, columnIndex);
    }

    @Override
    protected void disposeInner() {
        nullify();
    }

    @Override
    public Object getValue(TnsName tnsName, int column) {
        switch (column) {
            case 0: return tnsName.getName();
            case 1: return tnsName.getProtocol();
            case 2: return tnsName.getHost();
            case 3: return tnsName.getPort();
            case 4: return tnsName.getSid();
            case 5: return tnsName.getServiceName();
            case 6: return tnsName.getGlobalName();
            case 7: return tnsName.getFailover();
            case 8: return tnsName.getFailoverType();
            case 9: return tnsName.getFailoverMethod();
            default: return "";
        }
    }

    @Override
    public String getPresentableValue(TnsName tnsName, int column) {
        switch (column) {
            case 0: return tnsName.getName();
            case 1: return tnsName.getProtocol();
            case 2: return tnsName.getHost();
            case 3: return tnsName.getPort();
            case 4: return tnsName.getSid();
            case 5: return tnsName.getServiceName();
            case 6: return tnsName.getGlobalName();
            case 7: return tnsName.getFailover();
            case 8: return tnsName.getFailoverType();
            case 9: return tnsName.getFailoverMethod();
            default: return "";
        }
    }
}
