package com.dci.intellij.dbn.database.common.metadata.def;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;

import java.sql.SQLException;

public interface DBTypeAttributeMetadata extends DBObjectMetadata {

    String getAttributeName() throws SQLException;

    String getTypeName() throws SQLException;

    int getPosition() throws SQLException;

    DBDataTypeMetadata getDataType();
}
