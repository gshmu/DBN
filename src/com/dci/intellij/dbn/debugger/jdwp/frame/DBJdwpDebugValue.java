package com.dci.intellij.dbn.debugger.jdwp.frame;

import com.dci.intellij.dbn.debugger.common.frame.DBDebugValue;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.intellij.xdebugger.frame.XValueModifier;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class DBJdwpDebugValue extends DBDebugValue<DBJdwpDebugStackFrame> {
    private DBJdwpDebugValueModifier modifier;
    private DBJdwpDebugStackFrame stackFrame;

    DBJdwpDebugValue(DBJdwpDebugStackFrame stackFrame, DBJdwpDebugValue parentValue, String variableName, @Nullable List<String> childVariableNames, Icon icon) {
        super(stackFrame, variableName, childVariableNames, parentValue, icon);
        this.stackFrame = stackFrame;
    }

    @Override
    public DBJdwpDebugProcess getDebugProcess() {
        return (DBJdwpDebugProcess) super.getDebugProcess();
    }

    @Override
    public XValueModifier getModifier() {
        if (modifier == null) modifier = new DBJdwpDebugValueModifier(this);
        return modifier;
    }
}
