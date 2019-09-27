package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBDataTypeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeAttributeMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTypeAttributeMetadataImpl extends DBObjectMetadataBase implements DBTypeAttributeMetadata {
    private DBDataTypeMetadata dataType;

    public DBTypeAttributeMetadataImpl(ResultSet resultSet) {
        super(resultSet);
        dataType = new DBDataTypeMetadataImpl(resultSet);
    }

    @Override
    public String getAttributeName() throws SQLException {
        return resultSet.getString("ATTRIBUTE_NAME");
    }

    @Override
    public String getTypeName() throws SQLException {
        return resultSet.getString("TYPE_NAME");
    }

    @Override
    public short getPosition() throws SQLException {
        return resultSet.getShort("POSITION");
    }

    public DBDataTypeMetadata getDataType() {
        return dataType;
    }
}
