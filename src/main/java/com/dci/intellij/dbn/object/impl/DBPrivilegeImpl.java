package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.common.content.DynamicContent;
import com.dci.intellij.dbn.common.content.loader.DynamicContentLoaderImpl;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.common.metadata.DBObjectMetadata;
import com.dci.intellij.dbn.database.common.metadata.def.DBPrivilegeMetadata;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public abstract class DBPrivilegeImpl<M extends DBPrivilegeMetadata> extends DBObjectImpl<M> implements DBPrivilege {
    private DBObjectList<DBUser> userGrantees;
    private DBObjectList<DBRole> roleGrantees;

    DBPrivilegeImpl(ConnectionHandler connection, M metadata) throws SQLException {
        super(connection, metadata);
    }

    @Override
    protected String initObject(M metadata) throws SQLException {
        return metadata.getPrivilegeName();
    }

    @Override
    protected void initLists() {
        DBObjectListContainer childObjects = ensureChildObjects();
        userGrantees = childObjects.createSubcontentObjectList(USER, this, getObjectBundle(), USER);
        roleGrantees = childObjects.createSubcontentObjectList(ROLE, this, getObjectBundle(), ROLE);
    }

    @Override
    public List<DBUser> getUserGrantees() {
        return userGrantees.getObjects();
    }

    public List<DBRole> getRoleGrantees() {
        return roleGrantees.getObjects();
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
    public boolean isLeaf() {
        return true;
    }


    static {
        new DynamicContentLoaderImpl<DBUser, DBObjectMetadata>(PRIVILEGE, USER, true) {

            @Override
            public void loadContent(DynamicContent<DBUser> content) {
                DBPrivilege privilege = content.ensureParentEntity();
                List<DBUser> users = privilege.getObjectBundle().getUsers();
                if (users == null) return;

                List<DBUser> grantees = new ArrayList<>();
                for (DBUser user : users) {
                    if (user.hasPrivilege(privilege)) {
                        grantees.add(user);
                    }
                }
                content.setElements(grantees);
            }
        };

        new DynamicContentLoaderImpl<DBRole, DBObjectMetadata>(PRIVILEGE, ROLE, true) {

            @Override
            public void loadContent(DynamicContent<DBRole> content) {
                DBPrivilege privilege = content.ensureParentEntity();
                List<DBRole> roles = privilege.getObjectBundle().getRoles();
                if (roles == null) return;

                List<DBRole> grantees = new ArrayList<>();
                for (DBRole role : roles) {
                    if (role.hasPrivilege(privilege)) {
                        grantees.add(role);
                    }
                }
                content.setElements(grantees);
            }
        };
    }
}
