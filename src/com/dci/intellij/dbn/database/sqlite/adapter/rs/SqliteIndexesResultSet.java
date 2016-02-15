package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.cache.CacheAdapter;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.RawIndexInfo;


/**
 * TABLE_NAME,
 * INDEX_NAME,
 * IS_UNIQUE,
 * IS_VALID
 */

public abstract class SqliteIndexesResultSet extends SqliteDatasetInfoResultSetStub<SqliteIndexesResultSet.Index> {

    public SqliteIndexesResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, Connection connection) throws SQLException {
        super(ownerName, datasetNames, connection);
    }

    public SqliteIndexesResultSet(String ownerName, String datasetName, Connection connection) throws SQLException {
        super(ownerName, datasetName, connection);
    }

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
        return new CacheAdapter<RawIndexInfo, SQLException>(getCache()) {
            @Override
            protected RawIndexInfo load() throws SQLException {
                return new RawIndexInfo(loadIndexInfo(tableName));
            }
        }.get(tableName + ".INDEX_INFO");
    }

    protected abstract ResultSet loadIndexInfo(String tableName) throws SQLException;

    public String getString(String columnLabel) throws SQLException {
        Index index = getCurrentElement();
        return
                columnLabel.equals("TABLE_NAME") ? index.getTableName() :
                columnLabel.equals("INDEX_NAME") ? index.getIndexName() :
                columnLabel.equals("IS_UNIQUE") ? toFlag(index.isUnique()) :
                columnLabel.equals("IS_VALID") ? "Y" : null;
    }


    public static class Index implements ResultSetElement<Index> {
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
