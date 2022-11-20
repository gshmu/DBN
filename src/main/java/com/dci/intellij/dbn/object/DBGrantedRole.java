package com.dci.intellij.dbn.object;

public interface DBGrantedRole extends DBCastedObject{
    DBRoleGrantee getGrantee();
    DBRole getRole();
    boolean isAdminOption();
    boolean isDefaultRole();

    boolean hasPrivilege(DBPrivilege privilege);
}