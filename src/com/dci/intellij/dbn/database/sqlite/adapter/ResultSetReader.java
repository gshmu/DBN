package com.dci.intellij.dbn.database.sqlite.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.connection.ConnectionUtil;

public abstract class ResultSetReader {
    public ResultSetReader(ResultSet resultSet) throws SQLException {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                while (resultSet.next()) {
                    processRow(resultSet);
                }
            }
        } finally {
            ConnectionUtil.closeResultSet(resultSet);
        }
    }

    protected abstract void processRow(ResultSet resultSet) throws SQLException;
}
