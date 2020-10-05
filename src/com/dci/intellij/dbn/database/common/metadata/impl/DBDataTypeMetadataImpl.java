package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.common.latent.Latent;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadataBase;
import com.dci.intellij.dbn.database.common.metadata.def.DBDataTypeMetadata;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBDataTypeMetadataImpl extends DBObjectMetadataBase implements DBDataTypeMetadata {
    private static final String EMPTY_PREFIX = "";
    private final String prefix;
    private final Latent<DBDataTypeMetadataImpl> collection = Latent.basic(() -> new DBDataTypeMetadataImpl(resultSet, "COLLECTION_"));

    DBDataTypeMetadataImpl(ResultSet resultSet) {
        this(resultSet, EMPTY_PREFIX);
    }

    private DBDataTypeMetadataImpl(ResultSet resultSet, String prefix) {
        super(resultSet);
        this.prefix = prefix;
    }

    public String getDataTypeName() throws SQLException {
        return resultSet.getString(prefix + "DATA_TYPE_NAME");
    }

    public String getDataTypeOwner() throws SQLException {
        return resultSet.getString(prefix + "DATA_TYPE_OWNER");
    }

    public String getDataTypeProgram() throws SQLException {
        return resultSet.getString(prefix + "DATA_TYPE_PACKAGE");
    }

    public long getDataLength() throws SQLException {
        return resultSet.getLong(prefix + "DATA_LENGTH");
    }

    public int getDataPrecision() throws SQLException {
        return resultSet.getInt(prefix + "DATA_PRECISION");
    }

    public int getDataScale() throws SQLException {
        return resultSet.getInt(prefix + "DATA_SCALE");
    }

    public boolean isSet() throws SQLException {
        return "Y".equals(resultSet.getString(prefix + "IS_SET"));
    }

    public DBDataTypeMetadataImpl collection() {
        return collection.get();
    }
}
