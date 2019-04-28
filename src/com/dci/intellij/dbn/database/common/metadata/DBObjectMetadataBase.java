package com.dci.intellij.dbn.database.common.metadata;

import java.sql.ResultSet;

public abstract class DBObjectMetadataBase {
    protected ResultSet resultSet;

    public DBObjectMetadataBase(ResultSet resultSet) {
        this.resultSet = resultSet;
    }
}
