package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBArgumentMetadata extends DBObjectMetadata {

    String getArgumentName() throws SQLException;

    String getProgramName() throws SQLException;

    String getMethodName() throws SQLException;

    String getMethodType() throws SQLException;

    String getInOut() throws SQLException;

    int getOverload() throws SQLException;

    int getPosition() throws SQLException;

    int getSequence() throws SQLException;

    DBDataTypeMetadata getDataType();
}
