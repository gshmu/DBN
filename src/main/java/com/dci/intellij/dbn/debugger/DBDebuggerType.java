package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.ui.Presentable;
import lombok.Getter;

import java.util.Objects;

import static com.dci.intellij.dbn.diagnostics.Diagnostics.conditionallyLog;

@Getter
public enum DBDebuggerType implements Presentable {
    JDBC("Classic (over JDBC)"),
    JDWP("JDWP (over TCP)"),
    NONE("None");

    private final String name;

    DBDebuggerType(String name) {
        this.name = name;
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
                    conditionallyLog(e);
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
            if (Objects.equals(debuggerType.name, name) || Objects.equals(debuggerType.name(), name)) {
                return debuggerType;
            }
        }
        return null;
    }
}
