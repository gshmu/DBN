package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetadataResultSetRow;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.RawIndexInfo;


/**
 * TABLE_NAME,
 * INDEX_NAME,
 * IS_UNIQUE,
 * IS_VALID
 */

public abstract class SqliteIndexesResultSet extends SqliteDatasetInfoResultSetStub<SqliteIndexesResultSet.Index> {

    protected SqliteIndexesResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, DBNConnection connection) throws SQLException {
        super(ownerName, datasetNames, connection);
    }

    protected SqliteIndexesResultSet(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        super(ownerName, datasetName, connection);
    }

    @Override
    protected void init(String ownerName, String tableName) throws SQLException {
        RawIndexInfo indexInfo = getIndexInfo(tableName);

        for (RawIndexInfo.Row row : indexInfo.getRows()) {
            Index element = new Index();
            element.tableName = tableName;
            element.indexName = row.getName();
            element.unique = row.getUnique() == 1;
            element.valid = true;
            add(element);
        }
    }

    private RawIndexInfo getIndexInfo(final String tableName) throws SQLException {
        return cache().get(
                ownerName + "." + tableName + ".INDEX_INFO",
                () -> new RawIndexInfo(loadIndexInfo(tableName)));
    }

    protected abstract ResultSet loadIndexInfo(String tableName) throws SQLException;

    @Override
    public String getString(String columnLabel) throws SQLException {
        Index index = current();
        return
                columnLabel.equals("TABLE_NAME") ? index.tableName :
                columnLabel.equals("INDEX_NAME") ? index.indexName :
                columnLabel.equals("IS_UNIQUE") ? toFlag(index.unique) :
                columnLabel.equals("IS_VALID") ? "Y" : null;
    }


    static class Index implements SqliteMetadataResultSetRow<Index> {
        private String tableName;
        private String indexName;
        private boolean unique;
        private boolean valid;

        @Override
        public String identifier() {
            return tableName + "." + indexName;
        }

        @Override
        public String toString() {
            return "[INDEX] \"" + tableName + "\".\"" + indexName + "\"";
        }
    }
}
