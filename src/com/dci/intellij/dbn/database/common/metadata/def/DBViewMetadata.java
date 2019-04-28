package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBViewMetadata extends DBObjectMetadata {

    String getViewName() throws SQLException;

    String getViewType() throws SQLException;

    String getViewTypeOwner() throws SQLException;

    boolean isSystemView() throws SQLException;
}
