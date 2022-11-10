package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.common.util.ResultSetReader;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetadataResultSet;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetadataResultSetRow;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class SqliteDatasetInfoResultSetStub<T extends SqliteMetadataResultSetRow> extends SqliteMetadataResultSet<T> {
    private DBNConnection connection;
    protected String ownerName;

    SqliteDatasetInfoResultSetStub(final String ownerName, SqliteDatasetNamesResultSet datasetNames, DBNConnection connection) throws SQLException {
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

    SqliteDatasetInfoResultSetStub(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        this.connection = connection;
        this.ownerName = ownerName;
        init(ownerName, datasetName);
    }

    public DBNConnection getConnection() {
        return connection;
    }

    protected abstract void init(String ownerName, String datasetName) throws SQLException;
}
