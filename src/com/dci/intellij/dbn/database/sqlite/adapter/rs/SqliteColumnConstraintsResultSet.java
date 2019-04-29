package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteConstraintInfoResultSetStub.SqliteConstraintsLoader.*;

/**
 * COLUMN_NAME
 * CONSTRAINT_NAME
 * DATASET_NAME
 * POSITION
 */

public abstract class SqliteColumnConstraintsResultSet extends SqliteConstraintInfoResultSetStub<SqliteColumnConstraintsResultSet.ConstraintColumn> {

    public SqliteColumnConstraintsResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, DBNConnection connection) throws SQLException {
        super(ownerName, datasetNames, connection);
    }

    public SqliteColumnConstraintsResultSet(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        super(ownerName, datasetName, connection);
    }

    @Override
    protected void init(String ownerName, String datasetName) throws SQLException {
        Map<String, List<ConstraintColumnInfo>> constraints = loadConstraintInfo(ownerName, datasetName);

        for (String indexKey : constraints.keySet()) {
            List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
            if (indexKey.startsWith("FK")) {
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
                String constraintName = getConstraintName(ConstraintType.PK, constraintColumnInfos);
                for (ConstraintColumnInfo constraintColumnInfo : constraintColumnInfos) {
                    ConstraintColumn constraintColumn = new ConstraintColumn();
                    constraintColumn.setConstraintName(constraintName);
                    constraintColumn.setDatasetName(datasetName);
                    constraintColumn.setColumnName(constraintColumnInfo.getColumn());
                    constraintColumn.setPosition(constraintColumnInfo.getPosition());
                    addElement(constraintColumn);
                }
            } else if (indexKey.startsWith("UQ")) {
                String constraintName = getConstraintName(ConstraintType.UQ, constraintColumnInfos);
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

    @Override
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

    static class ConstraintColumn implements ResultSetElement<ConstraintColumn> {
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
            return (datasetName + "." + constraintName + "." + columnName).
                    compareTo(constraintColumn.datasetName + "." + constraintColumn.constraintName + "." + constraintColumn.columnName);
        }
    }
}
