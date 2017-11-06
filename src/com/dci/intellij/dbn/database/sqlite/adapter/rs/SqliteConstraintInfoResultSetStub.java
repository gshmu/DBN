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
import com.dci.intellij.dbn.connection.DBNConnection;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteRawMetaData.*;

public abstract class SqliteConstraintInfoResultSetStub<T extends ResultSetElement<T>> extends SqliteDatasetInfoResultSetStub<T> {

    public SqliteConstraintInfoResultSetStub(String ownerName, SqliteDatasetNamesResultSet datasetNames, DBNConnection connection) throws SQLException {
        super(ownerName, datasetNames, connection);
    }

    public SqliteConstraintInfoResultSetStub(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        super(ownerName, datasetName, connection);
    }

    protected abstract void init(String ownerName, String datasetName) throws SQLException;

    protected abstract ResultSet loadTableInfo(String ownerName, String datasetName) throws SQLException;
    protected abstract ResultSet loadForeignKeyInfo(String ownerName, String datasetName) throws SQLException;
    protected abstract ResultSet loadIndexInfo(String ownerName, String tableName) throws SQLException;
    protected abstract ResultSet loadIndexDetailInfo(String ownerName, String indexName) throws SQLException;

    protected Map<String, List<SqliteConstraintsLoader.ConstraintColumnInfo>> loadConstraintInfo(final String ownerName, String datasetName) throws SQLException {
        SqliteConstraintsLoader loader = new SqliteConstraintsLoader(ownerName, getCache()) {
            @Override
            public ResultSet loadTableInfo(String datasetName) throws SQLException {
                return SqliteConstraintInfoResultSetStub.this.loadTableInfo(ownerName, datasetName);
            }

            @Override
            public ResultSet loadForeignKeyInfo(String datasetName) throws SQLException {
                return SqliteConstraintInfoResultSetStub.this.loadForeignKeyInfo(ownerName, datasetName);
            }

            @Override
            public ResultSet loadIndexInfo(String tableName) throws SQLException {
                return SqliteConstraintInfoResultSetStub.this.loadIndexInfo(ownerName, tableName);
            }

            @Override
            public ResultSet loadIndexDetailInfo(String indexName) throws SQLException {
                return SqliteConstraintInfoResultSetStub.this.loadIndexDetailInfo(ownerName, indexName);
            }
        };
        return loader.loadConstraints(datasetName);
    }



    public abstract static class SqliteConstraintsLoader {
        private Cache cache;
        private String ownerName;

        public enum ConstraintType {
            PK,
            FK,
            UQ
        }

        public SqliteConstraintsLoader(String ownerName, Cache cache) {
            this.ownerName = ownerName;
            this.cache = cache;
        }

        @NotNull
        public Map<String, List<ConstraintColumnInfo>> loadConstraints(final String dataset) throws SQLException {
            RawTableInfo tableInfo = getTableInfo(dataset);
            RawForeignKeyInfo foreignKeyInfo = getForeignKeyInfo(dataset);
            RawIndexInfo indexInfo = getIndexInfo(dataset);

            Map<String, List<ConstraintColumnInfo>> constraints = new HashMap<String, List<ConstraintColumnInfo>>();
            AtomicInteger pkPosition = new AtomicInteger();

            for (RawTableInfo.Row row : tableInfo.getRows()) {
                String column = row.getName();
                if (row.getPk() > 0) {
                    List<ConstraintColumnInfo> primaryKeyColumns = getConstraintColumns(constraints, "PK");
                    primaryKeyColumns.add(new ConstraintColumnInfo(dataset, column, null, null, pkPosition.get()));
                    pkPosition.incrementAndGet();
                }

            }

            for (RawForeignKeyInfo.Row row : foreignKeyInfo.getRows()) {
                String column = row.getFrom();
                String fkColumn = row.getTo();
                String fkDataset = row.getTable();
                int position = row.getSeq();
                String indexId = "FK" + row.getId();
                List<ConstraintColumnInfo> foreignKeyColumns = getConstraintColumns(constraints, indexId);
                foreignKeyColumns.add(new ConstraintColumnInfo(dataset, column, fkDataset, fkColumn, position));
            }

            for (RawIndexInfo.Row row : indexInfo.getRows()) {
                if (row.getUnique() == 1 && !"pk".equals(row.getOrigin())) {
                    String indexId = "UQ" + row.getSeq();
                    String indexName = row.getName();
                    RawIndexDetailInfo detailInfo = getIndexDetailInfo(indexName);
                    for (RawIndexDetailInfo.Row detailRow : detailInfo.getRows()) {
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

        private RawForeignKeyInfo getForeignKeyInfo(final String datasetName) throws SQLException {
            return new CacheAdapter<RawForeignKeyInfo, SQLException>(cache) {
                @Override
                protected RawForeignKeyInfo load() throws SQLException {
                    return new RawForeignKeyInfo(loadForeignKeyInfo(datasetName));
                }
            }.get(ownerName + "." + datasetName + ".FOREIGN_KEY_INFO");
        }

        private RawTableInfo getTableInfo(final String datasetName) throws SQLException {
            return new CacheAdapter<RawTableInfo, SQLException>(cache) {
                @Override
                protected RawTableInfo load() throws SQLException {
                    return new RawTableInfo(loadTableInfo(datasetName));
                }
            }.get(ownerName + "." + datasetName + ".TABLE_INFO");
        }

        private RawIndexInfo getIndexInfo(final String tableName) throws SQLException {
            return new CacheAdapter<RawIndexInfo, SQLException>(cache) {
                @Override
                protected RawIndexInfo load() throws SQLException {
                    return new RawIndexInfo(loadIndexInfo(tableName));
                }
            }.get(ownerName + "." + tableName + ".INDEX_INFO");
        }

        private RawIndexDetailInfo getIndexDetailInfo(final String indexName) throws SQLException {
            return new CacheAdapter<RawIndexDetailInfo, SQLException>(cache) {
                @Override
                protected RawIndexDetailInfo load() throws SQLException {
                    return new RawIndexDetailInfo(loadIndexDetailInfo(indexName));
                }
            }.get(ownerName + "." + indexName + ".INDEX_DETAIL_INFO");
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
