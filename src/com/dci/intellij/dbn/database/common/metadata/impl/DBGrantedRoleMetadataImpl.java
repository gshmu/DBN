package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBGrantedRoleMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBGrantedRoleMetadataImpl extends DBObjectMetadataBase implements DBGrantedRoleMetadata {
    public DBGrantedRoleMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    @Override
    public String getGrantedRoleName() throws SQLException {
        return resultSet.getString("GRANTED_ROLE_NAME");
    }

    @Override
    public String getRoleName() throws SQLException {
        return resultSet.getString("ROLE_NAME");
    }

    @Override
    public String getUserName() throws SQLException {
        return resultSet.getString("USER_NAME");
    }

    @Override
    public boolean isAdminOption() throws SQLException {
        return resultSet.getString("IS_ADMIN_OPTION").equals("Y");
    }

    @Override
    public boolean isDefaultRole() throws SQLException {
        return resultSet.getString("IS_DEFAULT_ROLE").equals("Y");
    }
}
