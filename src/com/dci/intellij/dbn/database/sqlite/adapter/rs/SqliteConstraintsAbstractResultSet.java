package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.cache.Cache;
import com.dci.intellij.dbn.common.cache.CacheAdapter;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.database.common.util.ResultSetReader;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteResultSetAdapter;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.*;

public abstract class SqliteConstraintsAbstractResultSet<T extends ResultSetElement<T>> extends SqliteResultSetAdapter<T> {

    public SqliteConstraintsAbstractResultSet(ResultSet datasetNames) throws SQLException {
        new ResultSetReader(datasetNames) {
            @Override
            protected void processRow(ResultSet resultSet) throws SQLException {
                String parentName = resultSet.getString(1);
                init(parentName);

            }
        };
    }

    public SqliteConstraintsAbstractResultSet(String datasetName) throws SQLException {
        init(datasetName);
    }

    protected abstract void init(String parentName) throws SQLException;

    protected abstract ResultSet loadTableInfo(String datasetName) throws SQLException;
    protected abstract ResultSet loadForeignKeyInfo(String datasetName) throws SQLException;
    protected abstract ResultSet loadIndexInfo(String tableName) throws SQLException;
    protected abstract ResultSet loadIndexDetailInfo(String indexName) throws SQLException;

    protected Map<String, List<SqliteConstraintsLoader.ConstraintColumnInfo>> loadConstraintInfo(String datasetName) throws SQLException {
        SqliteConstraintsLoader loader = new SqliteConstraintsLoader(getCache()) {
            @Override
            public ResultSet loadTableInfo(String datasetName) throws SQLException {
                return SqliteConstraintsAbstractResultSet.this.loadTableInfo(datasetName);
            }

            @Override
            public ResultSet loadForeignKeyInfo(String datasetName) throws SQLException {
                return SqliteConstraintsAbstractResultSet.this.loadForeignKeyInfo(datasetName);
            }

            @Override
            public ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteConstraintsAbstractResultSet.this.loadIndexInfo(tableName);
            }

            @Override
            public ResultSet loadIndexDetailInfo(String indexName) throws SQLException {
                return SqliteConstraintsAbstractResultSet.this.loadIndexDetailInfo(indexName);
            }
        };
        return loader.loadConstraints(datasetName);
    }



    public abstract static class SqliteConstraintsLoader {
        Cache cache;

        public enum ConstraintType {
            PK,
            FK,
            UQ
        }

        public SqliteConstraintsLoader(Cache cache) {
            this.cache = cache;
        }

        @NotNull
        public Map<String, List<ConstraintColumnInfo>> loadConstraints(final String dataset) throws SQLException {
            TableInfo tableInfo = getTableInfo(dataset);
            ForeignKeyInfo foreignKeyInfo = getForeignKeyInfo(dataset);
            IndexInfo indexInfo = getIndexInfo(dataset);

            Map<String, List<ConstraintColumnInfo>> constraints = new HashMap<String, List<ConstraintColumnInfo>>();
            AtomicInteger pkPosition = new AtomicInteger();

            for (TableInfo.Row row : tableInfo.getRows()) {
                String column = row.getName();
                if (row.getPk() > 0) {
                    List<ConstraintColumnInfo> primaryKeyColumns = getConstraintColumns(constraints, "PK");
                    primaryKeyColumns.add(new ConstraintColumnInfo(dataset, column, null, null, pkPosition.get()));
                    pkPosition.incrementAndGet();
                }

            }

            for (ForeignKeyInfo.Row row : foreignKeyInfo.getRows()) {
                String column = row.getFrom();
                String fkColumn = row.getTo();
                String fkDataset = row.getTable();
                int position = row.getSeq();
                String indexId = "FK" + row.getId();
                List<ConstraintColumnInfo> foreignKeyColumns = getConstraintColumns(constraints, indexId);
                foreignKeyColumns.add(new ConstraintColumnInfo(dataset, column, fkDataset, fkColumn, position));
            }

            for (IndexInfo.Row row : indexInfo.getRows()) {
                if (row.getUnique() == 1 && !row.getOrigin().equals("pk")) {
                    String indexId = "UQ" + row.getSeq();
                    String indexName = row.getName();
                    IndexDetailInfo detailInfo = getIndexDetailInfo(indexName);
                    for (IndexDetailInfo.Row detailRow : detailInfo.getRows()) {
                        String column = detailRow.getName();
                        if (StringUtil.isNotEmpty(column)) {
                            int position = detailRow.getSeqno();
                            List<ConstraintColumnInfo> uniqueKeyColumns = getConstraintColumns(constraints, indexId);
                            uniqueKeyColumns.add(new ConstraintColumnInfo(dataset, column, null, null, position));
                        }
                    }
                }
            }

            return constraints;
        }

        private ForeignKeyInfo getForeignKeyInfo(final String datasetName) throws SQLException {
            return new CacheAdapter<ForeignKeyInfo, SQLException>(cache) {
                @Override
                protected ForeignKeyInfo load() throws SQLException {
                    return new ForeignKeyInfo(loadForeignKeyInfo(datasetName));
                }
            }.get(datasetName + ".FOREIGN_KEY_INFO");
        }

        private TableInfo getTableInfo(final String datasetName) throws SQLException {
            return new CacheAdapter<TableInfo, SQLException>(cache) {
                @Override
                protected TableInfo load() throws SQLException {
                    return new TableInfo(loadTableInfo(datasetName));
                }
            }.get(datasetName + ".TABLE_INFO");
        }

        private IndexInfo getIndexInfo(final String tableName) throws SQLException {
            return new CacheAdapter<IndexInfo, SQLException>(cache) {
                @Override
                protected IndexInfo load() throws SQLException {
                    return new IndexInfo(loadIndexInfo(tableName));
                }
            }.get(tableName + ".INDEX_INFO");
        }

        private IndexDetailInfo getIndexDetailInfo(final String indexName) throws SQLException {
            return new CacheAdapter<IndexDetailInfo, SQLException>(cache) {
                @Override
                protected IndexDetailInfo load() throws SQLException {
                    return new IndexDetailInfo(loadIndexDetailInfo(indexName));
                }
            }.get(indexName + ".INDEX_DETAIL_INFO");
        }

        @NotNull
        List<ConstraintColumnInfo> getConstraintColumns(Map<String, List<ConstraintColumnInfo>> constraints, String indexId) {
            List<ConstraintColumnInfo> foreignKeyColumns = constraints.get(indexId);
            if (foreignKeyColumns == null) {
                foreignKeyColumns = new ArrayList<ConstraintColumnInfo>();
                constraints.put(indexId, foreignKeyColumns);
            }
            return foreignKeyColumns;
        }

        public abstract ResultSet loadTableInfo(String datasetName) throws SQLException;
        public abstract ResultSet loadForeignKeyInfo(String datasetName) throws SQLException;
        public abstract ResultSet loadIndexInfo(String tableName) throws SQLException;
        public abstract ResultSet loadIndexDetailInfo(String indexName) throws SQLException;

        public static String getConstraintName(ConstraintType constraintType, List<ConstraintColumnInfo> constraintColumnInfos) {
            StringBuilder builder = new StringBuilder();
            for (ConstraintColumnInfo constraintColumnInfo : constraintColumnInfos) {
                if (builder.length() == 0) {
                    builder.append(constraintType.name().toLowerCase());
                    builder.append("_");
                    builder.append(constraintType == ConstraintType.FK ?
                            constraintColumnInfo.getFkDataset() :
                            constraintColumnInfo.getDataset());
                }
                builder.append("_");
                builder.append(constraintType == ConstraintType.FK ?
                        constraintColumnInfo.getFkColumn() :
                        constraintColumnInfo.getColumn());
            }
            return builder.toString().toLowerCase();
        }

        public static class ConstraintColumnInfo {
            String dataset;
            String column;
            String fkDataset;
            String fkColumn;
            int position;

            public ConstraintColumnInfo(String dataset, String column, String fkDataset, String fkColumn, int position) {
                this.dataset = dataset;
                this.column = column;
                this.fkDataset = fkDataset;
                this.fkColumn = fkColumn;
                this.position = position;
            }

            public String getDataset() {
                return dataset;
            }

            public String getColumn() {
                return column;
            }

            public String getFkDataset() {
                return fkDataset;
            }

            public String getFkColumn() {
                return fkColumn;
            }

            public int getPosition() {
                return position;
            }
        }
    }
}
