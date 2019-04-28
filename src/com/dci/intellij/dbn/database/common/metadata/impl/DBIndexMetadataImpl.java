package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBIndexMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBIndexMetadataImpl extends DBObjectMetadataBase implements DBIndexMetadata {

    public DBIndexMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getIndexName() throws SQLException {
        return resultSet.getString("INDEX_NAME");
    }

    public String getTableName() throws SQLException {
        return resultSet.getString("TABLE_NAME");
    }

    public boolean isUnique() throws SQLException {
        return resultSet.getString("IS_UNIQUE").equals("Y");
    }

    public boolean isValid() throws SQLException {
        return resultSet.getString("IS_VALID").equals("Y");
    }

}
