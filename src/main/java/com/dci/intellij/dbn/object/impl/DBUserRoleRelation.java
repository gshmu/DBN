package com.dci.intellij.dbn.object.impl;

import com.dci.intellij.dbn.object.DBGrantedRole;
import com.dci.intellij.dbn.object.DBUser;
import com.dci.intellij.dbn.object.common.list.DBObjectRelationImpl;
import com.dci.intellij.dbn.object.type.DBObjectRelationType;

class DBUserRoleRelation extends DBObjectRelationImpl<DBUser, DBGrantedRole> {
    public DBUserRoleRelation(DBUser user, DBGrantedRole role) {
        super(DBObjectRelationType.USER_ROLE, user, role);
    }

    public DBUser getUser() {
        return getSourceObject();
    }

    public DBGrantedRole getRole() {
        return getTargetObject();
    }
}