package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.def.DBDataTypeMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBTypeMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBTypeMetadataImpl extends DBProgramMetadataImpl implements DBTypeMetadata {
    private DBDataTypeMetadata dataType;

    public DBTypeMetadataImpl(ResultSet resultSet) {
        super(resultSet);
        dataType = new DBDataTypeMetadataImpl(resultSet);
    }

    public String getTypeName() throws SQLException {
        return resultSet.getString("TYPE_NAME");
    }

    public String getTypeCode() throws SQLException {
        return resultSet.getString("TYPECODE");
    }

    public String getSupertypeOwner() throws SQLException {
        return resultSet.getString("SUPERTYPE_OWNER");
    }

    public String getSupertypeName() throws SQLException {
        return resultSet.getString("SUPERTYPE_NAME");
    }

    public boolean isCollection() throws SQLException {
        String typeCode = getTypeCode();
        return "COLLECTION".equals(typeCode);
    }

    @Override
    public String getPackageName() throws SQLException {
        return resultSet.getString("PACKAGE_NAME");
    }

    @Override
    public DBDataTypeMetadata getDataType() {
        return dataType;
    }
}
