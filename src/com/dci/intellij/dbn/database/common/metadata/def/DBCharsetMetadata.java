package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBCharsetMetadata extends DBObjectMetadata {

    String getCharsetName() throws SQLException;

    String getDisplayName() throws SQLException;

    short getMaxLength() throws SQLException;

    boolean isDeprecated() throws SQLException;
}
