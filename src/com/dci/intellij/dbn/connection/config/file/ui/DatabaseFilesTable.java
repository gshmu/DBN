package com.dci.intellij.dbn.connection.config.file.ui;

import com.dci.intellij.dbn.common.ui.component.DBNComponent;
import com.dci.intellij.dbn.common.ui.table.DBNEditableTable;
import com.dci.intellij.dbn.common.ui.table.FileBrowserTableCellEditor;
import com.dci.intellij.dbn.connection.config.file.DatabaseFiles;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.TableColumn;

public class DatabaseFilesTable extends DBNEditableTable<DatabaseFilesTableModel> {

    public DatabaseFilesTable(@NotNull DBNComponent parent, DatabaseFiles databaseFiles) {
        super(parent, new DatabaseFilesTableModel(databaseFiles), false);
        setDefaultRenderer(Object.class, new DatabaseFilesTableCellRenderer());
        getColumnModel().getColumn(0).setCellEditor(new FileBrowserTableCellEditor(new FileChooserDescriptor(true, true, false, false, false, false)));
        setFixedWidth(columnModel.getColumn(1), 100);
    }

    public void setFilePaths(DatabaseFiles filesBundle) {
        super.setModel(new DatabaseFilesTableModel(filesBundle));
        setFixedWidth(columnModel.getColumn(1), 100);
    }

    void setFixedWidth(TableColumn tableColumn, int width) {
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }


    @Override
    public boolean isCellEditable(int row, int column) {
        return !(row == 0 && column == 1);
    }

}
