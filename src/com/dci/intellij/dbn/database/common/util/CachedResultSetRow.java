package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.common.dispose.DisposableBase;
import com.dci.intellij.dbn.common.dispose.Nullifiable;
import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Nullifiable
public class CachedResultSetRow extends DisposableBase {
    private CachedResultSet resultSet;
    private List<Object> columns = new ArrayList<>();


    private CachedResultSetRow(CachedResultSet parent, @Nullable ResultSet inner) throws SQLException {
        this.resultSet = parent;
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
        if (resultSet != null) {
            int index = resultSet.columnIndex(columnName);
            if (index > -1 && index < columns.size()) {
                return columns.get(index);
            }
        }
        return null;
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
        CachedResultSetRow clone = new CachedResultSetRow(resultSet, null);
        for (String columnName : resultSet.columnNames()) {
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

    @Override
    public void disposeInner() {
        resultSet = null;
    }
}
