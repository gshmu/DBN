package com.dci.intellij.dbn.object;

import com.dci.intellij.dbn.object.common.DBObject;

public interface DBCharset extends DBObject {

    String getDisplayName();
    boolean isDeprecated();
    int getMaxLength();
}