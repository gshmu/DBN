package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.ui.table.DBNEditableTableModel;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.config.file.DatabaseFile;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;

import javax.swing.event.TableModelListener;

public class DatabaseFilesTableModel extends DBNEditableTableModel {
    private DatabaseFiles databaseFiles;

    DatabaseFilesTableModel(DatabaseFiles databaseFiles) {
        this.databaseFiles = databaseFiles == null ? new DatabaseFiles(null) : databaseFiles.clone();
        addTableModelListener(defaultModelListener);
    }

    public void setDatabaseFiles(DatabaseFiles databaseFiles) {
        this.databaseFiles = databaseFiles == null ? new DatabaseFiles(null) : databaseFiles.clone();
        notifyListeners(0, this.databaseFiles.size(), -1);
    }

    @Override
    public int getRowCount() {
        return databaseFiles.size();
    }
    
    TableModelListener defaultModelListener = e -> {};

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? "File Path" :
               columnIndex == 1 ? "Database Name" : null;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DatabaseFile filePathOption = databaseFiles.get(rowIndex);
        return
           columnIndex == 0 ? filePathOption.getPath() :
           columnIndex == 1 ? filePathOption.getSchema() : null;
    }

    @Override
    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        Object actualValue = getValueAt(rowIndex, columnIndex);
        if (!Safe.equal(actualValue, o)) {
            DatabaseFile filePathOption = databaseFiles.get(rowIndex);
            if (columnIndex == 0) {
                filePathOption.setPath((String) o);
            } else if (columnIndex == 1) {
                filePathOption.setSchema((String) o);
            }

            notifyListeners(rowIndex, rowIndex, columnIndex);
        }
    }

    private DatabaseFile getFile(int rowIndex) {
        while (databaseFiles.size() <= rowIndex) {
            databaseFiles.add(new DatabaseFile());
        }
        return databaseFiles.get(rowIndex);
    }

    public DatabaseFiles getDatabaseFiles() {
        return databaseFiles;
    }

    @Override
    public void insertRow(int rowIndex) {
        databaseFiles.add(rowIndex, new DatabaseFile());
        notifyListeners(rowIndex, databaseFiles.size()-1, -1);
    }

    @Override
    public void removeRow(int rowIndex) {
        if (databaseFiles.size() > rowIndex) {
            databaseFiles.remove(rowIndex);
            notifyListeners(rowIndex, databaseFiles.size()-1, -1);
        }
    }

    public void validate() {

    }
}
