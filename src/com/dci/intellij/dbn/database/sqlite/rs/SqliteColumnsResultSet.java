package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * COLUMN_NAME
 * POSITION
 * DATA_TYPE_NAME
 * DATA_TYPE_OWNER
 * DATA_TYPE_PACKAGE
 * DATA_LENGTH
 * DATA_PRECISION
 * DATA_SCALE
 * IS_SET
 * IS_NULLABLE
 * IS_HIDDEN
 * IS_PRIMARY_KEY
 * IS_FOREIGN_KEY
 * IS_UNIQUE_KEY
 */

public abstract class SqliteColumnsResultSet extends SqliteResultSetAdapter {
    public SqliteColumnsResultSet(String parentName) {
        super(parentName);
    }

    public SqliteColumnsResultSet(ResultSet parentResultSet) {
        super(parentResultSet);
    }

    public String getString(String columnLabel) throws SQLException {
        return
                columnLabel.equals("DATASET_NAME") ? parentName :
                columnLabel.equals("COLUMN_NAME") ? childResultSet.getString("name") :
                columnLabel.equals("DATA_TYPE_NAME") ? childResultSet.getString("type") :
                columnLabel.equals("IS_FOREIGN_KEY") ? "N" :
                columnLabel.equals("IS_UNIQUE_KEY") ? "N" :
                columnLabel.equals("IS_HIDDEN") ? "N" :
                columnLabel.equals("IS_SET") ? "N" :
                columnLabel.equals("IS_NULLABLE") ? (childResultSet.getInt("notnull") == 0 ? "Y": "N") :
                columnLabel.equals("IS_PRIMARY_KEY") ? (childResultSet.getInt("pk") == 1 ? "Y" : "N") : null;
    }

    public int getInt(String columnLabel) throws SQLException {
        return
            columnLabel.equals("POSITION") ? childResultSet.getInt("cid") + 1 :
            columnLabel.equals("DATA_LENGTH") ? 0 :
            columnLabel.equals("DATA_PRECISION") ? 0 :
            columnLabel.equals("DATA_SCALE") ? 0 : 0;
    }

    public long getLong(String columnLabel) throws SQLException {
        return getInt(columnLabel);
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        return super.getBoolean(columnLabel);
    }
}
