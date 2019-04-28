package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBTableMetadata extends DBObjectMetadata {

    String getTableName() throws SQLException;

    boolean isTemporary() throws SQLException;
}
