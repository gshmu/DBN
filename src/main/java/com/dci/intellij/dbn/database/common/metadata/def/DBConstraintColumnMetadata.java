package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBConstraintColumnMetadata extends DBObjectMetadata {

    String getConstraintName() throws SQLException;

    String getColumnName() throws SQLException;

    String getDatasetName() throws SQLException;

    short getPosition() throws SQLException;

}
