package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBArgumentMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBDataTypeMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBArgumentMetadataImpl extends DBObjectMetadataBase implements DBArgumentMetadata {
    private DBDataTypeMetadata dataType;

    public DBArgumentMetadataImpl(ResultSet resultSet) {
        super(resultSet);
        dataType = new DBDataTypeMetadataImpl(resultSet);
    }

    @Override
    public String getArgumentName() throws SQLException {
        return resultSet.getString("ARGUMENT_NAME");
    }

    @Override
    public String getProgramName() throws SQLException {
        return resultSet.getString("PROGRAM_NAME");
    }

    @Override
    public String getMethodName() throws SQLException {
        return resultSet.getString("METHOD_NAME");
    }

    @Override
    public String getMethodType() throws SQLException {
        return resultSet.getString("METHOD_TYPE");
    }

    @Override
    public String getInOut() throws SQLException {
        return resultSet.getString("IN_OUT");
    }

    @Override
    public int getOverload() throws SQLException {
        return resultSet.getInt("OVERLOAD");
    }

    @Override
    public int getPosition() throws SQLException {
        return resultSet.getInt("POSITION");
    }

    @Override
    public int getSequence() throws SQLException {
        return resultSet.getInt("SEQUENCE");
    }

    public DBDataTypeMetadata getDataType() {
        return dataType;
    }

}
