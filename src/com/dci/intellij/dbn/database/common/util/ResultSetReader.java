package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.connection.ConnectionUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ResultSetReader {
    public ResultSetReader(ResultSet resultSet) throws SQLException {
        try {
            if (resultSet != null && !ConnectionUtil.isClosed(resultSet)) {
                while (resultSet.next()) {
                    processRow(resultSet);
                }
            }
        } finally {
            ConnectionUtil.close(resultSet);
        }
    }

    protected abstract void processRow(ResultSet resultSet) throws SQLException;
}
