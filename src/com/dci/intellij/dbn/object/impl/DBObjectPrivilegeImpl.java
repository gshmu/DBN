package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.object.DBObjectPrivilege;
import com.dci.intellij.dbn.object.common.DBObjectType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DBObjectPrivilegeImpl extends DBPrivilegeImpl implements DBObjectPrivilege {

    public DBObjectPrivilegeImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler, resultSet);
    }

    public DBObjectType getObjectType() {
        return DBObjectType.OBJECT_PRIVILEGE;
    }

}
