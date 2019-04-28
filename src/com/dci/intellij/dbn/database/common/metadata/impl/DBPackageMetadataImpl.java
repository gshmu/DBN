package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.def.DBPackageMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBPackageMetadataImpl extends DBProgramMetadataImpl implements DBPackageMetadata {

    public DBPackageMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    public String getPackageName() throws SQLException {
        return resultSet.getString("PACKAGE_NAME");
    }
}
