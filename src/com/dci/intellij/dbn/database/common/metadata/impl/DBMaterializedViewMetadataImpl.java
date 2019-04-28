package com.dci.intellij.dbn.database.common.metadata.impl;

import com.dci.intellij.dbn.database.common.metadata.def.DBMaterializedViewMetadata;

import java.sql.ResultSet;

public class DBMaterializedViewMetadataImpl extends DBViewMetadataImpl implements DBMaterializedViewMetadata {

    public DBMaterializedViewMetadataImpl(ResultSet resultSet) {
        super(resultSet);
    }
}
