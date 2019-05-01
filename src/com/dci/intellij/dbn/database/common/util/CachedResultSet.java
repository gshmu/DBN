package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.common.filter.Filter;
import com.dci.intellij.dbn.common.list.FiltrableListImpl;
import com.dci.intellij.dbn.common.routine.ThrowableConsumer;
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
    public void close() throws SQLException {
        // source already closed;
    }

    @Override
    public boolean isClosed() throws SQLException {
        return false; // cached - never closed
    }

    /**
     * Cached result sets are accessed concurrently
     * Open a copy with it's own cursor index to iterate
     */
    public CachedResultSet open() {
        return open(null, null);
    }

    public CachedResultSet groupBy(@NotNull GroupBy groupByClause) {
        return open(null, groupByClause);
    }

    public CachedResultSet where(@NotNull Where whereCondition) {
        return open(whereCondition, null);
    }

    public CachedResultSet open(@Nullable Where whereCondition, @Nullable GroupBy groupByClause) {
        List<CachedResultSetRow> rows = whereCondition == null ? this.rows : new FiltrableListImpl<>(this.rows, whereCondition);

        // TODO group rows using clause
        return new CachedResultSet(rows, columnNames) {
            private int cursor = -1;

            @Override
            public boolean next() throws SQLException {
                cursor++;
                return cursor < rows.size();
            }

            @Override
            protected CachedResultSetRow current() {
                return rows.get(cursor);
            }
        };
    }

    public interface Where extends Filter<CachedResultSetRow> {}

    public interface GroupBy {
        String[] columnNames();
    }

    public <V> void forEachRow(String columnName, Class<V> columnType, ThrowableConsumer<V, SQLException> consumer) throws SQLException {
        ResultSetUtil.forEachRow(this, columnName, columnType, consumer);
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
        return value == null ? null : value.toString();
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        return value == null ? 0 :
                value instanceof Integer ? (int) value :
                        Integer.valueOf(value.toString());
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        if (value instanceof Number) {
            Number number = (Number) value;
            return number.intValue() != 0;

        } else if (value instanceof Boolean) {
            return (boolean) value;
        }
        return false;
    }

    // TODO add more accessors overrides if needed

}
