package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.DatabaseBrowserUtils;
import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.browser.ui.HtmlToolTipBuilder;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseCompatibilityInterface;
import com.dci.intellij.dbn.database.common.metadata.def.DBUserMetadata;
import com.dci.intellij.dbn.object.*;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectBundle;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.list.DBObjectList;
import com.dci.intellij.dbn.object.common.list.DBObjectListContainer;
import com.dci.intellij.dbn.object.common.list.DBObjectNavigationList;
import com.dci.intellij.dbn.object.common.list.loader.DBObjectListFromRelationListLoader;
import com.dci.intellij.dbn.object.common.property.DBObjectProperty;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.ROOT_OBJECT;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.SESSION_USER;
import static com.dci.intellij.dbn.object.type.DBObjectType.*;

public class DBUserImpl extends DBObjectImpl<DBUserMetadata> implements DBUser {
    private DBObjectList<DBGrantedRole> roles;
    private DBObjectList<DBGrantedPrivilege> privileges;

    public DBUserImpl(ConnectionHandler connectionHandler, DBUserMetadata metadata) throws SQLException {
        super(connectionHandler, metadata);
    }

    @Nullable
    @Override
    public DBUser getOwner() {
        return this;
    }

    @Override
    protected String initObject(DBUserMetadata metadata) throws SQLException {
        String name = metadata.getUserName();
        set(DBObjectProperty.EXPIRED, metadata.isExpired());
        set(DBObjectProperty.LOCKED, metadata.isLocked());
        set(SESSION_USER, Strings.equalsIgnoreCase(name, getConnectionHandler().getUserName()));
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
    protected void initProperties() {
        properties.set(ROOT_OBJECT, true);
    }

    @NotNull
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
    protected @Nullable List<DBObjectNavigationList> createNavigationLists() {
        DBSchema schema = getSchema();
        if(schema != null) {
            List<DBObjectNavigationList> navigationLists = new LinkedList<>();
            navigationLists.add(DBObjectNavigationList.create("Schema", schema));
            return navigationLists;
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
