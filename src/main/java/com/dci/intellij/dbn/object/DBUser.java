package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBUser extends DBRoleGrantee, DBPrivilegeGrantee {
    boolean isExpired();
    boolean isLocked();
    boolean isSessionUser();

    List<DBGrantedPrivilege> getPrivileges();
    List<DBGrantedRole> getRoles();

    boolean hasPrivilege(DBPrivilege privilege);
    boolean hasRole(DBRole role);
}
