package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBConstraintMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConstraintMetadataImpl extends DBObjectMetadataBase implements DBConstraintMetadata {

    public DBConstraintMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    @Override
    public String getConstraintName() throws SQLException {
        return resultSet.getString("CONSTRAINT_NAME");
    }

    @Override
    public String getDatasetName() throws SQLException {
        return resultSet.getString("DATASET_NAME");
    }

    @Override
    public String getConstraintType() throws SQLException {
        return resultSet.getString("CONSTRAINT_TYPE");
    }


    @Override
    public String getCheckCondition() throws SQLException {
        return resultSet.getString("CHECK_CONDITION");
    }

    @Override
    public String getFkConstraintOwner() throws SQLException {
        return resultSet.getString("FK_CONSTRAINT_OWNER");
    }

    @Override
    public String getFkConstraintName() throws SQLException {
        return resultSet.getString("FK_CONSTRAINT_NAME");
    }

    @Override
    public boolean isEnabled() throws SQLException {
        return "Y".equals(resultSet.getString("IS_ENABLED"));
    }


/*
        String name = metadata.getString("CONSTRAINT_NAME");
        checkCondition = metadata.getString("CHECK_CONDITION");

        String typeString = metadata.getString("CONSTRAINT_TYPE");
        constraintType =
            typeString == null ? -1 :
            typeString.equals("CHECK")? DBConstraint.CHECK :
            typeString.equals("UNIQUE") ? DBConstraint.UNIQUE_KEY :
            typeString.equals("PRIMARY KEY") ? DBConstraint.PRIMARY_KEY :
            typeString.equals("FOREIGN KEY") ? DBConstraint.FOREIGN_KEY :
            typeString.equals("VIEW CHECK") ? DBConstraint.VIEW_CHECK :
            typeString.equals("VIEW READONLY") ? DBConstraint.VIEW_READONLY : -1;

        if (checkCondition == null && constraintType == CHECK) checkCondition = "";

        if (isForeignKey()) {
            String fkOwner = metadata.getString("FK_CONSTRAINT_OWNER");
            String fkName = metadata.getString("FK_CONSTRAINT_NAME");

            ConnectionHandler connectionHandler = getCache();
            DBSchema schema = connectionHandler.getObjectBundle().getSchema(fkOwner);
            if (schema != null) {
                DBObjectRef<DBSchema> schemaRef = schema.getRef();
                foreignKeyConstraint = new DBObjectRef<>(schemaRef, CONSTRAINT, fkName);
            }
        }
 */
}
