package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseInterface;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.RawIndexDetailInfo;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.RawIndexInfo;

/**
 * COLUMN_NAME
 * INDEX_NAME
 * TABLE_NAME
 * POSITION
 */

public abstract class SqliteColumnIndexesResultSet extends SqliteDatasetInfoResultSetStub<SqliteColumnIndexesResultSet.IndexColumn> {
    protected SqliteColumnIndexesResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, DBNConnection connection) throws SQLException {
        super(ownerName, datasetNames, connection);
    }

    protected SqliteColumnIndexesResultSet(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        super(ownerName, datasetName, connection);
    }

    @Override
    protected void init(String ownerName, String tableName) throws SQLException {
        RawIndexInfo indexInfo = getIndexInfo(tableName);
        for (RawIndexInfo.Row row : indexInfo.getRows()) {
            String indexName = row.getName();
            RawIndexDetailInfo indexDetailInfo = getIndexDetailInfo(indexName);
            for (RawIndexDetailInfo.Row detailRow : indexDetailInfo.getRows()) {
                String columnName = detailRow.getName();
                if (StringUtil.isNotEmpty(columnName)) {
                    IndexColumn indexColumn = new IndexColumn();
                    indexColumn.setTableName(tableName);
                    indexColumn.setIndexName(indexName);
                    indexColumn.setColumnName(columnName);
                    indexColumn.setPosition(detailRow.getSeqno());
                    addElement(indexColumn);
                }
            }
        }
    }

    private RawIndexInfo getIndexInfo(final String tableName) throws SQLException {
        return DatabaseInterface.getMetaDataCache().get(
                ownerName + "." + tableName + ".INDEX_INFO",
                () -> new RawIndexInfo(loadIndexInfo(tableName)));
    }

    private RawIndexDetailInfo getIndexDetailInfo(final String indexName) throws SQLException {
        return DatabaseInterface.getMetaDataCache().get(
                ownerName + "." + indexName + ".INDEX_DETAIL_INFO",
                () -> new RawIndexDetailInfo(loadIndexDetailInfo(indexName)));
    }


    protected abstract ResultSet loadIndexInfo(String tableName) throws SQLException;
    protected abstract ResultSet loadIndexDetailInfo(String indexName) throws SQLException;


    @Override
    public String getString(String columnLabel) throws SQLException {
        IndexColumn element = getCurrentElement();
        return columnLabel.equals("INDEX_NAME") ? element.getIndexName() :
               columnLabel.equals("COLUMN_NAME") ? element.getColumnName() :
               columnLabel.equals("TABLE_NAME") ? element.getTableName() : null;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        IndexColumn element = getCurrentElement();
        return columnLabel.equals("POSITION") ? element.getPosition() : 0;
    }

    public static class IndexColumn implements ResultSetElement<IndexColumn> {
        private String tableName;
        private String indexName;
        private String columnName;
        private int position;

        public String getTableName() {
            return tableName;
        }

        public void setTableName(String tableName) {
            this.tableName = tableName;
        }

        public String getIndexName() {
            return indexName;
        }

        public void setIndexName(String indexName) {
            this.indexName = indexName;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public String getName() {
            return getTableName() + "." + getIndexName() + "." + getColumnName();
        }

        @Override
        public int compareTo(@NotNull IndexColumn indexColumn) {
            return (tableName + "." + indexName + "." + columnName).compareTo(indexColumn.tableName + "." + indexColumn.indexName + "." + indexColumn.columnName);
        }
    }
}
