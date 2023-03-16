package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBRootObject;

import java.util.List;

public interface DBPrivilege extends DBRootObject {

    List<DBUser> getUserGrantees();

    List<DBRole> getRoleGrantees();
}
