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
        return resultSet.getString("COLUMN_NAME");
    }

    public String getDatasetName() throws SQLException {
        return resultSet.getString("DATASET_NAME");
    }

    public boolean isPrimaryKey() throws SQLException {
        return "Y".equals(resultSet.getString("IS_PRIMARY_KEY"));
    }

    public boolean isForeignKey() throws SQLException {
        return "Y".equals(resultSet.getString("IS_FOREIGN_KEY"));
    }

    public boolean isUniqueKey() throws SQLException {
        return "Y".equals(resultSet.getString("IS_UNIQUE_KEY"));
    }

    public boolean isNullable() throws SQLException {
        return "Y".equals(resultSet.getString("IS_NULLABLE"));
    }

    public boolean isHidden() throws SQLException {
        return "Y".equals(resultSet.getString("IS_HIDDEN"));
    }

    public int getPosition() throws SQLException {
        return resultSet.getInt("POSITION");
    }

    public DBDataTypeMetadata getDataType() {
        return dataType;
    }

}
