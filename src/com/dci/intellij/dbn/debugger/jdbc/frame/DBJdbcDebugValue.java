package com.dci.intellij.dbn.debugger.jdbc.frame;

import javax.swing.Icon;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.debugger.common.frame.DBDebugValue;
import com.dci.intellij.dbn.debugger.jdbc.DBJdbcDebugProcess;
import com.intellij.xdebugger.frame.XValueModifier;

public class DBJdbcDebugValue extends DBDebugValue<DBJdbcDebugStackFrame>{
    private DBJdbcDebugValueModifier modifier;

    public DBJdbcDebugValue(DBJdbcDebugStackFrame stackFrame, DBJdbcDebugValue parentValue, String variableName, @Nullable Set<String> childVariableNames, Icon icon) {
        super(stackFrame, variableName, childVariableNames, parentValue, icon);
    }

    @Override
    public DBJdbcDebugProcess getDebugProcess() {
        return (DBJdbcDebugProcess) super.getDebugProcess();
    }

    @Override
    public XValueModifier getModifier() {
        if (modifier == null) modifier = new DBJdbcDebugValueModifier(this);
        return modifier;
    }
}
