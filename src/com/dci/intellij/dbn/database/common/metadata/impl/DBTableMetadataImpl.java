package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBTableMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTableMetadataImpl extends DBObjectMetadataBase implements DBTableMetadata {

    public DBTableMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getTableName() throws SQLException {
        return resultSet.getString("TABLE_NAME");
    }

    public boolean isTemporary() throws SQLException {
        return resultSet.getString("IS_TEMPORARY").equals("Y");
    }

}
