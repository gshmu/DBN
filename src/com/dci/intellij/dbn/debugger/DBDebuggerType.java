package com.dci.intellij.dbn.debugger;

import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.ui.Presentable;

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

    public boolean isActive() {
        return this != NONE;
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
    }}
