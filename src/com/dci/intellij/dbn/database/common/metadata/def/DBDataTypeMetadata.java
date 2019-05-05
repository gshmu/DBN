package com.dci.intellij.dbn.database.common.metadata.def;

import java.sql.SQLException;

public interface DBDataTypeMetadata {

    String getDataTypeName() throws SQLException;

    String getDataTypeOwner() throws SQLException;

    String getDataTypeProgram() throws SQLException;

    long getDataLength() throws SQLException;

    int getDataPrecision() throws SQLException;

    int getDataScale() throws SQLException;

    boolean isSet() throws SQLException;

    DBDataTypeMetadata collection();
}
