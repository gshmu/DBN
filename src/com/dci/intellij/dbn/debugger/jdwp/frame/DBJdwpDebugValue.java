package com.dci.intellij.dbn.debugger.jdwp.frame;

import javax.swing.Icon;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.debugger.common.frame.DBDebugValue;
import com.dci.intellij.dbn.debugger.jdwp.process.DBJdwpDebugProcess;
import com.intellij.xdebugger.frame.XValueModifier;

public class DBJdwpDebugValue extends DBDebugValue<DBJdwpDebugStackFrame> {
    private DBJdwpDebugValueModifier modifier;
    private DBJdwpDebugStackFrame stackFrame;

    public DBJdwpDebugValue(DBJdwpDebugStackFrame stackFrame, DBJdwpDebugValue parentValue, String variableName, @Nullable Set<String> childVariableNames, Icon icon) {
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
