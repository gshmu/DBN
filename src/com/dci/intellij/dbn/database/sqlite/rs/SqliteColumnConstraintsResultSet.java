package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.lang.StringUtils;

import com.dci.intellij.dbn.database.common.util.SkipEntrySQLException;

/**
 * COLUMN_NAME,
 * INDEX_NAME
 */

public abstract class SqliteColumnConstraintsResultSet extends SqliteResultSetAdapter {

    public SqliteColumnConstraintsResultSet(ResultSet parentResultSet) {
        super(parentResultSet);
    }

    public SqliteColumnConstraintsResultSet(String parentName) {
        super(parentName);
    }

    protected abstract ResultSet loadChildren(String parentName) throws SQLException;

    public String getString(String columnLabel) throws SQLException {
        if (columnLabel.equals("COLUMN_NAME")) {
            String name = childResultSet.getString("name");
            if (StringUtils.isEmpty(name)) {
                throw new SkipEntrySQLException();
            }
            return name;
        }

        if (columnLabel.equals("CONSTRAINT_NAME")) {
            return parentName;
        }

        if (columnLabel.equals("DATASET_NAME")) {
            return parentResultSet.getString("DATASET_NAME");
        }

        return null;
    }

}
