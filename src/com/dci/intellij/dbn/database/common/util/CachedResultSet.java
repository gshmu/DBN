package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.common.routine.ThrowableConsumer;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CachedResultSet<T> extends ResultSetStub {
    private List<T> rows = new ArrayList<>();
    private ResultSetTranslator<T> translator;

    private CachedResultSet(@Nullable ResultSet resultSet, ResultSetTranslator<T> translator) throws SQLException {
        this.translator = translator;
        if (resultSet != null) {
            try {
                List<String> columnNames = ResultSetUtil.getColumnNames(resultSet);

                ResultSetUtil.forEachRow(resultSet, () -> {
                            T row = translator.read(resultSet, columnNames);
                            rows.add(row);
                        });
            } finally {
                ResourceUtil.close(resultSet);
            }
        }
    }

    public static <T> CachedResultSet<T> create(@Nullable ResultSet resultSet, ResultSetTranslator<T> translator) throws SQLException {
        return new CachedResultSet<T>(resultSet, translator);
    }

    /**
     * Internal constructor for clone on iteration
     */
    private CachedResultSet(List<T> rows, ResultSetTranslator<T> translator) {
        this.rows = rows;
        this.translator = translator;
    }

    public void add(T row) {
        rows.add(row);
    }

    public List<T> rows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
    /**
     * Current row at cursor
     */
    protected T current() {
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
     * Cached result sets may be used concurrently
     * Open a copy with it's own cursor index to iterate
     */
    public CachedResultSet scrollable() {
        return new CachedResultSet<T>(rows, translator) {
            private int cursor = -1;

            @Override
            public boolean next() throws SQLException {
                cursor++;
                return cursor < rows.size();
            }

            @Override
            protected T current() {
                return rows.get(cursor);
            }
        };
    }

    public <V> void forEachRow(String columnName, Class<V> columnType, ThrowableConsumer<V, SQLException> consumer) throws SQLException {
        ResultSetUtil.forEachRow(this, columnName, columnType, consumer);
    }

    /**************************************************************
     *            Value accessor implementations                  *
     **************************************************************/
    @Override
    public Object getObject(String columnLabel) throws SQLException {
        return translator.value(current(), columnLabel);
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


    // TODO add more accessors overrides if needed

}
