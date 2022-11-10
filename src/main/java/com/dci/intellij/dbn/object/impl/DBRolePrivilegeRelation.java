package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.object.DBGrantedPrivilege;
import com.dci.intellij.dbn.object.DBRole;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationImpl;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;

public class DBRolePrivilegeRelation extends DBObjectRelationImpl<DBRole, DBGrantedPrivilege> {
    public DBRolePrivilegeRelation(DBRole role, DBGrantedPrivilege privilege) {
        super(DBObjectRelationType.ROLE_PRIVILEGE, role, privilege);
    }

    public DBRole getRole() {
        return getSourceObject();
    }

    public DBGrantedPrivilege getPrivilege() {
        return getTargetObject();
    }
}