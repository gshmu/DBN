package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.common.data.Data;
import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FilteredList;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CachedResultSet extends ResultSetStub {
    private List<CachedResultSetRow> rows = new ArrayList<>();
    private List<String> columnNames;

    private CachedResultSet(@Nullable ResultSet resultSet) throws SQLException {
        if (resultSet != null) {
            try {
                columnNames = ResultSetUtil.getColumnNames(resultSet);
                ResultSetUtil.forEachRow(resultSet,
                        () -> {
                            CachedResultSetRow row = CachedResultSetRow.create(this, resultSet);
                            rows.add(row);
                        });
            } finally {
                ResourceUtil.close(resultSet);
            }
        }
    }

    public static <T> CachedResultSet create(@Nullable ResultSet resultSet) throws SQLException {
        return new CachedResultSet(resultSet);
    }

    /**
     * Internal constructor for clone on iteration
     */
    private CachedResultSet(List<CachedResultSetRow> rows, List<String> columnNames) {
        this.rows = rows;
        this.columnNames = columnNames;
    }

    List<String> columnNames() {
        return columnNames;
    }

    int columnIndex(String columnLabel) {
        return columnNames.indexOf(columnLabel);
    }

    public List<CachedResultSetRow> rows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
    /**
     * Current row at cursor
     */
    protected CachedResultSetRow current() {
        throw new IllegalStateException("Call open() on CachedResultSet to initialize isolated iteration (avoid concurrency issues)");
    }

    @Override
    public void close() {
        // source already closed;
    }

    @Override
    public boolean isClosed() {
        return false; // cached - never closed
    }

    /**************************************************************
     *                          Utilities                         *
     **************************************************************/
    public CachedResultSet where(@NotNull Condition whereCondition) {
        List<CachedResultSetRow> rows = new FilteredList<>(this.rows, whereCondition);
        return open(rows);
    }

    public boolean exists(@NotNull Condition whereCondition) {
        for (CachedResultSetRow row : this.rows) {
            if (whereCondition.accepts(row)) {
                return true;
            }
        }
        return false;
    }

    public <T> List<T> list(String columnName, Class<T> columnType) {
        List<T> list = new ArrayList<>();
        for (CachedResultSetRow row : this.rows) {
            Object columnValue = row.get(columnName);
            T castedValue = Data.cast(columnValue, columnType);
            list.add(castedValue);
        }

        return list;
    }

    @NotNull
    private CachedResultSet open(List<CachedResultSetRow> rows) {
        return new CachedResultSet(rows, columnNames) {
            private int cursor = -1;

            @Override
            public boolean next() {
                cursor++;
                return cursor < rows.size();
            }

            @Override
            protected CachedResultSetRow current() {
                return rows.get(cursor);
            }
        };
    }

    /**
     * Cached result sets are accessed concurrently
     * Open a copy with it's own cursor index to iterate
     */
    public CachedResultSet open() {
        return open(rows);
    }

    public CachedResultSet groupBy(@NotNull GroupBy groupByClause) throws SQLException {
        if (rows.isEmpty()) {
            return open(rows);
        } else {
            List<CachedResultSetRow> groupedRows = new ArrayList<>();
            String[] columnNames = groupByClause.columnNames();

            for (CachedResultSetRow row : rows) {
                CachedResultSetRow matchingRow = find(groupedRows, row, columnNames);
                if (matchingRow == null) {
                    groupedRows.add(row.clone(columnNames));
                } else {
                    // TODO ignore or pivot the rest of columns?
                }
            }

            return open(groupedRows);
        }
    }

    private static CachedResultSetRow find(List<CachedResultSetRow> list, CachedResultSetRow match, String[] columnNames) {
        for (CachedResultSetRow row : list) {
            if (row.matches(match, columnNames)) {
                return row;
            }
        }
        return null;
    }

    public interface Condition extends Filter<CachedResultSetRow> {}

    public interface GroupBy {
        String[] columnNames();
    }

    /**************************************************************
     *            Value accessor implementations                  *
     **************************************************************/
    @Override
    public Object getObject(String columnLabel) throws SQLException {
        CachedResultSetRow currentRow = current();
        return currentRow.get(columnLabel);
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        return Data.asString(value);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        return Data.asInt(value);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        return Data.asBool(value);
    }

    // TODO add more accessors overrides if needed

}
