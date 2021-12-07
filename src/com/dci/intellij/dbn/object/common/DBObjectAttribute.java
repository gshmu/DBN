package com.dci.intellij.dbn.object.common;

import com.dci.intellij.dbn.common.util.Naming;

public class DBObjectAttribute {
    private String name;
    private String friendlyName;
    public DBObjectAttribute(String name) {
        this.name = name;
        this.friendlyName = Naming.createFriendlyName(name);
    }

    public String getName() {
        return name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
