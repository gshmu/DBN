package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.ui.table.DBNEditableTableModel;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.connection.config.file.DatabaseFile;
import com.dci.intellij.dbn.connection.config.file.DatabaseFilesBundle;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.io.File;

public class DatabaseFilesTableModel extends DBNEditableTableModel {
    private DatabaseFilesBundle databaseFiles;

    public DatabaseFilesTableModel(DatabaseFilesBundle filesBundle) {
        this.databaseFiles = filesBundle;
        addTableModelListener(defaultModelListener);
    }

    public void setDatabaseFiles(DatabaseFilesBundle filesBundle) {
        this.databaseFiles = filesBundle;
        notifyListeners(0, filesBundle.size(), -1);
    }

    public int getRowCount() {
        return databaseFiles.size();
    }
    
    TableModelListener defaultModelListener = new TableModelListener() {
        @Override
        public void tableChanged(TableModelEvent e) {
        }
    };    

    public int getColumnCount() {
        return 2;
    }

    public String getColumnName(int columnIndex) {
        return columnIndex == 0 ? "File Path" :
               columnIndex == 1 ? "Database Name" : null;
    }

    public Class<?> getColumnClass(int columnIndex) {
        return columnIndex == 0 ? File.class :
                columnIndex == 1 ? String.class : null;
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        DatabaseFile filePathOption = databaseFiles.get(rowIndex);
        return
           columnIndex == 0 ? filePathOption.getFile() :
           columnIndex == 1 ? filePathOption.getDatabaseName() : null;
    }

    public void setValueAt(Object o, int rowIndex, int columnIndex) {
        Object actualValue = getValueAt(rowIndex, columnIndex);
        if (!CommonUtil.safeEqual(actualValue, o)) {
            DatabaseFile filePathOption = databaseFiles.get(rowIndex);
            if (columnIndex == 0) {
                filePathOption.setFile((File) o);
            } else if (columnIndex == 1) {
                filePathOption.setDatabaseName((String) o);
            }

            notifyListeners(rowIndex, rowIndex, columnIndex);
        }
    }

    private DatabaseFile getFilePath(int rowIndex) {
        while (databaseFiles.size() <= rowIndex) {
            databaseFiles.add(new DatabaseFile());
        }
        return databaseFiles.get(rowIndex);
    }

    public DatabaseFilesBundle getDatabaseFiles() {
        return databaseFiles;
    }

    public void insertRow(int rowIndex) {
        databaseFiles.add(rowIndex, new DatabaseFile());
        notifyListeners(rowIndex, databaseFiles.size()-1, -1);
    }

    public void removeRow(int rowIndex) {
        if (databaseFiles.size() > rowIndex) {
            databaseFiles.remove(rowIndex);
            notifyListeners(rowIndex, databaseFiles.size()-1, -1);
        }
    }

    public void validate() {

    }

    /********************************************************
     *                    Disposable                        *
     ********************************************************/
    public void dispose() {
        super.dispose();
    }
}
