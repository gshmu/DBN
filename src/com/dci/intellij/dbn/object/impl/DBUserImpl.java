package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBSchema;
import com.dci.intellij.dbn.object.DBSystemPrivilege;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectRelationType;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationListImpl;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.DBObjectType.GRANTED_PRIVILEGE;
import static com.dci.intellij.dbn.object.common.DBObjectType.GRANTED_ROLE;
import static com.dci.intellij.dbn.object.common.DBObjectType.USER;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.SESSION_USER;

public class DBUserImpl extends DBObjectImpl implements DBUser {
    DBObjectList<DBGrantedRole> roles;
    DBObjectList<DBGrantedPrivilege> privileges;

    public DBUserImpl(ConnectionHandler connectionHandler, ResultSet resultSet) throws SQLException {
        super(connectionHandler, resultSet);
    }

    @Nullable
    @Override
    public DBUser getOwner() {
        return this;
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("USER_NAME");
        set(DBObjectProperty.EXPIRED, resultSet.getString("IS_EXPIRED").equals("Y"));
        set(DBObjectProperty.LOCKED, resultSet.getString("IS_LOCKED").equals("Y"));
        set(SESSION_USER, name.equalsIgnoreCase(getConnectionHandler().getUserName()));
        return name;
    }

    @Override
    protected void initLists() {
        DBObjectListContainer childObjects = initChildObjects();
        DBObjectBundle sourceContentHolder = getConnectionHandler().getObjectBundle();
        roles = childObjects.createSubcontentObjectList(GRANTED_ROLE, this, sourceContentHolder, DBObjectRelationType.USER_ROLE);
        privileges = childObjects.createSubcontentObjectList(GRANTED_PRIVILEGE, this, sourceContentHolder, DBObjectRelationType.USER_PRIVILEGE);
    }


    @Override
    public DBObjectType getObjectType() {
        return USER;
    }

    @Override
    public DBSchema getSchema() {
        return getObjectBundle().getSchema(getName());
    }

    @Override
    public boolean isExpired() {
        return is(DBObjectProperty.EXPIRED);
    }

    @Override
    public boolean isLocked() {
        return is(DBObjectProperty.LOCKED);
    }

    @Override
    public boolean isSessionUser() {
        return is(DBObjectProperty.SESSION_USER);
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return isExpired() ?
               (isLocked() ? Icons.DBO_USER_EXPIRED_LOCKED : Icons.DBO_USER_EXPIRED) :
               (isLocked() ? Icons.DBO_USER_LOCKED : Icons.DBO_USER);
    }

    @Nullable
    @Override
    public DBObject getDefaultNavigationObject() {
        return getSchema();
    }

    @Override
    public List<DBGrantedPrivilege> getPrivileges() {
        return privileges.getObjects();
    }

    @Override
    public List<DBGrantedRole> getRoles() {
        return roles.getObjects();
    }

    @Override
    public boolean hasSystemPrivilege(DBSystemPrivilege systemPrivilege) {
        for (DBGrantedPrivilege grantedPrivilege : getPrivileges()) {
            if (grantedPrivilege.getPrivilege().equals(systemPrivilege)) {
                return true;
            }
        }
        DatabaseCompatibilityInterface compatibilityInterface = getConnectionHandler().getInterfaceProvider().getCompatibilityInterface();
        if (compatibilityInterface.supportsObjectType(GRANTED_ROLE.getTypeId())) {
            for (DBGrantedRole grantedRole : getRoles()) {
                if (grantedRole.getRole().hasPrivilege(systemPrivilege)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasRole(DBRole role) {
        for (DBGrantedRole grantedRole : getRoles()) {
            if (grantedRole.getRole().equals(role)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void buildToolTip(HtmlToolTipBuilder ttb) {
        ttb.append(true, getObjectType().getName(), true);
        if (isLocked() || isExpired()) {
            if (isLocked() && isExpired())
                ttb.append(false, " - expired & locked" , true);
            else if (isLocked())
                ttb.append(false, " - locked" , true); else
                ttb.append(false, " - expired" , true);


        }

        ttb.createEmptyRow();
        super.buildToolTip(ttb);
    }

    @Override
    protected List<DBObjectNavigationList> createNavigationLists() {
        DBSchema schema = getSchema();
        if(schema != null) {
            List<DBObjectNavigationList> objectNavigationLists = new ArrayList<>();
            objectNavigationLists.add(new DBObjectNavigationListImpl("Schema", schema));
            return objectNavigationLists;
        }
        return null;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    @Override
    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return DatabaseBrowserUtils.createList(roles, privileges);
    }

    /*********************************************************
     *                         Loaders                       *
     *********************************************************/
    static {
        DBObjectListFromRelationListLoader.create(USER, GRANTED_ROLE);
        DBObjectListFromRelationListLoader.create(USER, GRANTED_PRIVILEGE);
    }
}
