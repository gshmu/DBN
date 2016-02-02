package com.dci.intellij.dbn.database.sqlite.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.ForeignKeyInfo;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.TableInfo;

public abstract class SqliteConstraintsLoader {

    public enum ConstraintType {
        PK,
        FK,
        UQ
    }

    @NotNull
    public Map<String, List<ConstraintColumnInfo>> loadConstraints(final String dataset) throws SQLException {
        TableInfo tableInfo = new TableInfo(getColumns(dataset));
        ForeignKeyInfo foreignKeyInfo = new ForeignKeyInfo(getForeignKeys(dataset));

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

    public abstract ResultSet getColumns(String datasetName) throws SQLException;
    public abstract ResultSet getForeignKeys(String datasetName) throws SQLException;
    public abstract ResultSet getIndexes(String tableName) throws SQLException;
    public abstract ResultSet getIndexDetails(String indexName) throws SQLException;

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
