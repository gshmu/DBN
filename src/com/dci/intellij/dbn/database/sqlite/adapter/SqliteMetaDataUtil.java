package com.dci.intellij.dbn.database.sqlite.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

public class SqliteMetaDataUtil {
    public enum ConstraintType {
        PK,
        FK,
        UQ
    }
    @NotNull
    public static Map<String, List<ConstraintColumnInfo>> loadConstraints(final String dataset, ResultSet columnsRs, ResultSet foreignKeysRs) throws SQLException {
        SqliteMetaDataUtil.TableInfo tableInfo = new SqliteMetaDataUtil.TableInfo(columnsRs);
        SqliteMetaDataUtil.ForeignKeyInfo foreignKeyInfo = new SqliteMetaDataUtil.ForeignKeyInfo(foreignKeysRs);

        Map<String, List<ConstraintColumnInfo>> constraints = new HashMap<String, List<ConstraintColumnInfo>>();
        AtomicInteger pkPosition = new AtomicInteger();

        for (TableInfo.Row row : tableInfo.getRows()) {
            String column = row.getName();
            if (row.getPk() > 0) {
                List<ConstraintColumnInfo> primaryKeyColumns = constraints.get("PK");
                if (primaryKeyColumns == null) {
                    primaryKeyColumns = new ArrayList<ConstraintColumnInfo>();
                    constraints.put("PK", primaryKeyColumns);
                }
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
            List<ConstraintColumnInfo> foreignKeyColumns = constraints.get(indexId);
            if (foreignKeyColumns == null) {
                foreignKeyColumns = new ArrayList<ConstraintColumnInfo>();
                constraints.put(indexId, foreignKeyColumns);
            }
            foreignKeyColumns.add(new ConstraintColumnInfo(dataset, column, fkDataset, fkColumn, position));
        }
        return constraints;
    }

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

    public static class ForeignKeyInfo extends MetaData<ForeignKeyInfo.Row>{

        public ForeignKeyInfo(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            int id;
            int seq;
            String table;
            String from;
            String to;

            Row (ResultSet resultSet) throws SQLException {
                id = resultSet.getInt("id");
                seq = resultSet.getInt("seq");
                table = resultSet.getString("table");
                from = resultSet.getString("from");
                to = resultSet.getString("to");
            }

            public int getId() {
                return id;
            }

            public int getSeq() {
                return seq;
            }

            public String getTable() {
                return table;
            }

            public String getFrom() {
                return from;
            }

            public String getTo() {
                return to;
            }
        }
    }

    public static class IndexInfo extends MetaData<IndexInfo.Row>{

        public IndexInfo(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            int seq;
            int unique;
            int partial;
            String name;
            String origin;

            Row (ResultSet resultSet) throws SQLException {
                seq = resultSet.getInt("seq");
                name = resultSet.getString("name");
                unique = resultSet.getInt("unique");
                partial = resultSet.getInt("partial");
                origin = resultSet.getString("origin");
            }

            public int getSeq() {
                return seq;
            }

            public int getUnique() {
                return unique;
            }

            public int getPartial() {
                return partial;
            }

            public String getName() {
                return name;
            }

            public String getOrigin() {
                return origin;
            }
        }
    }
    public static class IndexDetailInfo extends MetaData<IndexDetailInfo.Row>{

        public IndexDetailInfo(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            int seqno;
            int cid;
            int desc;
            int key;
            String name;
            String coll;

            Row (ResultSet resultSet) throws SQLException {
                seqno = resultSet.getInt("seqno");
                cid = resultSet.getInt("cid");
                key = resultSet.getInt("key");
                name = resultSet.getString("name");
                desc = resultSet.getInt("desc");
                coll = resultSet.getString("coll");
            }

            public int getSeqno() {
                return seqno;
            }

            public int getCid() {
                return cid;
            }

            public int getDesc() {
                return desc;
            }

            public int getKey() {
                return key;
            }

            public String getName() {
                return name;
            }

            public String getColl() {
                return coll;
            }
        }
    }

    public static class TableInfo extends MetaData<TableInfo.Row>{

        public TableInfo(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected Row createRow(ResultSet resultSet) throws SQLException {
            return new Row(resultSet);
        }

        public static class Row {
            int cid;
            String name;
            String type;
            int notnull;
            int pk;

            Row (ResultSet resultSet) throws SQLException {
                cid = resultSet.getInt("cid");
                name = resultSet.getString("name");
                type = resultSet.getString("type");
                notnull = resultSet.getInt("notnull");
                pk = resultSet.getInt("pk");
            }

            public int getCid() {
                return cid;
            }

            public String getName() {
                return name;
            }

            public String getType() {
                return type;
            }

            public int getNotnull() {
                return notnull;
            }

            public int getPk() {
                return pk;
            }
        }
    }

    public abstract static class MetaData<T> extends ResultSetReader {
        private List<T> rows;
        public MetaData(ResultSet resultSet) throws SQLException {
            super(resultSet);
        }

        @Override
        protected final void processRow(ResultSet resultSet) throws SQLException {
            T row = createRow(resultSet);
            getRows().add(row);
        }

        public List<T> getRows() {
            if (rows == null) {
                rows = new ArrayList<T>();
            }
            return rows;
        }

        protected abstract T createRow(ResultSet resultSet) throws SQLException;
    }
}
