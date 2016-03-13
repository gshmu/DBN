package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.database.common.util.ResultSetReader;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteResultSetAdapter;

public abstract class SqliteDatasetInfoResultSetStub<T extends ResultSetElement> extends SqliteResultSetAdapter<T> {
    private Connection connection;
    protected String ownerName;
    public SqliteDatasetInfoResultSetStub(final String ownerName, SqliteDatasetNamesResultSet datasetNames, Connection connection) throws SQLException {
        this.connection = connection;
        this.ownerName = ownerName;
        new ResultSetReader(datasetNames) {
            @Override
            protected void processRow(ResultSet resultSet) throws SQLException {
                String datasetName = resultSet.getString("DATASET_NAME");
                init(ownerName, datasetName);
            }
        };
    }

    public SqliteDatasetInfoResultSetStub(String ownerName, String datasetName, Connection connection) throws SQLException {
        this.connection = connection;
        this.ownerName = ownerName;
        init(ownerName, datasetName);
    }

    public Connection getConnection() {
        return connection;
    }

    protected abstract void init(String ownerName, String datasetName) throws SQLException;
}
