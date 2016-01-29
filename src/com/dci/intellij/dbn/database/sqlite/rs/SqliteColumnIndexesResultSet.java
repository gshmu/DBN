package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;

import com.dci.intellij.dbn.database.common.util.SkipEntrySQLException;

/**
 * COLUMN_NAME
 * INDEX_NAME
 * TABLE_NAME
 */

public abstract class SqliteColumnIndexesResultSet extends SqliteResultSetAdapter {

    public SqliteColumnIndexesResultSet(ResultSet parentResultSet) {
        super(parentResultSet);
    }

    protected abstract ResultSet loadChildren(String parentName, int index) throws SQLException;

    public String getString(String columnLabel) throws SQLException {
        if (columnLabel.equals("COLUMN_NAME")) {
            String name = childResultSet.getString("name");
            if (StringUtils.isEmpty(name)) {
                throw new SkipEntrySQLException();
            }
            return name;
        }

        if (columnLabel.equals("INDEX_NAME")) {
            return parentName;
        }

        if (columnLabel.equals("TABLE_NAME")) {
            return parentResultSet.getString("TABLE_NAME");
        }

        return null;
    }

}
