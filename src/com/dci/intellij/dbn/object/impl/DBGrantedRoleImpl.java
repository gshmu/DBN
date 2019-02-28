package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBRoleGrantee;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.ADMIN_OPTION;
import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.DEFAULT_ROLE;

public class DBGrantedRoleImpl extends DBObjectImpl implements DBGrantedRole {
    private DBObjectRef<DBRole> roleRef;

    public DBGrantedRoleImpl(DBRoleGrantee grantee, ResultSet resultSet) throws SQLException {
        super(grantee, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("GRANTED_ROLE_NAME");
        this.roleRef = DBObjectRef.from(getConnectionHandler().getObjectBundle().getRole(name));
        set(ADMIN_OPTION, resultSet.getString("IS_ADMIN_OPTION").equals("Y"));
        set(DEFAULT_ROLE, resultSet.getString("IS_DEFAULT_ROLE").equals("Y"));
        return name;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.GRANTED_ROLE;
    }

    @Override
    public DBRoleGrantee getGrantee() {
        return (DBRoleGrantee) getParentObject();
    }

    @Override
    public DBRole getRole() {
        return DBObjectRef.get(roleRef);
    }

    @Override
    public boolean isAdminOption() {
        return is(ADMIN_OPTION);
    }

    @Override
    public boolean isDefaultRole() {
        return is(DEFAULT_ROLE);
    }

    @Nullable
    @Override
    public DBObject getDefaultNavigationObject() {
        return getRole();
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
