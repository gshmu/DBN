package com.dci.intellij.dbn.editor.data.filter;

import com.dci.intellij.dbn.object.DBColumn;
import com.dci.intellij.dbn.object.DBDataset;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatasetFilterInput {
    private final DBDataset dataset;
    private final List<DBColumn> columns = new ArrayList<>();
    private final Map<DBColumn, Object> values = new HashMap<>();

    public DatasetFilterInput(DBDataset dataset) {
        this.dataset = dataset;
    }

    public DBDataset getDataset() {
        return dataset;
    }

    public List<DBColumn> getColumns() {
        return columns;
    }

    public void setColumnValue(@NotNull DBColumn column, Object value) {
        columns.add(column);
        values.put(column, value);
    }

    public Object getColumnValue(DBColumn column) {
        return values.get(column);
    }
}
