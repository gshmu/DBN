package com.dci.intellij.dbn.connection;

public enum ConnectionType {
    TEST("Test"),
    MAIN("Main"),
    POOL("Pool"),
    SESSION("Session"),
    DEBUG("Debug")
    ;

    private String name;

    ConnectionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
