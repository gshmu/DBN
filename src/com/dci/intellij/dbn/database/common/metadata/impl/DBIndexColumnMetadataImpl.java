package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBIndexColumnMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBIndexColumnMetadataImpl extends DBObjectMetadataBase implements DBIndexColumnMetadata {

    public DBIndexColumnMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getIndexName() throws SQLException {
        return resultSet.getString("INDEX_NAME");
    }

    @Override
    public String getColumnName() throws SQLException {
        return resultSet.getString("COLUMN_NAME");
    }

    public String getTableName() throws SQLException {
        return resultSet.getString("TABLE_NAME");
    }
}
