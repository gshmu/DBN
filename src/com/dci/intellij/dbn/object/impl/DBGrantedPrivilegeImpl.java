package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.database.common.metadata.def.DBGrantedPrivilegeMetadata;
import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBPrivilege;
import com.dci.intellij.dbn.object.DBPrivilegeGrantee;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.common.DBObjectImpl;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.object.type.DBObjectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

import static com.dci.intellij.dbn.object.common.property.DBObjectProperty.ADMIN_OPTION;

public class DBGrantedPrivilegeImpl extends DBObjectImpl<DBGrantedPrivilegeMetadata> implements DBGrantedPrivilege {
    private DBObjectRef<DBPrivilege> privilegeRef;

    public DBGrantedPrivilegeImpl(DBPrivilegeGrantee grantee, DBGrantedPrivilegeMetadata metadata) throws SQLException {
        super(grantee, metadata);
    }

    @Override
    protected String initObject(DBGrantedPrivilegeMetadata metadata) throws SQLException {
        String name = metadata.getGrantedPrivilegeName();
        privilegeRef = DBObjectRef.of(this.getConnection().getObjectBundle().getPrivilege(name));
        set(ADMIN_OPTION, metadata.isAdminOption());
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

}
