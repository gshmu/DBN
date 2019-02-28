package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBPrivilegeGrantee;
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

public class DBGrantedPrivilegeImpl extends DBObjectImpl implements DBGrantedPrivilege {
    private DBObjectRef<DBPrivilege> privilegeRef;

    public DBGrantedPrivilegeImpl(DBPrivilegeGrantee grantee, ResultSet resultSet) throws SQLException {
        super(grantee, resultSet);
    }

    @Override
    protected String initObject(ResultSet resultSet) throws SQLException {
        String name = resultSet.getString("GRANTED_PRIVILEGE_NAME");
        privilegeRef = DBObjectRef.from(getConnectionHandler().getObjectBundle().getPrivilege(name));
        set(ADMIN_OPTION, resultSet.getString("IS_ADMIN_OPTION").equals("Y"));
        return name;
    }

    @NotNull
    @Override
    public DBObjectType getObjectType() {
        return DBObjectType.GRANTED_PRIVILEGE;
    }

    @Override
    public DBPrivilegeGrantee getGrantee() {
        return (DBPrivilegeGrantee) getParentObject();
    }

    @Override
    public DBPrivilege getPrivilege() {
        return DBObjectRef.get(privilegeRef);
    }

    @Override
    public boolean isAdminOption() {
        return is(ADMIN_OPTION);
    }

    @Nullable
    @Override
    public DBObject getDefaultNavigationObject() {
        return getPrivilege();
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
