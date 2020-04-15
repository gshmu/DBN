package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;

public enum DBDebuggerType implements Presentable {
    JDBC("Classic (over JDBC)"),
    JDWP("JDWP (over TCP)"),
    NONE("None");

    private String name;

    DBDebuggerType(String name) {
        this.name = name;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }


    public boolean isDebug() {
        return this != NONE;
    }

    public boolean isSupported() {
        switch (this) {
            case JDWP: {
                try {
                    Class.forName("com.intellij.debugger.engine.JavaStackFrame");
                    Class.forName("com.intellij.debugger.PositionManagerFactory");
                    return true;
                } catch (ClassNotFoundException e) {
                    return false;
                }
            }
            case JDBC: return true;
            case NONE: return true;
        }
        return false;
    }

    public static DBDebuggerType get(String name) {
        for (DBDebuggerType debuggerType : DBDebuggerType.values()) {
            if (debuggerType.name.equals(name) || debuggerType.name().equals(name)) {
                return debuggerType;
            }
        }
        return null;
    }
}
