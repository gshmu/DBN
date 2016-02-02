package com.dci.intellij.dbn.database.sqlite.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.database.common.util.ResultSetReader;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteConstraintsLoader.ConstraintColumnInfo;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteConstraintsLoader.ConstraintType;
import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteConstraintsLoader.getConstraintName;

/**
 * COLUMN_NAME
 * CONSTRAINT_NAME
 * DATASET_NAME
 * POSITION
 */

public abstract class SqliteColumnConstraintsResultSet extends SqliteResultSetAdapter<SqliteColumnConstraintsResultSet.ConstraintColumn> {

    public SqliteColumnConstraintsResultSet(ResultSet datasetNames) throws SQLException {
        new ResultSetReader(datasetNames) {
            @Override
            protected void processRow(ResultSet resultSet) throws SQLException {
                String parentName = resultSet.getString(1);
                init(parentName);

            }
        };
    }
    public SqliteColumnConstraintsResultSet(String datasetName) throws SQLException {
        init(datasetName);
    }

    void init(String datasetName) throws SQLException {
        SqliteConstraintsLoader loader = new SqliteConstraintsLoader() {
            @Override
            public ResultSet getColumns(String datasetName) throws SQLException {
                return getColumnsResultSet(datasetName);
            }

            @Override
            public ResultSet getForeignKeys(String datasetName) throws SQLException {
                return getForeignKeyResultSet(datasetName);
            }

            @Override
            public ResultSet getIndexes(String tableName) throws SQLException {
                return getIndexResultSet(tableName);
            }

            @Override
            public ResultSet getIndexDetails(String indexName) throws SQLException {
                return getIndexInfoResultSet(indexName);
            }
        };
        Map<String, List<ConstraintColumnInfo>> constraints = loader.loadConstraints(datasetName);

        for (String indexKey : constraints.keySet()) {
            if (indexKey.startsWith("FK")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.FK, constraintColumnInfos);
                for (ConstraintColumnInfo constraintColumnInfo : constraintColumnInfos) {
                    ConstraintColumn constraintColumn = new ConstraintColumn();
                    constraintColumn.setConstraintName(constraintName);
                    constraintColumn.setDatasetName(constraintColumnInfo.getDataset());
                    constraintColumn.setColumnName(constraintColumnInfo.getColumn());
                    constraintColumn.setPosition(constraintColumnInfo.getPosition());
                    addElement(constraintColumn);
                }

            } else if (indexKey.startsWith("PK")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.PK, constraintColumnInfos);
                for (ConstraintColumnInfo constraintColumnInfo : constraintColumnInfos) {
                    ConstraintColumn constraintColumn = new ConstraintColumn();
                    constraintColumn.setConstraintName(constraintName);
                    constraintColumn.setDatasetName(datasetName);
                    constraintColumn.setColumnName(constraintColumnInfo.getColumn());
                    constraintColumn.setPosition(constraintColumnInfo.getPosition());
                    addElement(constraintColumn);
                }
            }
        }
    }

    protected abstract ResultSet getColumnsResultSet(String datasetName) throws SQLException;
    protected abstract ResultSet getForeignKeyResultSet(String datasetName) throws SQLException;
    protected abstract ResultSet getIndexResultSet(String tableName) throws SQLException;
    protected abstract ResultSet getIndexInfoResultSet(String indexName) throws SQLException;

    public String getString(String columnLabel) throws SQLException {
        ConstraintColumn element = getCurrentElement();
        return columnLabel.equals("CONSTRAINT_NAME") ? element.getConstraintName() :
               columnLabel.equals("COLUMN_NAME") ? element.getColumnName() :
               columnLabel.equals("DATASET_NAME") ? element.getDatasetName() : null;
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        ConstraintColumn element = getCurrentElement();
        return columnLabel.equals("POSITION") ? element.getPosition() : 0;
    }

    public static class ConstraintColumn implements ResultSetElement<ConstraintColumn> {
        private String datasetName;
        private String constraintName;
        private String columnName;
        private int position;

        public String getDatasetName() {
            return datasetName;
        }

        public void setDatasetName(String datasetName) {
            this.datasetName = datasetName;
        }

        public String getConstraintName() {
            return constraintName;
        }

        public void setConstraintName(String constraintName) {
            this.constraintName = constraintName;
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
            return getDatasetName() + "." + getConstraintName() + "." + getColumnName();
        }

        @Override
        public int compareTo(@NotNull ConstraintColumn constraintColumn) {
            return (datasetName + "." + constraintName + "." + columnName).compareTo(constraintColumn.datasetName + "." + constraintColumn.constraintName + "." + constraintColumn.columnName);
        }
    }
}
