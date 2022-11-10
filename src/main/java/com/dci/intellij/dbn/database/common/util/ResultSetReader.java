package com.dci.intellij.dbn.database.common.util;

import com.dci.intellij.dbn.connection.ResultSets;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ResultSetReader {
    public ResultSetReader(ResultSet resultSet) throws SQLException {
        ResultSets.forEachRow(resultSet, () -> processRow(resultSet));
    }

    protected abstract void processRow(ResultSet resultSet) throws SQLException;
}
