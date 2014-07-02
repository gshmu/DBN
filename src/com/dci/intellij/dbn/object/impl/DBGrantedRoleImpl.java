package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.browser.model.BrowserTreeNode;
import com.dci.intellij.dbn.editor.DBContentType;
import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.DBRoleGrantee;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.common.DBObjectType;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBGrantedRoleImpl extends DBObjectImpl implements DBGrantedRole {
    private DBRole role;
    private boolean isAdminOption;
    private boolean isDefaultRole;

    public DBGrantedRoleImpl(DBRoleGrantee grantee, ResultSet resultSet) throws SQLException {
        super(grantee, DBContentType.NONE, resultSet);
    }

    @Override
    protected void initObject(ResultSet resultSet) throws SQLException {
        this.name = resultSet.getString("GRANTED_ROLE_NAME");
        this.role = getConnectionHandler().getObjectBundle().getRole(name);
        this.isAdminOption = resultSet.getString("IS_ADMIN_OPTION").equals("Y");
        this.isDefaultRole = resultSet.getString("IS_DEFAULT_ROLE").equals("Y");
    }

    public DBObjectType getObjectType() {
        return DBObjectType.GRANTED_ROLE;
    }

    public DBRoleGrantee getGrantee() {
        return (DBRoleGrantee) getParentObject();
    }

    public DBRole getRole() {
        return role;
    }

    public boolean isAdminOption() {
        return isAdminOption;
    }

    public boolean isDefaultRole() {
        return isDefaultRole;
    }

    @Override
    public DBObject getDefaultNavigationObject() {
        return role;
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
