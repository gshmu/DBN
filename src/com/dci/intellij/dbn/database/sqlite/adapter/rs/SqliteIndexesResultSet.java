package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;

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
            element.setTableName(tableName);
            element.setIndexName(row.getName());
            element.setUnique(row.getUnique() == 1);
            element.setValid(true);
            addElement(element);
        }
    }

    private RawIndexInfo getIndexInfo(final String tableName) throws SQLException {
        return DatabaseInterface.getMetaDataCache().get(
                ownerName + "." + tableName + ".INDEX_INFO",
                () -> new RawIndexInfo(loadIndexInfo(tableName)));
    }

    protected abstract ResultSet loadIndexInfo(String tableName) throws SQLException;

    @Override
    public String getString(String columnLabel) throws SQLException {
        Index index = getCurrentElement();
        return
                columnLabel.equals("TABLE_NAME") ? index.getTableName() :
                columnLabel.equals("INDEX_NAME") ? index.getIndexName() :
                columnLabel.equals("IS_UNIQUE") ? toFlag(index.isUnique()) :
                columnLabel.equals("IS_VALID") ? "Y" : null;
    }


    static class Index implements ResultSetElement<Index> {
        String tableName;
        String indexName;
        boolean unique;
        boolean valid;

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public void setUnique(boolean unique) {
            this.unique = unique;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getTableName() {
            return tableName;
        }

        public String getIndexName() {
            return indexName;
        }

        public boolean isUnique() {
            return unique;
        }

        public boolean isValid() {
            return valid;
        }

        @Override
        public String getName() {
            return tableName + "." + indexName;
        }

        @Override
        public int compareTo(Index index) {
            return (tableName + "." + indexName).compareTo(index.tableName + "." + index.indexName);
        }

        @Override
        public String toString() {
            return "[INDEX] \"" + tableName + "\".\"" + indexName + "\"";
        }
    }
}
