package com.dci.intellij.dbn.debugger;

import com.dci.intellij.dbn.common.ui.Presentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Icon;

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

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    public boolean isDebug() {
        return this != NONE;
    }

    public boolean isSupported() {
        switch (this) {
            case JDWP: {
                try {
                    Class.forName("com.intellij.debugger.engine.JavaStackFrame");
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

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
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
