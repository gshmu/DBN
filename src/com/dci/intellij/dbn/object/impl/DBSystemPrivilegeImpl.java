package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObjectType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBSystemPrivilegeImpl extends DBPrivilegeImpl implements DBSystemPrivilege {

    public DBSystemPrivilegeImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler, resultSet);
    }

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
