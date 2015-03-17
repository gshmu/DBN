package com.dci.intellij.dbn.connection;

public enum ConnectionType {
    MAIN("Main"),
    POOL("Pool"),
    TEST("Test");

    private String name;

    ConnectionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
