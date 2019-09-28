package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetadataResultSetRow;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteConstraintInfoResultSetStub.SqliteConstraintsLoader.ConstraintColumnInfo;
import static com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteConstraintInfoResultSetStub.SqliteConstraintsLoader.ConstraintType;
import static com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteConstraintInfoResultSetStub.SqliteConstraintsLoader.getConstraintName;

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
                    constraintColumn.constraintName = constraintName;
                    constraintColumn.datasetName = constraintColumnInfo.getDataset();
                    constraintColumn.columnName = constraintColumnInfo.getColumn();
                    constraintColumn.position = constraintColumnInfo.getPosition();
                    add(constraintColumn);
                }

            } else if (indexKey.startsWith("PK")) {
                String constraintName = getConstraintName(ConstraintType.PK, constraintColumnInfos);
                for (ConstraintColumnInfo constraintColumnInfo : constraintColumnInfos) {
                    ConstraintColumn constraintColumn = new ConstraintColumn();
                    constraintColumn.constraintName = constraintName;
                    constraintColumn.datasetName = datasetName;
                    constraintColumn.columnName = constraintColumnInfo.getColumn();
                    constraintColumn.position = constraintColumnInfo.getPosition();
                    add(constraintColumn);
                }
            } else if (indexKey.startsWith("UQ")) {
                String constraintName = getConstraintName(ConstraintType.UQ, constraintColumnInfos);
                for (ConstraintColumnInfo constraintColumnInfo : constraintColumnInfos) {
                    ConstraintColumn constraintColumn = new ConstraintColumn();
                    constraintColumn.constraintName = constraintName;
                    constraintColumn.datasetName = datasetName;
                    constraintColumn.columnName = constraintColumnInfo.getColumn();
                    constraintColumn.position = constraintColumnInfo.getPosition();
                    add(constraintColumn);
                }
            }
        }
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        ConstraintColumn element = current();
        return columnLabel.equals("CONSTRAINT_NAME") ? element.constraintName :
               columnLabel.equals("COLUMN_NAME") ? element.columnName :
               columnLabel.equals("DATASET_NAME") ? element.datasetName : null;
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        ConstraintColumn element = current();
        return (short) (columnLabel.equals("POSITION") ? element.position : 0);
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        ConstraintColumn element = current();
        return columnLabel.equals("POSITION") ? element.position : 0;
    }

    static class ConstraintColumn implements SqliteMetadataResultSetRow<ConstraintColumn> {
        private String datasetName;
        private String constraintName;
        private String columnName;
        private int position;

        @Override
        public String identifier() {
            return datasetName + "." + constraintName + "." + columnName;
        }
    }
}
