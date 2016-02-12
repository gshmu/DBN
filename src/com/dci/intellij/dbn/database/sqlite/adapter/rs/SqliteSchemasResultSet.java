package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.database.common.util.ResultSetAdapter;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * SCHEMA_NAME
 * IS_PUBLIC
 * IS_SYSTEM
 * IS_EMPTY
 */
public class SqliteSchemasResultSet extends ResultSetAdapter {
    private ResultSet resultSet;

    public SqliteSchemasResultSet(ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        if (columnLabel.equals("SCHEMA_NAME")) {
            return resultSet.getString("name");
        }

        return "N";
    }

    @Override
    public boolean next() throws SQLException {
        return resultSet.next();
    }

    @Override
    public void close() throws SQLException {
        resultSet.close();
    }
}
