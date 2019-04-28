package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBSynonymMetadata extends DBObjectMetadata {

    String getSynonymName() throws SQLException;

    String getUnderlyingObjectOwner() throws SQLException;

    String getUnderlyingObjectName() throws SQLException;

    String getUnderlyingObjectType() throws SQLException;

    abstract boolean isValid() throws SQLException;
}
