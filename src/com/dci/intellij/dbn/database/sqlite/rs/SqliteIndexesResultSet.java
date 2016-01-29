package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * INDEX_NAME,
 * IS_UNIQUE,
 * IS_VALID
 */

public abstract class SqliteIndexesResultSet extends SqliteResultSetAdapter {

    public SqliteIndexesResultSet(ResultSet parentResultSet) {
        super(parentResultSet);
    }

    public SqliteIndexesResultSet(String parentName) {
        super(parentName);
    }

    protected abstract ResultSet loadChildren(String parentName, int index) throws SQLException;

    public String getString(String columnLabel) throws SQLException {
        return
                columnLabel.equals("TABLE_NAME") ? parentName :
                columnLabel.equals("INDEX_NAME") ? childResultSet.getString("name") :
                columnLabel.equals("IS_UNIQUE") ? (childResultSet.getInt("unique") == 1 ? "Y": "N") :
                columnLabel.equals("IS_VALID") ? "Y" : null;
    }

}
