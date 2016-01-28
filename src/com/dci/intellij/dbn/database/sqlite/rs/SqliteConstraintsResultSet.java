package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.database.common.util.SkipEntrySQLException;

/**
 * DATASET_NAME,
 * CONSTRAINT_NAME,
 * CONSTRAINT_TYPE,
 * FK_CONSTRAINT_OWNER
 * FK_CONSTRAINT_NAME
 * IS_ENABLED
 * CHECK_CONDITION
 */

public abstract class SqliteConstraintsResultSet extends SqliteResultSetAdapter {


    public SqliteConstraintsResultSet(ResultSet parentResultSet) {
        super(parentResultSet);
    }

    public SqliteConstraintsResultSet(String parentName) {
        super(parentName);
    }

    public String getString(String columnLabel) throws SQLException {
        boolean isUnique = childResultSet.getInt("unique") == 1;
        boolean isPrimaryKey = "pk".equals(childResultSet.getString("origin"));

        if (!isUnique && !isPrimaryKey) {
            throw new SkipEntrySQLException();
        }

        if (columnLabel.equals("CONSTRAINT_NAME")) {
            String indexName = childResultSet.getString("name");
            return indexName + "_c";
        }

        if (columnLabel.equals("CONSTRAINT_TYPE")) {
            if (isPrimaryKey) {
                return "PRIMARY KEY";
            }

            if (isUnique) {
                return "UNIQUE";
            }

        }

        if (columnLabel.equals("DATASET_NAME")) {
            return parentName;
        }

        if (columnLabel.equals("IS_ENABLED")) {
            return "Y";
        }

        return null;
    }
}
