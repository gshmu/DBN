package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.language.common.WeakRef;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CachedResultSetRow {
    private WeakRef<CachedResultSet> resultSet;
    private List<Object> columns = new ArrayList<>();


    private CachedResultSetRow(CachedResultSet parent, @Nullable ResultSet inner) throws SQLException {
        this.resultSet = WeakRef.from(parent);
        if (inner != null) {
            for (String columnName : parent.columnNames()) {
                Object columnValue = inner.getObject(columnName);
                columns.add(columnValue);
            }
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

    public boolean matches(CachedResultSetRow that, String[] columnNames) {
        for (String columnName : columnNames) {
            Object thisColumnValue = this.get(columnName);
            Object thatColumnValue = that.get(columnName);
            if (!CommonUtil.safeEqual(thisColumnValue, thatColumnValue)) {
                return false;
            }
        }
        return true;
    }

    public CachedResultSetRow clone(String[] columnNames) throws SQLException {
        CachedResultSetRow clone = new CachedResultSetRow(getResultSet(), null);
        for (String columnName : getResultSet().columnNames()) {
            if (StringUtil.isOneOf(columnName, columnNames)) {
                Object columnValue = get(columnName);
                clone.columns.add(columnValue);
            } else {
                clone.columns.add(null);
            }
        }
        return clone;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Object column : columns) {
            if (builder.length() > 0) builder.append(" / ");
            builder.append(column);
        }

        return builder.toString();
    }
}
