package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBObject;

import java.util.List;

public interface DBPrivilege extends DBObject {

    List<DBUser> getUserGrantees();

    List<DBRole> getRoleGrantees();
}
