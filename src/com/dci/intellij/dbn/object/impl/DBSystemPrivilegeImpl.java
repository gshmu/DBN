package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.ROOT_OBJECT;

public class DBSystemPrivilegeImpl extends DBPrivilegeImpl<DBPrivilegeMetadata> implements DBSystemPrivilege {

    public DBSystemPrivilegeImpl(ConnectionHandler connectionHandler, DBPrivilegeMetadata metadata) throws SQLException {
        super(connectionHandler, metadata);
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

    @Override
    public List<DBUser> getUserGrantees() {
        List<DBUser> grantees = new ArrayList<DBUser>();
        List<DBUser> users = getConnectionHandler().getObjectBundle().getUsers();
        if (users != null) {
            for (DBUser user : users) {
                if (user.hasSystemPrivilege(this)) {
                    grantees.add(user);
                }
            }
        }
        return grantees;
    }
}
