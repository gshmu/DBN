package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBSchemaMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBSchemaMetadataImpl extends DBObjectMetadataBase implements DBSchemaMetadata {
    public DBSchemaMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getSchemaName() throws SQLException {
        return resultSet.getString("SCHEMA_NAME");
    }

    public boolean isPublic() throws SQLException {
        return resultSet.getString("IS_PUBLIC").equals("Y");
    }

    public boolean isSystem() throws SQLException {
        return resultSet.getString("IS_SYSTEM").equals("Y");
    }

    public boolean isEmpty() throws SQLException {
        return resultSet.getString("IS_EMPTY").equals("Y");
    }
}
