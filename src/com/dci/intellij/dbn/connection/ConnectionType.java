package com.dci.intellij.dbn.connection;

import com.dci.intellij.dbn.common.util.Enumerations;
import lombok.Getter;

public enum ConnectionType{
    MAIN("Main", 0),
    POOL("Pool", 1),
    SESSION("Session", 2),
    DEBUG("Debug", 3),
    DEBUGGER("Debugger", 4),
    TEST("Test", 5)
    ;

    private final @Getter String name;
    private final @Getter int priority;

    ConnectionType(String name, int priority) {
        this.name = name;
        this.priority = priority;
    }

    public boolean isOneOf(ConnectionType... connectionTypes){
        return Enumerations.isOneOf(this, connectionTypes);
    }

    public boolean matches(ConnectionType... connectionTypes){
        return connectionTypes == null || connectionTypes.length == 0 || isOneOf(connectionTypes);
    }
}
