package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
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
    protected @Nullable List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> navigationLists = new LinkedList<>();
        navigationLists.add(DBObjectNavigationList.create("User grantees", getUserGrantees()));

        DatabaseCompatibilityInterface compatibilityInterface = getConnectionHandler().getInterfaceProvider().getCompatibilityInterface();
        if (compatibilityInterface.supportsObjectType(DBObjectType.ROLE.getTypeId())) {
            navigationLists.add(DBObjectNavigationList.create("Role grantees", getRoleGrantees()));
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
