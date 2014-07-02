package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBPrivilegeGrantee;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBGrantedPrivilegeImpl extends DBObjectImpl implements DBGrantedPrivilege {
    private DBPrivilege privilege;
    private boolean isAdminOption;

    public DBGrantedPrivilegeImpl(DBPrivilegeGrantee grantee, ResultSet resultSet) throws SQLException {
        super(grantee, DBContentType.NONE, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        this.name = resultSet.getString("GRANTED_PRIVILEGE_NAME");
        this.privilege = getConnectionHandler().getObjectBundle().getPrivilege(name);
        this.isAdminOption = resultSet.getString("IS_ADMIN_OPTION").equals("Y");
    }

    public DBObjectType getObjectType() {
        return DBObjectType.GRANTED_PRIVILEGE;
    }

    public DBPrivilegeGrantee getGrantee() {
        return (DBPrivilegeGrantee) getParentObject();
    }

    public DBPrivilege getPrivilege() {
        return privilege;
    }

    public boolean isAdminOption() {
        return isAdminOption;
    }

    @Override
    public DBObject getDefaultNavigationObject() {
        return privilege;
    }

    /*********************************************************
     *                     TreeElement                       *
     *********************************************************/
    public boolean isLeafTreeElement() {
        return true;
    }


    @NotNull
    public List<BrowserTreeNode> buildAllPossibleTreeChildren() {
        return BrowserTreeNode.EMPTY_LIST;
    }

}
