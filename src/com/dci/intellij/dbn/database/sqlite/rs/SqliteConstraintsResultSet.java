package com.dci.intellij.dbn.database.sqlite.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

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
        return
            columnLabel.equals("DATASET_NAME") ? parentName :
            columnLabel.equals("CONSTRAINT_NAME") ? childResultSet.getString("name") :
            columnLabel.equals("CONSTRAINT_NAME") ? childResultSet.getString("name") :
            columnLabel.equals("IS_ENABLED") ? "Y" : null;
    }
}
