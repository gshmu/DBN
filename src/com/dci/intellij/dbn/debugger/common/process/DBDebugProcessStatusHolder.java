package com.dci.intellij.dbn.debugger.common.process;

import com.dci.intellij.dbn.common.property.PropertyHolderImpl;

public class DBDebugProcessStatusHolder extends PropertyHolderImpl<DBDebugProcessStatus>{
    @Override
    protected DBDebugProcessStatus[] properties() {
        return DBDebugProcessStatus.values();
    }
}
