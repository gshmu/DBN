package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.ROOT_OBJECT;

class DBSystemPrivilegeImpl extends DBPrivilegeImpl<DBPrivilegeMetadata> implements DBSystemPrivilege {



    public DBSystemPrivilegeImpl(ConnectionHandler connection, DBPrivilegeMetadata metadata) throws SQLException {
        super(connection, metadata);
    }

    private Byte getUserListSignature() {
        DBObjectList<DBObject> objectList = getObjectBundle().getObjectLists().getObjectList(DBObjectType.USER);
        return objectList == null ? 0 : objectList.getSignature();
    }

    @Override
    protected void initProperties() {
        properties.set(ROOT_OBJECT, true);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.SYSTEM_PRIVILEGE;
    }
}
