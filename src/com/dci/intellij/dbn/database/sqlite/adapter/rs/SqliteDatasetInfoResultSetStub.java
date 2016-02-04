package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.database.common.util.ResultSetReader;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteResultSetAdapter;

public abstract class SqliteDatasetInfoResultSetStub<T extends ResultSetElement> extends SqliteResultSetAdapter<T> {
    private Connection connection;
    public SqliteDatasetInfoResultSetStub(SqliteDatasetNamesResultSet datasetNames, Connection connection) throws SQLException {
        this.connection = connection;
        new ResultSetReader(datasetNames) {
            @Override
            protected void processRow(ResultSet resultSet) throws SQLException {
                String datasetName = resultSet.getString("DATASET_NAME");
                init(datasetName);
            }
        };
    }

    public SqliteDatasetInfoResultSetStub(String datasetName, Connection connection) throws SQLException {
        this.connection = connection;
        init(datasetName);
    }

    public Connection getConnection() {
        return connection;
    }

    protected abstract void init(String datasetName) throws SQLException;
}
