package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBNestedTableMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBNestedTableMetadataImpl extends DBObjectMetadataBase implements DBNestedTableMetadata {

    public DBNestedTableMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    @Override
    public String getNestedTableName() throws SQLException {
        return resultSet.getString("NESTED_TABLE_NAME");
    }

    @Override
    public String getTableName() throws SQLException {
        return resultSet.getString("TABLE_NAME");
    }

    @Override
    public String getDataTypeName() throws SQLException {
        return resultSet.getString("DATA_TYPE_NAME");
    }

    @Override
    public String getDataTypeOwner() throws SQLException {
        return resultSet.getString("DATA_TYPE_OWNER");
    }
}
