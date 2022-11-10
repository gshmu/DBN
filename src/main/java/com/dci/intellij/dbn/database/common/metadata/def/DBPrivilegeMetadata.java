package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBPrivilegeMetadata extends DBObjectMetadata {

    String getPrivilegeName() throws SQLException;
}
