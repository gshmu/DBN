package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBRootObject;

public interface DBCharset extends DBRootObject {

    String getDisplayName();
    boolean isDeprecated();
    int getMaxLength();
}