package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBGrantedPrivilegeMetadata extends DBObjectMetadata {

    String getUserName() throws SQLException;

    String getGrantedPrivilegeName() throws SQLException;

    String getRoleName() throws SQLException;

    boolean isAdminOption() throws SQLException;
}
