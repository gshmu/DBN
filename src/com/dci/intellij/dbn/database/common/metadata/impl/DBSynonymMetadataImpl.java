package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBSynonymMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBSynonymMetadataImpl extends DBObjectMetadataBase implements DBSynonymMetadata {

    public DBSynonymMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getSynonymName() throws SQLException {
        return resultSet.getString("SYNONYM_NAME");
    }

    @Override
    public String getUnderlyingObjectOwner() throws SQLException {
        return resultSet.getString("OBJECT_OWNER");
    }

    @Override
    public String getUnderlyingObjectName() throws SQLException {
        return resultSet.getString("OBJECT_NAME");
    }

    @Override
    public String getUnderlyingObjectType() throws SQLException {
        return resultSet.getString("OBJECT_TYPE");
    }

    @Override
    public boolean isValid() throws SQLException {
        return resultSet.getString("IS_VALID").equals("Y");
    }
}
