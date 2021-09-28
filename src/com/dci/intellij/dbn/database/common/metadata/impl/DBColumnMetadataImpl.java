package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBColumnMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBDataTypeMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBColumnMetadataImpl extends DBObjectMetadataBase implements DBColumnMetadata {
    private DBDataTypeMetadata dataType;

    public DBColumnMetadataImpl(ResultSet resultSet) {
        super(resultSet);
        dataType = new DBDataTypeMetadataImpl(resultSet);
    }

    public String getColumnName() throws SQLException {
        return getString("COLUMN_NAME");
    }

    public String getDatasetName() throws SQLException {
        return getString("DATASET_NAME");
    }

    public boolean isPrimaryKey() throws SQLException {
        return "Y".equals(getString("IS_PRIMARY_KEY"));
    }

    public boolean isForeignKey() throws SQLException {
        return "Y".equals(getString("IS_FOREIGN_KEY"));
    }

    public boolean isUniqueKey() throws SQLException {
        return "Y".equals(getString("IS_UNIQUE_KEY"));
    }

    public boolean isNullable() throws SQLException {
        return "Y".equals(getString("IS_NULLABLE"));
    }

    public boolean isHidden() throws SQLException {
        return "Y".equals(getString("IS_HIDDEN"));
    }

    public short getPosition() throws SQLException {
        return resultSet.getShort("POSITION");
    }

    public DBDataTypeMetadata getDataType() {
        return dataType;
    }

}
