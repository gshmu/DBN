package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import org.jetbrains.annotations.NotNull;

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
        return new ArrayList<DBUser>();
    }

    public List<DBRole> getRoleGrantees() {
        List<DBRole> grantees = new ArrayList<DBRole>();
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
        List<DBObjectNavigationList> navigationLists = new ArrayList<DBObjectNavigationList>();
        navigationLists.add(new DBObjectNavigationListImpl<DBUser>("User grantees", getUserGrantees()));
        if (getConnectionHandler().getInterfaceProvider().getCompatibilityInterface().supportsObjectType(DBObjectType.ROLE.getTypeId())) {
            navigationLists.add(new DBObjectNavigationListImpl<DBRole>("Role grantees", getRoleGrantees()));    
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


    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return EMPTY_TREE_NODE_LIST;
    }
}
