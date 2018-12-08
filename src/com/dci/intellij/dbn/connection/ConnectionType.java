package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.util.EnumerationUtil;

public enum ConnectionType {
    TEST("Test"),
    MAIN("Main"),
    POOL("Pool"),
    SESSION("Session"),
    DEBUG("Debug"),
    DEBUGGER("Debugger")
    ;

    private String name;

    ConnectionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean isOneOf(ConnectionType... connectionTypes){
        return EnumerationUtil.isOneOf(this, connectionTypes);
    }
}
