package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBMethodMetadata extends DBObjectMetadata {

    boolean isDeterministic() throws SQLException;

    boolean isValid() throws SQLException;

    boolean isDebug() throws SQLException;

    int getOverload() throws SQLException;

    int getPosition() throws SQLException;

    String getLanguage() throws SQLException;

    String getTypeName() throws SQLException;

    String getPackageName() throws SQLException;
}
