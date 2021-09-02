package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.data.Data;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.latent.MapLatent;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.dci.intellij.dbn.common.util.CommonUtil.nvl;

public class CachedResultSet extends StatefulDisposable.Base implements ResultSetStub {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private List<CachedResultSetRow> rows = new ArrayList<>();
    private List<String> columnNames;

    public static final CachedResultSet EMPTY = new CachedResultSet(
            Collections.emptyList(),
            Collections.emptyList()){
        @Override
        public CachedResultSet open() {
            return this;
        }

        @Override
        public boolean next() throws SQLException {
            return false;
        }
    };

    private final MapLatent<Condition, CachedResultSet> filtered = MapLatent.create(condition -> {
        if (rows.isEmpty()) {
            return this;
        } else {
            List<CachedResultSetRow> filteredRows = new ArrayList<>();
            for (CachedResultSetRow element : rows) {
                try {
                    if (condition.evaluate(element)) {
                        filteredRows.add(element);
                    }
                } catch (SQLException e) {
                    LOGGER.error("Failed to filter cached result set", e);
                }
            }
            return new CachedResultSet(filteredRows, columnNames);
        }
    });

    private final MapLatent<Columns, CachedResultSet> grouped = MapLatent.create(columns -> {
        if (rows.isEmpty()) {
            return this;
        } else {
            List<CachedResultSetRow> groupedRows = new ArrayList<>();
            try {

                for (CachedResultSetRow row : rows) {
                    CachedResultSetRow matchingRow = find(groupedRows, row, columns);
                    if (matchingRow == null) {
                        groupedRows.add(row.clone(columns));
                    } else {
                        // TODO ignore or pivot the rest of columns?
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("Failed to group cached result set", e);
            }

            return new CachedResultSet(groupedRows, columnNames);
        }
    });

    private CachedResultSet(@Nullable ResultSet source, @Nullable ResultSetCondition condition) throws SQLException {
        if (source instanceof CachedResultSet) {
            CachedResultSet cachedResultSet = (CachedResultSet) source;
            this.columnNames = new ArrayList<>(cachedResultSet.columnNames);
            source = cachedResultSet.open();
            load(source, condition);
        } else if (source != null && !source.isClosed()) {
            try {
                List<String> columnNames = ResultSetUtil.getColumnNames(source);
                this.columnNames = columnNames.stream().map(s -> s.toUpperCase().trim()).collect(Collectors.toList());
                load(source, condition);
            } finally {
                ResourceUtil.close(source);
            }
        }
    }

    private CachedResultSet(@NotNull CachedResultSet source, @NotNull Condition condition) throws SQLException {
        columnNames = new ArrayList<>(source.columnNames);
        for (CachedResultSetRow row : source.rows) {
            if (condition.evaluate(row)) {
                rows.add(row);
            }
        }
    }

    /**
     * Internal constructor for clone on iteration
     */
    private CachedResultSet(List<CachedResultSetRow> rows, List<String> columnNames) {
        this.rows = rows;
        this.columnNames = columnNames;
    }

    /**
     * Load and cache the result set passed in as parameter
     * @param resultSet the result set to cache
     * @param condition the condition to filter out records from original result set
     * @throws SQLException propagated from original result set evaluations
     */
    private void load(@NotNull ResultSet resultSet, @Nullable ResultSetCondition condition) throws SQLException {
        ResultSetUtil.forEachRow(resultSet, () -> {
            if (condition == null || condition.evaluate(resultSet)) {
                CachedResultSetRow row = CachedResultSetRow.create(resultSet, columnNames);
                rows.add(row);
            }
        });
    }

    public static <T> CachedResultSet create(@Nullable ResultSet resultSet) throws SQLException {
        return create(resultSet, null);
    }

    public static <T> CachedResultSet create(@Nullable ResultSet resultSet, @Nullable ResultSetCondition condition) throws SQLException {
        return new CachedResultSet(resultSet, condition);
    }

    public List<CachedResultSetRow> rows() {
        return rows;
    }

    public CachedResultSetRow rowAt(int index) {
        return rows.get(index);
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
        return filtered.get(whereCondition);
    }

    public CachedResultSet filter(@NotNull Condition whereCondition) throws SQLException {
        return new CachedResultSet(this, whereCondition);
    }

    public CachedResultSet filter(@NotNull ResultSetCondition whereCondition) throws SQLException {
        return new CachedResultSet(this, whereCondition);
    }

    public CachedResultSet normalize(Mapper<String> columnMapper) {
        columnNames = columnNames.stream().
                map(columnName -> nvl(columnMapper.map(columnName), columnName)).
                collect(Collectors.toList());
        for (int i = 0; i < rows.size(); i++) {
            CachedResultSetRow row = rows.get(i);
            row.normalize(columnMapper);
        }
        return this;
    }

    public CachedResultSet enrich(String columnName, ColumnValue value) throws SQLException {
        columnNames.add(columnName);
        for (int i = 0; i < rows.size(); i++) {
            CachedResultSetRow row = rows.get(i);
            row.extend(columnName, value.resolve(this, i));
        }
        return this;
    }

    @Nullable
    public CachedResultSetRow first(@NotNull Condition whereCondition) throws SQLException {
        return next(whereCondition, 0);
    }

    /**
     * Next row from index (inclusive) matching the condition
     */
    public CachedResultSetRow next(@NotNull Condition whereCondition, int fromIndex) throws SQLException {
        for (int i = fromIndex; i < rows.size(); i++) {
            CachedResultSetRow row = rows.get(i);
            if (whereCondition.evaluate(row)) {
                return row;
            }
        }
        return null;
    }

    /**
     * Previous row from index (non-inclusive) matching the condition
     */
    public CachedResultSetRow previous(@NotNull Condition whereCondition, int fromIndex) throws SQLException {
        for (int i = fromIndex-1; i >= 0; i--) {
            CachedResultSetRow row = rows.get(i);
            if (whereCondition.evaluate(row)) {
                return row;
            }
        }
        return null;
    }

    public int count(@NotNull Condition whereCondition) throws SQLException {
        int count = 0;
        for (int i = 0; i < rows.size(); i++) {
            CachedResultSetRow row = rows.get(i);
            if (whereCondition.evaluate(row)) {
                count++;
            }
        }
        return count;
    }

    public boolean exists(@NotNull Condition whereCondition) throws SQLException {
        return first(whereCondition) != null;
    }

    @NotNull
    public <T> List<T> list(String columnName, Class<T> columnType) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            CachedResultSetRow row = rows.get(i);
            Object columnValue = row.get(columnName);
            T castedValue = Data.cast(columnValue, columnType);
            list.add(castedValue);
        }

        return list;
    }

    @NotNull
    private CachedResultSet open(List<CachedResultSetRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return EMPTY;
        } else {
            return new CachedResultSet(rows, columnNames) {
                private int cursor = -1;

                @Override
                public boolean next() {
                    if (cursor == rows.size()) {
                        throw new IllegalStateException("Result set exhausted. Call open() again");
                    }
                    cursor++;
                    return cursor < rows.size();
                }

                @Override
                protected CachedResultSetRow current() {
                    return rows.get(cursor);
                }
            };
        }
    }

    public CachedResultSet unionAll(CachedResultSet resultSet) {
        if (isEmpty()) {
            return resultSet;

        } else if (resultSet.isEmpty()) {
            return this;

        } else {
            List<CachedResultSetRow> newRows = new ArrayList<>(rows);
            newRows.addAll(resultSet.rows);
            return new CachedResultSet(newRows, columnNames);
        }
    }

    /**
     * Cached result sets are accessed concurrently
     * Open a copy with it's own cursor index to iterate
     */
    public CachedResultSet open() {
        return open(rows);
    }

    public CachedResultSet groupBy(@NotNull Columns groupByClause) throws SQLException {
        if (rows.isEmpty()) {
            return this ;
        } else {
            return grouped.get(groupByClause);
        }
    }

    private static CachedResultSetRow find(List<CachedResultSetRow> list, CachedResultSetRow match, Columns columns) {
        for (int i = 0; i < list.size(); i++) {
            CachedResultSetRow row = list.get(i);
            if (row.matches(match, columns)) {
                return row;
            }
        }
        return null;
    }

    @FunctionalInterface
    public interface Condition {
        boolean evaluate(CachedResultSetRow row) throws SQLException;

        static Condition in(CachedResultSet source, Columns matchColumns) {
            return row -> source.exists(sourceRow ->
                    sourceRow.matches(row, matchColumns));
        }

        static Condition notIn(CachedResultSet source, Columns matchColumns) {
            return row -> !source.exists(sourceRow ->
                    sourceRow.matches(row, matchColumns));
        }
    }

    @FunctionalInterface
    public interface ColumnValue {
        Object resolve(CachedResultSet resultSet, int index) throws SQLException;
    }

    @FunctionalInterface
    public interface Columns {
        String[] names();
    }

    @FunctionalInterface
    public interface Mapper<T> {
        T map(T original);
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
    public short getShort(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        return Data.asShrt(value);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        return Data.asInt(value);
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        return Data.asLng(value);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        Object value = getObject(columnLabel);
        return Data.asBool(value);
    }

    // TODO add more accessors overrides if needed


    @Override
    protected void disposeInner() {
        nullify();
    }
}
