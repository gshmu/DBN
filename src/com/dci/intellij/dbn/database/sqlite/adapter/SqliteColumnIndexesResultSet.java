package com.dci.intellij.dbn.database.sqlite.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.StringUtil;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.IndexDetailInfo;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.IndexInfo;

/**
 * COLUMN_NAME
 * INDEX_NAME
 * TABLE_NAME
 * POSITION
 */

public abstract class SqliteColumnIndexesResultSet extends SqliteResultSetAdapter<SqliteColumnIndexesResultSet.IndexColumn> {

    public SqliteColumnIndexesResultSet(ResultSet tableNames) throws SQLException {
        new ResultSetReader(tableNames) {
            @Override
            protected void processRow(ResultSet resultSet) throws SQLException {
                String tableName = resultSet.getString(1);
                init(tableName);

            }
        };
    }
    public SqliteColumnIndexesResultSet(String tableName) throws SQLException {
        init(tableName);
    }

    void init(String tableName) throws SQLException {
        IndexInfo indexInfo = new IndexInfo(getIndexResultSet(tableName));
        for (IndexInfo.Row row : indexInfo.getRows()) {
            String indexName = row.getName();
            IndexDetailInfo indexDetailInfo = new IndexDetailInfo(getIndexInfoResultSet(indexName));
            for (IndexDetailInfo.Row detailRow : indexDetailInfo.getRows()) {
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

    protected abstract ResultSet getIndexResultSet(String tableName) throws SQLException;
    protected abstract ResultSet getIndexInfoResultSet(String indexName) throws SQLException;


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
