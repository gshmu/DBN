package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.connection.ResourceUtil;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ResultSetReader {
    public ResultSetReader(ResultSet resultSet) throws SQLException {
        try {
            if (resultSet != null && !ResourceUtil.isClosed(resultSet)) {
                while (resultSet.next()) {
                    processRow(resultSet);
                }
            }
        } finally {
            ResourceUtil.close(resultSet);
        }
    }

    protected abstract void processRow(ResultSet resultSet) throws SQLException;
}
