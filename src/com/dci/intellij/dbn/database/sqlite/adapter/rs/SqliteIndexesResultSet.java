package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.dci.intellij.dbn.common.cache.CacheAdapter;
import com.dci.intellij.dbn.database.common.util.ResultSetReader;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteResultSetAdapter;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.IndexInfo;


/**
 * TABLE_NAME,
 * INDEX_NAME,
 * IS_UNIQUE,
 * IS_VALID
 */

public abstract class SqliteIndexesResultSet extends SqliteResultSetAdapter<SqliteIndexesResultSet.Index> {

    public SqliteIndexesResultSet(ResultSet tableName) throws SQLException {
        new ResultSetReader(tableName) {
            @Override
            protected void processRow(ResultSet resultSet) throws SQLException {
                String parentName = resultSet.getString(1);
                init(parentName);

            }
        };
    }
    public SqliteIndexesResultSet(String tableName) throws SQLException {
        init(tableName);
    }

    void init(String tableName) throws SQLException {
        IndexInfo indexInfo = getIndexInfo(tableName);

        for (IndexInfo.Row row : indexInfo.getRows()) {
            Index element = new Index();
            element.setTableName(tableName);
            element.setIndexName(row.getName());
            element.setUnique(row.getUnique() == 1);
            element.setValid(true);
            addElement(element);
        }
    }

    private IndexInfo getIndexInfo(final String tableName) throws SQLException {
        return new CacheAdapter<IndexInfo, SQLException>(getCache()) {
            @Override
            protected IndexInfo load() throws SQLException {
                return new IndexInfo(loadIndexInfo(tableName));
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
    }
}
