package com.dci.intellij.dbn.execution.script.options.ui;

import java.util.HashSet;
import java.util.Set;

import com.dci.intellij.dbn.common.ui.table.DBNEditableTableModel;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.intellij.openapi.options.ConfigurationException;

public class CmdLineInterfacesTableModel extends DBNEditableTableModel {
    private CmdLineInterfaceBundle cmdLineInterfaces;

    public CmdLineInterfacesTableModel(CmdLineInterfaceBundle cmdLineInterfaces) {
        this.cmdLineInterfaces = cmdLineInterfaces.clone();
    }

    public CmdLineInterfaceBundle getCmdLineInterfaces() {
        return cmdLineInterfaces;
    }

    public void setCmdLineInterfaces(CmdLineInterfaceBundle cmdLineInterfaces) {
        this.cmdLineInterfaces = cmdLineInterfaces.clone();
        notifyListeners(0, cmdLineInterfaces.size(), -1);
    }

    public int getRowCount() {
        return cmdLineInterfaces.size();
    }

    public int getColumnCount() {
        return 3;
    }

    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? "Database Type" :
               columnIndex == 1 ? "Name" :
               columnIndex == 2 ? "Executable Path" : null;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return String.class;

    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        CmdLineInterface environmentType = getExecutor(rowIndex);
        return
           columnIndex == 0 ? environmentType.getDatabaseType() :
           columnIndex == 1 ? environmentType.getName() :
           columnIndex == 2 ? environmentType.getExecutablePath() : null;
    }

    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        Object actualValue = getValueAt(rowIndex, columnIndex);
        if (!CommonUtil.safeEqual(actualValue, o)) {
            CmdLineInterface cmdLineInterface = cmdLineInterfaces.get(rowIndex);
            if (columnIndex == 0) {
                DatabaseType databaseType = (DatabaseType) o;
                cmdLineInterface.setDatabaseType(databaseType);
            } else if (columnIndex == 1) {
                cmdLineInterface.setName((String) o);
            } else if (columnIndex == 2) {
                cmdLineInterface.setExecutablePath((String) o);
            }

            notifyListeners(rowIndex, rowIndex, columnIndex);
        }
    }

    private CmdLineInterface getExecutor(int rowIndex) {
        while (cmdLineInterfaces.size() <= rowIndex) {
            cmdLineInterfaces.add(new CmdLineInterface());
        }
        return cmdLineInterfaces.get(rowIndex);
    }

    public void insertRow(int rowIndex) {
        cmdLineInterfaces.add(rowIndex, new CmdLineInterface());
        notifyListeners(rowIndex, cmdLineInterfaces.size()-1, -1);
    }

    public void removeRow(int rowIndex) {
        if (cmdLineInterfaces.size() > rowIndex) {
            cmdLineInterfaces.remove(rowIndex);
            notifyListeners(rowIndex, cmdLineInterfaces.size()-1, -1);
        }
    }

    public void validate() throws ConfigurationException {
        Set<String> names = new HashSet<String>();
        for (CmdLineInterface cmdLineInterface : cmdLineInterfaces.getInterfaces()) {
            String name = cmdLineInterface.getName();
            if (StringUtil.isEmpty(name)) {
                throw new ConfigurationException("Please provide names for each Command-Line Interface.");
            } else if (names.contains(name)) {
                throw new ConfigurationException("Please provide unique Command-Line Interface names.");
            } else {
                names.add(name);
            }
        }

        for (CmdLineInterface cmdLineInterface : cmdLineInterfaces.getInterfaces()) {
            if (StringUtil.isEmpty(cmdLineInterface.getExecutablePath())) {
                throw new ConfigurationException("Please provide executable paths for each Command-Line Interface.");
            }
        }
    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    public void dispose() {
        super.dispose();
    }
}
