package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.def.DBCharsetMetadata;
import com.dci.intellij.dbn.object.DBCharset;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DBCharsetImpl extends DBObjectImpl<DBCharsetMetadata> implements DBCharset {
    private int maxLength;
    public DBCharsetImpl(ConnectionHandler connectionHandler, DBCharsetMetadata resultSet) throws SQLException {
        super(connectionHandler, resultSet);
    }

    @Override
    protected String initObject(DBCharsetMetadata metadata) throws SQLException {
        maxLength = metadata.getMaxLength();
        return metadata.getCharsetName();
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.CHARSET;
    }

    @Override
    public int getMaxLength() {
        return maxLength;
    }
}
