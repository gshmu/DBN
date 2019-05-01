package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.language.common.WeakRef;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CachedResultSetRow {
    private WeakRef<CachedResultSet> resultSet;
    private List<Object> columns = new ArrayList<>();

    private CachedResultSetRow(CachedResultSet parent, ResultSet inner) throws SQLException {
        this.resultSet = WeakRef.from(parent);
        for (String columnName : parent.columnNames()) {
            Object columnValue = inner.getObject(columnName);
            columns.add(columnValue);
        }
    }

    public static CachedResultSetRow create(CachedResultSet parent, ResultSet inner) throws SQLException {
        return new CachedResultSetRow(parent, inner);
    }

    public Object get(String columnName) {
        CachedResultSet resultSet = getResultSet();
        if (resultSet != null) {
            int index = resultSet.columnIndex(columnName);
            if (index > -1 && index < columns.size()) {
                return columns.get(index);
            }
        }
        return null;
    }

    @Nullable
    protected CachedResultSet getResultSet() {
        return resultSet.get();
    }
}
