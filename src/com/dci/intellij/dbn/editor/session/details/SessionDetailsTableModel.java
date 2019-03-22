package com.dci.intellij.dbn.editor.session.details;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.ui.table.DBNTableModel;
import com.dci.intellij.dbn.editor.session.model.SessionBrowserModelRow;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.TableModelListener;

public class SessionDetailsTableModel extends DisposableBase implements DBNTableModel {
    private String sessionId = "";
    private String user = "";
    private String schema = "";
    private String host = "";
    private String status = "";

    public SessionDetailsTableModel() {
        this(null);
    }

    public SessionDetailsTableModel(@Nullable SessionBrowserModelRow row) {
        if (row == null) {
            sessionId = "";
            user = "";
            schema = "";
            host = "";
            status = "";
        } else {
            sessionId = String.valueOf(row.getSessionId());
            user = row.getUser();
            schema = row.getSchema();
            host = row.getHost();
            status = row.getStatus();
        }
    }

    @Override
    public int getRowCount() {
        return 4;
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0: return "Attribute";
            case 1: return "Value";
        }
        return null;
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
        if (columnIndex == 0) {
            switch (rowIndex) {
                case 0: return "Session Id";
                case 1: return "User";
                case 2: return "Schema";
                case 3: return "Host";
                case 4: return "Status";
            }
        } else if (columnIndex == 1) {
            switch (rowIndex) {
                case 0: return sessionId;
                case 1: return user;
                case 2: return schema;
                case 3: return host;
                case 4: return status;
            }
        }
        return null;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }

    /********************************************************
     *                     Disposable                       *
     ********************************************************/

    @Override
    public void disposeInner() {
        Disposer.nullify(this);
        super.disposeInner();
    }
}
