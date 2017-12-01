package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.common.property.Property;
import com.dci.intellij.dbn.common.property.PropertyGroup;
import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public enum DBDebugProcessStatus implements Property{
    BREAKPOINT_SETTING_ALLOWED,
    TARGET_EXECUTION_STARTED,
    TARGET_EXECUTION_TERMINATED,
    TARGET_EXECUTION_THREW_EXCEPTION,
    SESSION_INITIALIZATION_THREW_EXCEPTION,
    PROCESS_TERMINATING,
    PROCESS_TERMINATED,
    PROCESS_STOPPED_NORMALLY,
    DEBUGGER_STOPPING;


    @Override
    public int index() {
        return PropertyHolderImpl.idx(this);
    }

    @Override
    public PropertyGroup group() {
        return null;
    }

    @Override
    public boolean implicit() {
        return false;
    }
}
