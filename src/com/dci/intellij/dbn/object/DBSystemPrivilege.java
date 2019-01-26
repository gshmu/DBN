package com.dci.intellij.dbn.object;

import java.util.List;

public interface DBSystemPrivilege extends DBPrivilege {

    @Override
    List<DBUser> getUserGrantees();
}
