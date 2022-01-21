package com.dci.intellij.dbn.connection.mapping.ui;

import com.dci.intellij.dbn.common.ui.table.DBNReadonlyTableModel;
import com.dci.intellij.dbn.common.util.Safe;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMapping;
import com.intellij.openapi.Disposable;

import java.util.List;

public class FileConnectionMappingTableModel implements DBNReadonlyTableModel<FileConnectionMapping>, Disposable {
    public static final String[] COLUMNS = {"File", "Connection", "Schema", "Session"};
    private final List<FileConnectionMapping> mappings;

    public FileConnectionMappingTableModel(List<FileConnectionMapping> mappings) {
        this.mappings = mappings;
    }

    @Override
    public final int getRowCount() {
        return mappings.size();
    }

    @Override
    public final int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public final String getColumnName(int columnIndex) {
        return COLUMNS[columnIndex];
    }

    @Override
    public final Class<?> getColumnClass(int columnIndex) {
        return FileConnectionMapping.class;
    }

    @Override
    public final Object getValueAt(int rowIndex, int columnIndex) {
        return mappings.get(rowIndex);
    }

    @Override
    public Object getValue(FileConnectionMapping row, int column) {
        switch (column) {
            case 0: return row.getFile();
            case 1: return row.getConnection();
            case 2: return row.getSchemaId();
            case 3: return row.getSession();
        }
        return "";
    }

    @Override
    public String getPresentableValue(FileConnectionMapping row, int column) {
        switch (column) {
            case 0: return Safe.call(row.getFile(), f -> f.getPath(), "");
            case 1: return Safe.call(row.getConnection(), c -> c.getName(), "");
            case 2: return Safe.call(row.getSchemaId(), s -> s.getName(), "");
            case 3: return Safe.call(row.getSession(), s -> s.getName(), "");
        }
        return "";
    }

    @Override
    public void dispose() {
        mappings.clear();
    }
}
