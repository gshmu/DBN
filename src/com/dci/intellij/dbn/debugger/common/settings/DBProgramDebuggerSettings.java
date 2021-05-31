package com.dci.intellij.dbn.debugger.common.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.xdebugger.settings.XDebuggerSettings;
import org.jetbrains.annotations.NotNull;

public class DBProgramDebuggerSettings extends XDebuggerSettings<DBProgramDebuggerState> {
    DBProgramDebuggerState state = new DBProgramDebuggerState();

    protected DBProgramDebuggerSettings() {
        super("db-program");
    }

    @NotNull
    @Override
    public Configurable createConfigurable() {
        return new DBProgramDebuggerConfigurable();
    }

    @Override
    public DBProgramDebuggerState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull DBProgramDebuggerState state) {
        this.state = state;
    }

}
