package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.def.DBRoleMetadata;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.ROOT_OBJECT;
import static com.dci.intellij.dbn.object.type.DBObjectRelationType.ROLE_PRIVILEGE;
import static com.dci.intellij.dbn.object.type.DBObjectRelationType.ROLE_ROLE;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public class DBRoleImpl extends DBObjectImpl<DBRoleMetadata> implements DBRole {
    private DBObjectList<DBGrantedPrivilege> privileges;
    private DBObjectList<DBGrantedRole> grantedRoles;

    public DBRoleImpl(ConnectionHandler connection, DBRoleMetadata metadata) throws SQLException {
        super(connection, metadata);
    }

    @Override
    protected String initObject(DBRoleMetadata metadata) throws SQLException {
        return metadata.getRoleName();
    }

    @Override
    protected void initLists() {
        DBObjectBundle objectBundle = getObjectBundle();
        DBObjectListContainer childObjects = ensureChildObjects();
        privileges = childObjects.createSubcontentObjectList(GRANTED_PRIVILEGE, this, objectBundle, ROLE_PRIVILEGE);
        grantedRoles = childObjects.createSubcontentObjectList(GRANTED_ROLE, this, objectBundle, ROLE_ROLE);
    }

    @Override
    protected void initProperties() {
        properties.set(ROOT_OBJECT, true);
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return ROLE;
    }

    @Override
    public List<DBGrantedPrivilege> getPrivileges() {
        return privileges.getObjects();
    }

    @Override
    public List<DBGrantedRole> getGrantedRoles() {
        return grantedRoles.getObjects();
    }

    @Override
    public List<DBUser> getUserGrantees() {
        List<DBUser> grantees = new ArrayList<>();
        List<DBUser> users = this.getConnection().getObjectBundle().getUsers();
        if (users != null) {
            for (DBUser user : users) {
                if (user.hasRole(this)) {
                    grantees.add(user);
                }
            }
        }
        return grantees;
    }

    @Override
    public List<DBRole> getRoleGrantees() {
        List<DBRole> grantees = new ArrayList<>();
        List<DBRole> roles = this.getConnection().getObjectBundle().getRoles();
        if (roles != null) {
            for (DBRole role : roles) {
                if (role.hasRole(this)) {
                    grantees.add(role);
                }
            }
        }
        return grantees;
    }

    @Override
    public boolean hasPrivilege(DBPrivilege privilege) {
        for (DBGrantedPrivilege rolePrivilege : getPrivileges()) {
            if (rolePrivilege.getPrivilege().equals(privilege)) {
                return true;
            }
        }
        for (DBGrantedRole inheritedRole : getGrantedRoles()) {
            if (inheritedRole.getRole().hasPrivilege(privilege)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasRole(DBRole role) {
        for (DBGrantedRole inheritedRole : getGrantedRoles()) {
            if (inheritedRole.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected @Nullable List<DBObjectNavigationList> createNavigationLists() {
        List<DBObjectNavigationList> navigationLists = new LinkedList<>();
        navigationLists.add(DBObjectNavigationList.create("User grantees", getUserGrantees()));

        if (ROLE.isSupported(this)) {
            navigationLists.add(DBObjectNavigationList.create("Role grantees", getRoleGrantees()));
        }
        return navigationLists;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(privileges, grantedRoles);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        DBObjectListFromRelationListLoader.create(ROLE, GRANTED_PRIVILEGE);
        DBObjectListFromRelationListLoader.create(ROLE, GRANTED_ROLE);
    }
}
