package com.dci.intellij.dbn.execution.script.options.ui;

import com.dci.intellij.dbn.common.ui.table.DBNEditableTableModel;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.DatabaseType;
import com.dci.intellij.dbn.execution.script.CmdLineInterface;
import com.dci.intellij.dbn.execution.script.CmdLineInterfaceBundle;
import com.intellij.openapi.options.ConfigurationException;

import java.util.HashSet;
import java.util.Set;

public class CmdLineInterfacesTableModel extends DBNEditableTableModel {
    private CmdLineInterfaceBundle bundle;

    public CmdLineInterfacesTableModel(CmdLineInterfaceBundle bundle) {
        this.bundle = bundle.clone();
    }

    public CmdLineInterfaceBundle getBundle() {
        return bundle;
    }

    public void setBundle(CmdLineInterfaceBundle bundle) {
        this.bundle = bundle.clone();
        notifyListeners(0, bundle.size(), -1);
    }

    @Override
    public int getRowCount() {
        return bundle.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? "Database Type" :
               columnIndex == 1 ? "Name" :
               columnIndex == 2 ? "Executable Path" : null;
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
        CmdLineInterface environmentType = getInterface(rowIndex);
        return
           columnIndex == 0 ? environmentType.getDatabaseType() :
           columnIndex == 1 ? environmentType.getName() :
           columnIndex == 2 ? environmentType.getExecutablePath() : null;
    }

    @Override
    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        Object actualValue = getValueAt(rowIndex, columnIndex);
        if (!CommonUtil.safeEqual(actualValue, o)) {
            CmdLineInterface cmdLineInterface = bundle.get(rowIndex);
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

    public Set<String> getInterfaceNames() {
        return bundle.getInterfaceNames();
    }


    private CmdLineInterface getInterface(int rowIndex) {
        while (bundle.size() <= rowIndex) {
            bundle.add(new CmdLineInterface());
        }
        return bundle.get(rowIndex);
    }

    public void addInterface(CmdLineInterface cmdLineInterface) {
        bundle.add(cmdLineInterface);
        int rowIndex = bundle.size() - 1;
        notifyListeners(rowIndex, rowIndex, -1);
    }

    @Override
    public void insertRow(int rowIndex) {
        bundle.add(rowIndex, new CmdLineInterface());
        notifyListeners(rowIndex, bundle.size()-1, -1);
    }

    @Override
    public void removeRow(int rowIndex) {
        if (bundle.size() > rowIndex) {
            bundle.remove(rowIndex);
            notifyListeners(rowIndex, bundle.size()-1, -1);
        }
    }

    public void validate() throws ConfigurationException {
        Set<String> names = new HashSet<String>();
        for (CmdLineInterface cmdLineInterface : bundle.getInterfaces()) {
            String name = cmdLineInterface.getName();
            if (StringUtil.isEmpty(name)) {
                throw new ConfigurationException("Please provide names for each Command-Line Interface.");
            } else if (names.contains(name)) {
                throw new ConfigurationException("Please provide unique Command-Line Interface names.");
            } else {
                names.add(name);
            }
        }

        for (CmdLineInterface cmdLineInterface : bundle.getInterfaces()) {
            if (StringUtil.isEmpty(cmdLineInterface.getExecutablePath())) {
                throw new ConfigurationException("Please provide executable paths for each Command-Line Interface.");
            }
        }
    }
}
