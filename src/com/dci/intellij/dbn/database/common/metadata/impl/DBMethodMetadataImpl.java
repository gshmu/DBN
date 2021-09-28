package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBMethodMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class DBMethodMetadataImpl extends DBObjectMetadataBase implements DBMethodMetadata {
    DBMethodMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }

    @Override
    public boolean isDeterministic() throws SQLException {
        return getString("IS_DETERMINISTIC").equals("Y");
    }

    @Override
    public boolean isValid() throws SQLException {
        return getString("IS_VALID").equals("Y");
    }

    @Override
    public boolean isDebug() throws SQLException {
        return getString("IS_DEBUG").equals("Y");
    }

    @Override
    public short getOverload() throws SQLException {
        return resultSet.getShort("OVERLOAD");
    }

    @Override
    public short getPosition() throws SQLException {
        return resultSet.getShort("POSITION");
    }

    @Override
    public String getLanguage() throws SQLException {
        return getString("LANGUAGE");
    }

    @Override
    public String getTypeName() throws SQLException {
        return getString("TYPE_NAME");
    }

    @Override
    public String getPackageName() throws SQLException {
        return getString("PACKAGE_NAME");
    }
}
