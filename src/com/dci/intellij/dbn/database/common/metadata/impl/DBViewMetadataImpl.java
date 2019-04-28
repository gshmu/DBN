package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBViewMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBViewMetadataImpl extends DBObjectMetadataBase implements DBViewMetadata {

    public DBViewMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getViewName() throws SQLException {
        return resultSet.getString("VIEW_NAME");
    }

    public String getViewType() throws SQLException {
        return resultSet.getString("VIEW_TYPE");
    }

    public String getViewTypeOwner() throws SQLException {
        return resultSet.getString("VIEW_TYPE_OWNER");
    }

    public boolean isSystemView() throws SQLException {
        return resultSet.getString("IS_SYSTEM_VIEW").equals("Y");
    }
}
