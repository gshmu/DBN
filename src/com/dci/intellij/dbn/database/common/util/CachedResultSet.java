package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.data.Data;
import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.latent.MapLatent;
import com.dci.intellij.dbn.connection.ResourceUtil;
import com.dci.intellij.dbn.connection.ResultSetUtil;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.dci.intellij.dbn.common.util.CommonUtil.nvl;

@Nullifiable
public class CachedResultSet extends DisposableBase implements ResultSetStub {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private static final CachedResultSetRow[] EMPTY_ROWS = new CachedResultSetRow[0];
    private CachedResultSetRow[] rows = EMPTY_ROWS;
    private List<String> columnNames;

    public static final CachedResultSet EMPTY = new CachedResultSet(
            EMPTY_ROWS,
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

    private MapLatent<Condition, CachedResultSet> filtered = MapLatent.create(condition -> {
        if (rows.length == 0) {
            return this;
        } else {
            List<CachedResultSetRow> filteredRows = new ArrayList<>();
            for (int i = 0; i < rows.length; i++) {
                CachedResultSetRow element = rows[i];
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

    private MapLatent<Columns, CachedResultSet> grouped = MapLatent.create(groupBy -> {
        if (rows.length == 0) {
            return this;
        } else {
            List<CachedResultSetRow> groupedRows = new ArrayList<>();
            try {
                String[] columnNames = groupBy.names();

                for (CachedResultSetRow row : rows) {
                    CachedResultSetRow matchingRow = find(groupedRows, row, columnNames);
                    if (matchingRow == null) {
                        groupedRows.add(row.clone(columnNames));
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

    private CachedResultSet(@Nullable ResultSet resultSet, @Nullable ResultSetCondition condition) throws SQLException {
        if (resultSet != null) {
            try {
                if (resultSet instanceof CachedResultSet) {
                    CachedResultSet cachedResultSet = (CachedResultSet) resultSet;
                    this.columnNames = cachedResultSet.columnNames;
                    resultSet = cachedResultSet.open();
                } else {
                    List<String> columnNames = ResultSetUtil.getColumnNames(resultSet);
                    this.columnNames = columnNames.stream().map(s -> s.toUpperCase().trim()).collect(Collectors.toList());
                }

                this.rows = array(load(resultSet, condition));
            } finally {
                ResourceUtil.close(resultSet);
            }
        }
    }

    private CachedResultSet(@NotNull CachedResultSet resultSet, @NotNull Condition condition) throws SQLException {
        List<CachedResultSetRow> rows = new ArrayList<>();
        for (CachedResultSetRow row : resultSet.rows) {
            if (condition.evaluate(row)) {
                rows.add(row);
            }
        }
        this.rows = array(rows);
    }

    /**
     * Internal constructor for clone on iteration
     */
    private CachedResultSet(List<CachedResultSetRow> rows, List<String> columnNames) {
        this(array(rows), columnNames);
    }

    @NotNull
    private static CachedResultSetRow[] array(List<CachedResultSetRow> rows) {
        return rows.toArray(new CachedResultSetRow[0]);
    }

    private CachedResultSet(CachedResultSetRow[] rows, List<String> columnNames) {
        this.rows = rows;
        this.columnNames = columnNames;
    }

    /**
     * Load and cache the result set passed in as parameter
     * @param resultSet the result set to cache
     * @param condition the condition to filter out records from original result set
     * @return a list of {@link CachedResultSetRow}
     * @throws SQLException propagated from original result set evaluations
     */
    private List<CachedResultSetRow> load(@NotNull ResultSet resultSet, @Nullable ResultSetCondition condition) throws SQLException {
        List<CachedResultSetRow> rows = new ArrayList<>();
        ResultSetUtil.forEachRow(resultSet, () -> {
            if (condition == null || condition.evaluate(resultSet)) {
                CachedResultSetRow row = CachedResultSetRow.create(this, resultSet);
                rows.add(row);
            }
        });
        return rows;
    }

    public static <T> CachedResultSet create(@Nullable ResultSet resultSet) throws SQLException {
        return create(resultSet, null);
    }

    public static <T> CachedResultSet create(@Nullable ResultSet resultSet, @Nullable ResultSetCondition condition) throws SQLException {
        return new CachedResultSet(resultSet, condition);
    }

    List<String> columnNames() {
        return columnNames;
    }

    int columnIndex(String columnLabel) {
        return columnNames.indexOf(columnLabel);
    }

    public CachedResultSetRow[] rows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows.length == 0;
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
        return this;
    }

    @Nullable
    public CachedResultSetRow first(@NotNull Condition whereCondition) throws SQLException {
        for (int i = 0; i < rows.length; i++) {
            CachedResultSetRow row = rows[i];
            if (whereCondition.evaluate(row)) {
                return row;
            }
        }
        return null;
    }

    public boolean exists(@NotNull Condition whereCondition) throws SQLException {
        return first(whereCondition) != null;
    }

    @NotNull
    public <T> List<T> list(String columnName, Class<T> columnType) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < rows.length; i++) {
            CachedResultSetRow row = rows[i];
            Object columnValue = row.get(columnName);
            T castedValue = Data.cast(columnValue, columnType);
            list.add(castedValue);
        }

        return list;
    }

    @NotNull
    private CachedResultSet open(CachedResultSetRow[] rows) {
        if (rows == null || rows.length == 0) {
            return EMPTY;
        } else {
            return new CachedResultSet(rows, columnNames) {
                private int cursor = -1;

                @Override
                public boolean next() {
                    if (cursor == rows.length) {
                        throw new IllegalStateException("Result set exhausted. Call open() again");
                    }
                    cursor++;
                    return cursor < rows.length;
                }

                @Override
                protected CachedResultSetRow current() {
                    return rows[cursor];
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
            List<CachedResultSetRow> newRows = new ArrayList<>(Arrays.asList(rows));
            newRows.addAll(Arrays.asList(resultSet.rows));
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
        if (rows.length == 0) {
            return this ;
        } else {
            return grouped.get(groupByClause);
        }
    }

    private static CachedResultSetRow find(List<CachedResultSetRow> list, CachedResultSetRow match, String[] columnNames) {
        for (int i = 0; i < list.size(); i++) {
            CachedResultSetRow row = list.get(i);
            if (row.matches(match, columnNames)) {
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
                    sourceRow.matches(row, matchColumns.names()));
        }

        static Condition notIn(CachedResultSet source, Columns matchColumns) {
            return row -> !source.exists(sourceRow ->
                    sourceRow.matches(row, matchColumns.names()));
        }
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
}
