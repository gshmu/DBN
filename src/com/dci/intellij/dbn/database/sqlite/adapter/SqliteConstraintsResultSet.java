package com.dci.intellij.dbn.database.sqlite.adapter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

import static com.dci.intellij.dbn.database.sqlite.adapter.SqliteMetaDataUtil.*;

/**
 * DATASET_NAME,
 * CONSTRAINT_NAME,
 * CONSTRAINT_TYPE,
 * FK_CONSTRAINT_OWNER
 * FK_CONSTRAINT_NAME
 * IS_ENABLED
 * CHECK_CONDITION
 */

public abstract class SqliteConstraintsResultSet extends SqliteResultSetAdapter<SqliteConstraintsResultSet.Constraint> {


    public SqliteConstraintsResultSet(ResultSet datasetNames) throws SQLException {
        new ResultSetReader(datasetNames) {
            @Override
            protected void processRow(ResultSet resultSet) throws SQLException {
                String parentName = resultSet.getString(1);
                init(parentName);

            }
        };
    }
    public SqliteConstraintsResultSet(String datasetName) throws SQLException {
        init(datasetName);
    }

    void init(String datasetName) throws SQLException {
        Map<String, List<ConstraintColumnInfo>> constraints = loadConstraints(
                datasetName,
                getColumnsResultSet(datasetName),
                getForeignKeyResultSet(datasetName));

        for (String indexKey : constraints.keySet()) {
            if (indexKey.startsWith("FK")) {
                List<ConstraintColumnInfo> constraintColumnInfos = constraints.get(indexKey);
                String constraintName = getConstraintName(ConstraintType.FK, constraintColumnInfos);
                Constraint constraint = new Constraint();
                constraint.setConstraintName(constraintName);
                constraint.setDatasetName(datasetName);
                constraint.setConstraintType("FOREIGN KEY");
                constraint.setFkConstraintOwner("main");
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

            }
        }
    }

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

    protected abstract ResultSet getColumnsResultSet(String datasetName) throws SQLException;
    protected abstract ResultSet getForeignKeyResultSet(String datasetName) throws SQLException;

    public static class Constraint implements ResultSetElement<Constraint> {
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
    }
}
