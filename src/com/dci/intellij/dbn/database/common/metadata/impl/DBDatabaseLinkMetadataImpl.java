package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBDatabaseLinkMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBDatabaseLinkMetadataImpl extends DBObjectMetadataBase implements DBDatabaseLinkMetadata {
    public DBDatabaseLinkMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getDblinkName() throws SQLException {
        return resultSet.getString("DBLINK_NAME");
    }

    public String getUserName() throws SQLException {
        return resultSet.getString("USER_NAME");
    }

    public String getHost() throws SQLException {
        return resultSet.getString("HOST");
    }
}
