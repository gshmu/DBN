package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.type.DBObjectType;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class DBPrivilegeImpl<M extends DBPrivilegeMetadata> extends DBObjectImpl<M> implements DBPrivilege {
    DBPrivilegeImpl(ConnectionHandler connectionHandler, M metadata) throws SQLException {
        super(connectionHandler, metadata);
    }

    @Override
    protected String initObject(M metadata) throws SQLException {
        return metadata.getPrivilegeName();
    }

    @Override
    public List<DBUser> getUserGrantees() {
        return new ArrayList<>();
    }

    public List<DBRole> getRoleGrantees() {
        List<DBRole> grantees = new ArrayList<>();
        List<DBRole> roles = getConnectionHandler().getObjectBundle().getRoles();
        if (roles != null) {
            for (DBRole role : roles) {
                if (role.hasPrivilege(this)) {
                    grantees.add(role);
                }
            }
        }
        return grantees;
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> navigationLists = new ArrayList<>();
        navigationLists.add(new DBObjectNavigationListImpl<>("User grantees", getUserGrantees()));
        if (getConnectionHandler().getInterfaceProvider().getCompatibilityInterface().supportsObjectType(DBObjectType.ROLE.getTypeId())) {
            navigationLists.add(new DBObjectNavigationListImpl<>("Role grantees", getRoleGrantees()));
        }
        return navigationLists;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @Override
    public boolean isLeaf() {
        return true;
    }

}
