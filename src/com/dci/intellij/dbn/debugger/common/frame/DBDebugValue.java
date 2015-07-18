package com.dci.intellij.dbn.debugger.common.frame;

import javax.swing.Icon;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.debugger.common.process.DBDebugProcess;
import com.intellij.xdebugger.frame.XNamedValue;

public abstract class DBDebugValue<T extends DBDebugStackFrame> extends XNamedValue implements Comparable<DBDebugValue>{
    protected String value;
    protected String errorMessage;
    protected Icon icon;
    protected Set<String> childVariableNames;

    private T stackFrame;
    private DBDebugValue parentValue;

    protected DBDebugValue(T stackFrame, @NotNull String variableName, @Nullable Set<String> childVariableNames, @Nullable DBDebugValue parentValue, @Nullable Icon icon) {
        super(variableName);
        this.stackFrame = stackFrame;
        this.parentValue = parentValue;
        if (icon == null) {
            icon = parentValue == null ?
                    Icons.DBO_VARIABLE :
                    Icons.DBO_ATTRIBUTE;
        }
        this.icon = icon;
        this.childVariableNames = childVariableNames;
    }

    public T getStackFrame() {
        return stackFrame;
    }

    public DBDebugProcess getDebugProcess() {
        return stackFrame.getDebugProcess();
    }

    public DBDebugValue getParentValue() {
        return parentValue;
    }

    public String getValue() {
        return value;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getVariableName() {
        return getName();
    }


    @Override
    public int compareTo(@NotNull DBDebugValue remote) {
        return getName().compareTo(remote.getName());
    }
}

