package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBNestedTableMetadata extends DBObjectMetadata {

    String getNestedTableName() throws SQLException;

    String getTableName() throws SQLException;

    String getDataTypeName() throws SQLException;

    String getDataTypeOwner() throws SQLException;
}
