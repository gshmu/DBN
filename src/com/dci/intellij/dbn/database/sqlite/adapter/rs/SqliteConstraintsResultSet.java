package com.dci.intellij.dbn.database.sqlite.adapter.rs;

import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.sqlite.adapter.ResultSetElement;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static com.dci.intellij.dbn.database.sqlite.adapter.rs.SqliteConstraintInfoResultSetStub.SqliteConstraintsLoader.*;

/**
 * DATASET_NAME,
 * CONSTRAINT_NAME,
 * CONSTRAINT_TYPE,
 * FK_CONSTRAINT_OWNER
 * FK_CONSTRAINT_NAME
 * IS_ENABLED
 * CHECK_CONDITION
 */

public abstract class SqliteConstraintsResultSet extends SqliteConstraintInfoResultSetStub<SqliteConstraintsResultSet.Constraint> {


    public SqliteConstraintsResultSet(String ownerName, SqliteDatasetNamesResultSet datasetNames, DBNConnection connection) throws SQLException {
        super(ownerName, datasetNames, connection);
    }

    public SqliteConstraintsResultSet(String ownerName, String datasetName, DBNConnection connection) throws SQLException {
        super(ownerName, datasetName, connection);
    }

    @Override
    protected void init(String ownerName, String datasetName) throws SQLException {
        Map<String, List<ConstraintColumnInfo>> constraints = loadConstraintInfo(ownerName, datasetName);

        for (String indexKey : constraints.keySet()) {
            if (indexKey.startsWith("FK")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.FK, constraintColumnInfos);
                Constraint constraint = new Constraint();
                constraint.setConstraintName(constraintName);
                constraint.setDatasetName(datasetName);
                constraint.setConstraintType("FOREIGN KEY");
                constraint.setFkConstraintOwner(ownerName);
                constraint.setFkConstraintName(constraintName.replace("fk_", "pk_"));
                addElement(constraint);
            } else if (indexKey.startsWith("PK")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.PK, constraintColumnInfos);
                Constraint constraint = new Constraint();
                constraint.setConstraintName(constraintName);
                constraint.setDatasetName(datasetName);
                constraint.setConstraintType("PRIMARY KEY");
                addElement(constraint);
            } else if (indexKey.startsWith("UQ")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.UQ, constraintColumnInfos);
                Constraint constraint = new Constraint();
                constraint.setConstraintName(constraintName);
                constraint.setDatasetName(datasetName);
                constraint.setConstraintType("UNIQUE");
                addElement(constraint);
            }
        }
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        Constraint element = getCurrentElement();
        return
            columnLabel.equals("DATASET_NAME") ? element.getDatasetName() :
            columnLabel.equals("CONSTRAINT_NAME") ? element.getConstraintName() :
            columnLabel.equals("CONSTRAINT_TYPE") ? element.getConstraintType() :
            columnLabel.equals("CHECK_CONDITION") ? element.getCheckCondition() :
            columnLabel.equals("FK_CONSTRAINT_OWNER") ? element.getFkConstraintOwner() :
            columnLabel.equals("FK_CONSTRAINT_NAME") ? element.getFkConstraintName() :
            columnLabel.equals("IS_ENABLED") ? "Y" : null;
    }

    static class Constraint implements ResultSetElement<Constraint> {
        String datasetName;
        String constraintName;
        String constraintType;
        String checkCondition;
        String fkConstraintOwner;
        String fkConstraintName;

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

        public String getConstraintType() {
            return constraintType;
        }

        public void setConstraintType(String constraintType) {
            this.constraintType = constraintType;
        }

        public String getFkConstraintOwner() {
            return fkConstraintOwner;
        }

        public void setFkConstraintOwner(String fkConstraintOwner) {
            this.fkConstraintOwner = fkConstraintOwner;
        }

        public String getFkConstraintName() {
            return fkConstraintName;
        }

        public void setFkConstraintName(String fkConstraintName) {
            this.fkConstraintName = fkConstraintName;
        }

        public String getCheckCondition() {
            return checkCondition;
        }

        public void setCheckCondition(String checkCondition) {
            this.checkCondition = checkCondition;
        }

        @Override
        public String getName() {
            return getDatasetName() + "." + getConstraintName();
        }

        @Override
        public int compareTo(@NotNull Constraint constraint) {
            return (datasetName + "." + constraintName).compareTo(constraint.datasetName + "." + constraint.constraintName);
        }

        @Override
        public String toString() {
            return "[CONSTRAINT] \"" + datasetName + "\".\"" + constraintName + "\"";
        }
    }
}
