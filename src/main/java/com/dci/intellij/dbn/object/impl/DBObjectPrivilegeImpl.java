package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dci.intellij.dbn.object.DBObjectPrivilege;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class DBObjectPrivilegeImpl extends DBPrivilegeImpl<DBPrivilegeMetadata> implements DBObjectPrivilege {

    public DBObjectPrivilegeImpl(ConnectionHandler connection, DBPrivilegeMetadata metadata) throws SQLException {
        super(connection, metadata);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.OBJECT_PRIVILEGE;
    }

}
